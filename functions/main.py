import os
import time
import logging

from firebase_admin import initialize_app, credentials, db, messaging
from firebase_admin.exceptions import FirebaseError
from firebase_admin.messaging import UnregisteredError
from firebase_functions import https_fn, scheduler_fn

########################################################################################################################
streams = frozenset({'ZN', 'NQ', 'BTC', 'ES', 'GC', 'E6', 'CL'})
########################################################################################################################

# initialize logs
logger = logging.getLogger(__name__)

# initialize database target
db_config = {
    'databaseURL': 'https://signalvoice-api-default-rtdb.firebaseio.com/'
}

# production cloud runtime injects service account credentials
if os.getenv('FUNCTION_TARGET'):
    APP = initialize_app(options = db_config)

# local development needs explicit credentials
else:
    APP = initialize_app(
        credential = credentials.Certificate('admin.json'),
        options = db_config
    )

# user sources
TRADINGVIEW = {'52.89.214.238', '34.212.75.30', '54.218.53.128', '52.32.178.7'}
TRENDSPIDER = '3.12.143.24'

# configure notification
BASE_CONFIG = messaging.AndroidConfig(
    priority = 'normal',  # 'normal' default, 'high' attempts to wake device in doze mode
    ttl = 0)  # ttl is 'time to live', 0 = 'now or never', '43200' = 12h, 86400 = 24h

# database
TOKENS = db.reference('tokens')
USERS = db.reference('users')
STREAMS = db.reference('streams')
SIGNALS = db.reference('signals')

# retained days for each source
DAY_MILLIS = 86400000
STREAM_RETAINED_DAYS = 2
SIGNALS_RETAINED_DAYS = 7

@https_fn.on_request()
def signal(req: https_fn.Request) -> https_fn.Response:

    # parse request
    stream = req.args.get(key = 'broadcast', type = str) # query param
    uid = req.args.get(key = 'uid', type = str) # query param
    message = req.get_data(as_text = True) # message as plain/text from body
    source_ip = req.headers.get('X-Forwarded-For') # extract source ip from header
    source_override = req.headers.get('dev-source') # catch dev environment: postman, insomnia, ...

    # clean raw message
    message = message.strip() if message else ''
    message = message[:200] + '...' if len(message) > 200 else message # keep messages short for client display

    # calculate raw utc timestamp from system (millis)
    timestamp = time.time_ns() // 1_000_000 # // floor division discards remainder after ms

    # catch malformed request
    if req.method != 'POST':
        return https_fn.Response('Request must be POST and include stream or uid as query parameter')
    if stream and uid:
        return https_fn.Response('Request must not include both stream and uid query parameters')
    if not stream and not uid:
        return https_fn.Response('Request must include either stream or uid query parameters')

    # broadcast to stream subscribers
    if stream:

        # prevent unauthorized broadcasts
        if stream not in streams:
            return https_fn.Response(f'Stream {stream} does not exist')

        broadcast_to_stream(stream, timestamp, message)
        write_stream_message_to_database(stream, timestamp, message)

        return https_fn.Response(f'Broadcasted to stream: {stream}')

    # send message to single device
    if uid:

        # get user device token
        device_token = USERS.child(uid).get()
        if device_token is None:
            return https_fn.Response(f'Sign-in to hear message')

        # get source from ip
        if source_override: source = source_override # dev environment
        else: source = resolve_source_from_ip(source_ip) # user signal

        send_message_to_single_device(uid, device_token, timestamp, message, source)
        write_user_message_to_database(uid, timestamp, message, source)

        return https_fn.Response(f'Message sent to uid: {uid}')

    # respond with simple generic message, should never happen
    return https_fn.Response('Thank you for using BarAudio! :)')

def resolve_source_from_ip(source_ip: str) -> str:

    # catch empty ip list
    if not source_ip: return 'unknown'

    # take first instance of IPv4 address
    ips = [ip.strip() for ip in source_ip.split(',')]
    ip = next((ip for ip in ips if '.' in ip), None)

    # catch empty ip
    if not ip: return 'unknown'

    # clean ip
    ip = ip.strip()
    if len(ip) > 45: return 'unknown' # IPv6, localhost, ...

    if ip in TRADINGVIEW: return 'tradingview'
    if ip == TRENDSPIDER: return 'trendspider'

    return 'unknown'

########################################################################################################################

def broadcast_to_stream(stream, timestamp, message):

    # construct notification
    broadcast = messaging.Message(
        data = {
            'stream': stream,
            'timestamp': str(timestamp),
            'message': message},
        android = BASE_CONFIG,
        topic = stream)

    # broadcast to stream subscribers
    try: messaging.send(broadcast)
    except FirebaseError: logger.exception(f'Broadcast to stream failed: {stream}')

def send_message_to_single_device(uid, device_token, timestamp, message, source):

    # construct notification
    notification = messaging.Message(
        data = {
            'uid': uid,
            'timestamp': str(timestamp),
            'message': message,
            'source': source},
        android = BASE_CONFIG,
        token = device_token)

    # send notification to single device
    try: messaging.send(notification)
    except UnregisteredError:

        # delete orphaned uid:token
        current_token = USERS.child(uid).get()
        if current_token == device_token:
            TOKENS.child(device_token).delete()
            USERS.child(uid).delete()

    except FirebaseError: logger.exception(f'Send to uid failed: {uid}')

########################################################################################################################

def write_stream_message_to_database(stream, timestamp, message):
    STREAMS.child(stream).child(str(timestamp)).set({
        'message': message })

def write_user_message_to_database(uid, timestamp, message, source):
    SIGNALS.child(uid).child(str(timestamp)).set({
        'message': message,
        'source': source })

########################################################################################################################

@scheduler_fn.on_schedule(schedule = '1 17 * * 1-5', timezone = 'America/New_York') # run once per weekday at NYC 5:01 PM (market close)
def purge_stale_messages(event: scheduler_fn.ScheduledEvent):

    timestamp = time.time_ns() // 1_000_000

    # calculate cutoff timestamp
    stream_cutoff = timestamp - STREAM_RETAINED_DAYS * DAY_MILLIS
    signals_cutoff = timestamp - SIGNALS_RETAINED_DAYS * DAY_MILLIS

    purge_streams(stream_cutoff)
    purge_signals(signals_cutoff)

    logger.warning(f'stream_cutoff={stream_cutoff}')
    logger.warning(f'signals_cutoff={signals_cutoff}')
    logger.warning('purge_stale_messages completed')

def purge_streams(timestamp):

    for stream in streams:

        node = STREAMS.child(stream)

        # query old messages
        old_messages = node.order_by_key().end_at(str(timestamp)).get()
        if not old_messages: continue

        # batch delete
        old_messages = { key: None for key in old_messages.keys() }
        node.update(old_messages)

def purge_signals(timestamp):

    users = SIGNALS.get(shallow = True)
    if not users: return

    for uid in users.keys():

        node = SIGNALS.child(uid)

        # query old messages
        old_messages = node.order_by_key().end_at(str(timestamp)).get()
        if not old_messages: continue

        # batch delete
        old_messages = { key: None for key in old_messages.keys() }
        node.update(old_messages)
