# Runtime configuration

For `Meter` instance configuration should be used method `getConfig()` which returns instance of Configuration. 

```java
/** Statistics output and Tracking behavior configuration. */
public final static class Config {
  /** Output tag for logs used by meter class. */
  public String OutputTag = "meter";
  /**
   * Default DUMP trace file name. Used only when {@link Config#DoMethodsTrace} is set to <code>true</code>. Field
   * initialized by android default dump file name.
   */
  public String MethodsTraceFilePath = DEFAULT_TRACE_PATH_PREFIX + "dmtrace.trace";
  /**
   * <code>true</code> - in addition do Android default methods tracing, otherwise <code>false</code>. {@link
   * Config#MethodsTraceFilePath} defines the output file name for trace info.
   */
  public boolean DoMethodsTrace;
  /** <code>true</code> - show steps grid in output, otherwise <code>false</code>. */
  public boolean ShowStepsGrid;
  /** <code>true</code> - show accumulated time column, otherwise <code>false</code>. */
  public boolean ShowAccumulatedTime;
  /** <code>true</code> - show cost in percents column, otherwise <code>false</code>. */
  public boolean ShowStepCostPercents = true;
  /** <code>true</code> - show step cost time column, otherwise <code>false</code>. */
  public boolean ShowStepCostTime = true;
  /** <code>true</code> - show log message column, otherwise <code>false</code>. */
  public boolean ShowLogMessage = true;
  /** <code>true</code> - show after tracking summary, otherwise <code>false</code>. */
  public boolean ShowSummary = true;
  /** <code>true</code> - place column starter symbol "| " on each row start, otherwise <code>false</code>. */
  public boolean ShowTableStart = true;
  /** Show in statistics summary list of longest steps. Define the Number of steps to show. */
  public int ShowTopNLongest = 5;
  /** True - use {@link System#nanoTime()}, otherwise use {@link SystemClock#elapsedRealtimeNanos()}. */
  public boolean UseSystemNanos = true;
}
```

Several options inside the config influence on runtime behavior, several influence only on output and can be changed 
mostly in any place before the `finish()` or `stats()` methods call. 

# TraceView integration
This is Android SDK profiler. Meter is integrated with it. Integration is very simple - it just enables and disables 
profiling on Dalvik layer for a specific micro-benchmark scope.
 
To enable profiling you should call `meter().getConfig().DoMethodTrace = true;`. This configuration change should be 
executed before the `meter().start()` method call.  

To customize profiler output file name use: 
```java
meter().getConfig().MethodsTraceFilePath = Meter.DEFAULT_TRACE_PATH_PREFIX + "custom-name.trace";
```

# Output Filtering

![Android Studio logcat Filtering](images/logcat-filtering.png)