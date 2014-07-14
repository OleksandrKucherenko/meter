# Custom Logging

For those cases designed special interface `Meter.Output` and two API methods: `meter().getOutput()` and 
`meter().setOutput(Output)`. By default class uses LOGCAT for output, but you can easily override this behavior.
 
Logcat output:
```java
static final Output logToLogcat = new Output() {
    @Override
    public void log(final Level level, final String tag, final String msg) {
      if (Level.INFO == level) {
        Log.i(tag, msg);
      } else if (Level.WARNING == level) {
        Log.w(tag, msg);
      } else if (Level.SEVERE == level) {
        Log.e(tag, msg);
      } else if (Level.FINE == level) {
        Log.d(tag, msg);
      } else {
        Log.v(tag, msg);
      }
    }
  }; 
``` 

Due to use of default Java Logger's interfaces, its very easy to create dumping of results to file or any 
other destination.