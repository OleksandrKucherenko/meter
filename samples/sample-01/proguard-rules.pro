# --------------------------------------------------------------------------------------
# REMOVE LOGGING AND BENCHMARKING
# http://proguard.sourceforge.net/manual/examples.html#logging
# --------------------------------------------------------------------------------------

#    public static boolean isLoggable(java.lang.String, int);
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-assumenosideeffects class com.artfulbits.benchmark.Meter { *; }
