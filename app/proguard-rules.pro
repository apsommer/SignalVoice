# keep line numbers for crashlytics
-keepattributes SourceFile,LineNumberTable

# keep kotlin coroutine internals readable in stack traces
-keepattributes *Annotation*

# preserve enum values/names
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}