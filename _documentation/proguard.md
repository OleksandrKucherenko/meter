# Exclude Meter calls from final Binary

You can try to add into proguard configuration those lines:

```
# --------------------------------------------------------------------------------------
# REMOVE LOGGING AND BENCHMARKING
# http://proguard.sourceforge.net/manual/examples.html#logging
# --------------------------------------------------------------------------------------

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-assumenosideeffects class com.artfulbits.benchmark.Meter { *; }
```

It will remove calls to Meter class and all it methods from the final binary. That allows to keep benchmarking all the 
time enabled and will be automatically disabled during release. This is extremely good for release builds.