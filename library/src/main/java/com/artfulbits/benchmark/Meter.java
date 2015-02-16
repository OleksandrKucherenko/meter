package com.artfulbits.benchmark;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Performance measurement class. Should be used for micro-benchmarking and
 * comparison of different implementations. Class implements very simple logic.
 */
@SuppressWarnings("UnusedDeclaration")
@SuppressLint("DefaultLocale")
public final class Meter {
    /* [ CONSTANTS ] ============================================================================================= */

  /** Flag. Tells Meter class that loop is with unknown number of iterations. */
  public static final int LOOP_ENDLESS = -1000;

  /** preallocate size for reduce performance impacts. */
  private static final int PREALLOCATE = 256;
  /** length of the delimiter line. */
  private static final int DELIMITER_LENGTH = 80;
  /** Delimiter for statistics output. */
  private static final String DELIMITER = new String(new char[DELIMITER_LENGTH]).replace("\0", "-");

	/* [ STATIC MEMBERS ] ========================================================================================== */

  /** Store instance of Meter per thread. */
  private final static WeakHashMap<Thread, Meter> sThreadsToMeter = new WeakHashMap<Thread, Meter>();

	/* [ MEMBERS ] ================================================================================================= */

  /** Current active measure. */
  private Measure mCurrent;
  /** reference on Log output instance. */
  private Output mLog;
  /** List of captured measures. */
  private final List<Measure> mMeasures = new ArrayList<Measure>(PREALLOCATE);
  /** Instance of the meter class configuration. */
  private final Config mConfig = new Config();
  /** Calibrate metrics. */
  private final Calibrate mCalibrate = new Calibrate();

	/* [ OPTIONS ] ================================================================================================= */

  /**
   * Gets config.
   *
   * @return the config
   */
  public Config getConfig() {
    return mConfig;
  }

  /**
   * Get instance of the output logger.
   *
   * @return Instance of the Output provider.
   */
  public Output getOutput() {
    if (null == mLog) {
      mLog = new Output() {
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
    }

    return mLog;
  }

  /**
   * Set custom output instance. Set <code>null</code> to reset to logcat output.
   *
   * @param out set another instance of the output provider, set {@code null} if you want to reset it to default.
   */
  public void setOutput(final Output out) {
    mLog = out;
  }

	/* [ STATIC METHODS ] ========================================================================================== */

  /**
   * Calibrate class, benchmark cost of execution for Meter class on a specific device. Allows to compute more
   * accurate results during statistics displaying.
   *
   * @returns instance of captured calibration metrics.
   */
  public Calibrate calibrate() {
    // DONE: measure each method execution time and store for future calculations

    long point1 = timestamp(), point2;

    start();
    mCalibrate.Start = (point2 = timestamp()) - point1;

    beat();
    mCalibrate.Beat = (point1 = timestamp()) - point2;

    log("calibrate");
    mCalibrate.Log = (point2 = timestamp()) - point1;

    skip();
    mCalibrate.Skip = (point1 = timestamp()) - point2;

    loop();
    mCalibrate.Loop = (point2 = timestamp()) - point1;

    recap();
    mCalibrate.Recap = (point1 = timestamp()) - point2;

    unloop();
    mCalibrate.UnLoop = (point2 = timestamp()) - point1;

    end();
    mCalibrate.End = (point1 = timestamp()) - point2;

    pop();
    mCalibrate.Pop = timestamp() - point1;

    return mCalibrate;
  }

  /** Method used for timestamp value extracting. */
  @SuppressLint("NewApi")
  private long timestamp() {
    final boolean apiLevel = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1);

    if (apiLevel && !getConfig().UseSystemNanos) {
      // alternative:
      return SystemClock.elapsedRealtimeNanos();
    }

    return System.nanoTime();
  }

  /**
   * Check is meter tracking something now.
   *
   * @return <code>true</code> - we are in tracking mode, otherwise <code>false</code>.
   */
  public boolean isTracking() {
    return (mCurrent != null);
  }

	/* [ MAIN API ] =============================================================================================== */

  /**
   * Start benchmarking. On each call a new benchmark measurement object created.
   *
   * @return unique id of the benchmark object.
   */
  private int start() {
    synchronized (mMeasures) {
      mMeasures.add(mCurrent = new Measure(this));
    }

    if (getConfig().DoMethodsTrace) {
      Debug.startMethodTracing(getConfig().MethodsTraceFilePath);
    }

    return mCurrent.Id;
  }

  /**
   * Start benchmarking with attached custom log message.
   *
   * @param log log message.
   * @return unique id of the benchmark object.
   */
  public int start(final String log) {
    int id = start();
    mCurrent.Logs.append(mCurrent.Position.get() - 1, log);
    return id;
  }

  /** Add/Include step beat interval into benchmarking report. */
  private void beat() {
    mCurrent.add(timestamp(), Bits.INCLUDE);
  }

  /**
   * Add/Include step beat interval into benchmarking report with custom log message.
   *
   * @param log log message.
   */
  public void beat(final String log) {
    beat();
    log(log);
  }

  /**
   * Assign a log string to a last step in benchmark run.
   *
   * @param log message to assign.
   */
  public void log(final String log) {
    mCurrent.Logs.append(mCurrent.Position.get() - 1, log);
  }

  /** Skip/Ignore/Exclude step interval from benchmarking. */
  public void skip() {
    mCurrent.add(timestamp(), Bits.EXCLUDE);
  }

  /**
   * Skip/Ignore/Exclude step interval from benchmarking with custom log message.
   *
   * @param log log message.
   */
  public void skip(final String log) {
    skip();
    log(log);
  }

  /** Start the loop tracking with unknown number of iterations. */
  public void loop() {
    loop(LOOP_ENDLESS);
  }

  /**
   * Start the loop tracking with unknown number of iterations with custom log message.
   *
   * @param log log message.
   */
  public void loop(final String log) {
    loop(LOOP_ENDLESS, log);
  }

  /**
   * Start the loop tracking.
   *
   * @param counter maximum number of iterations.
   */
  public void loop(final int counter) {
    final long time = timestamp();
    final long flags = Bits.INCLUDE | Bits.LOOP | Math.abs(counter);
    final long modifiers = (counter < 0) ? Bits.ENDLESS : 0;

    mCurrent.add(time, flags | modifiers);
  }

  /**
   * Start the loop tracking.
   *
   * @param counter maximum number of iterations.
   * @param log     log message.
   */
  public void loop(final int counter, final String log) {
    loop(counter);
    log(log);
  }

  /** Inside the loop store one iteration time. */
  public void recap() {
    mCurrent.add(timestamp(), Bits.INCLUDE | Bits.RECAP);
  }

  /**
   * Inside the loop store one iteration time.
   *
   * @param log log message.
   */
  public void recap(final String log) {
    recap();
    log(log);
  }

  /** Loop ends. Finalize benchmarking of the loop. */
  public void unloop() {
    mCurrent.add(timestamp(), Bits.UNLOOP);
  }

  /**
   * Loop ends. Finalize benchmarking of the loop.
   *
   * @param log log message.
   */
  public void unloop(final String log) {
    unloop();
    log(log);
  }

  /** End benchmarking, print statistics, prepare class for next run. */
  public void finish() {
    end();
    stats();
    pop();
  }

  /** End benchmarking. */
  public void end() {
    mCurrent.add(timestamp(), Bits.END);

    if (getConfig().DoMethodsTrace) {
      Debug.stopMethodTracing();
    }
  }

  /** Print captured statistics into logcat. */
  public void stats() {
    final Output log = getOutput();
    final Config config = getConfig();
    final int totalSteps = mCurrent.Position.get();
    final List<Step> steps = new ArrayList<Step>(totalSteps);
    long totalSkipped = 0;

    Step subStep;
    for (int i = 0; i < totalSteps; i++) {
      steps.add(subStep = new Step(config, mCurrent, i));
      totalSkipped += subStep.Skipped;
    }

    // dump all
    for (Step step : steps) {
      log.log((step.IsSkipped) ? Level.WARNING : Level.FINEST, config.OutputTag, step.toString());
    }

    // generate summary of tracking: top items by time, total time, total skipped time,
    if (getConfig().ShowSummary) {
      log.log(Level.FINEST, config.OutputTag, DELIMITER);

      // generate summary of tracking: top items by time, total time, total skipped time,
      log.log(Level.INFO, config.OutputTag, String.format(Locale.US, "final: %.3f ms%s, steps: %d",
          toMillis(mCurrent.total() - totalSkipped),
          (totalSkipped > 1000) ? String.format(" (-%.3f ms)", toMillis(totalSkipped)) : "",
          totalSteps));
    }

    final PriorityQueue<Step> pq = new PriorityQueue<Step>(totalSteps, Step.Comparator);
    pq.addAll(steps);

    // publish longest steps
    if (config.ShowTopNLongest > 0) {
      log.log(Level.FINEST, config.OutputTag, DELIMITER);

      for (int i = 1, len = Math.min(pq.size(), config.ShowTopNLongest); i <= len; i++) {
        final Step step = pq.poll();

        if (null != step && !step.IsSkipped) {
          log.log(Level.INFO, config.OutputTag, "top-" + i + ": " + step.toString());
        }
      }
    }

    log.log(Level.FINEST, config.OutputTag, DELIMITER);
  }

  /* [ UTILITIES ] =============================================================================================== */

  /**
   * Utility method. Converts array of long primitive types to collection of objects.
   *
   * @param timing array of long values.
   * @return Collection with converted values.
   */
  public static List<Object> toParams(long[] timing) {
    final List<Object> params = new ArrayList<Object>(Math.max(timing.length, PREALLOCATE));

    for (int j = 0, len = timing.length; j < len; j++) {
      params.add(timing[j]);
    }

    return params;
  }

  /**
   * Calculate percent value.
   *
   * @param value current value.
   * @param min   x-scale start point.
   * @param max   y-scale end point.
   * @return calculated percent value.
   */
  public static double percent(final long value, final long min, final long max) {
    final long point = value - min;
    final long end = max - min;

    return (point * 100.0 /* percentage scale */) / end;
  }

  /**
   * Convert nanoseconds to milliseconds with high accuracy.
   *
   * @param nanos nanoseconds to convert.
   * @return total milliseconds.
   */
  public static double toMillis(final long nanos) {
    return nanos / 1000.0 /* micros in 1 milli */ / 1000.0 /* nanos in 1 micro */;
  }

	/* [FINISH] ==================================================================================================== */

  /** Remove from measurements stack last done tracking. Method switches current Measure instance to next in stack. */
  public void pop() {
    synchronized (mMeasures) {
      mMeasures.remove(mCurrent.Id);
      mCurrent = (mMeasures.size() == 0) ? null : mMeasures.get(mMeasures.size() - 1);
    }
  }

  /**
   * End benchmarking, print statistics, prepare class for next run.
   *
   * @param log log message.
   */
  public void finish(final String log) {
    end(log);
    stats();
    pop();
  }

  /**
   * End benchmarking.
   *
   * @param log log message.
   */
  public void end(final String log) {
    end();
    log(log);
  }

  /** Cleanup the class. */
  public void clear() {
    synchronized (mMeasures) {
      mMeasures.clear();
    }
  }

	/* [ CONSTRUCTORS ] ============================================================================================ */

  /** Hidden constructor. */
  private Meter() {
    // do nothing, just keep the protocol of calls safe
  }

  /**
   * Get instance of Meter class for current thread.
   *
   * @return the instance of Meter for current thread.
   */
  public static Meter getInstance() {
    final Thread key = Thread.currentThread();

    // double check pattern
    if (!sThreadsToMeter.containsKey(key)) {
      synchronized (sThreadsToMeter) {
        if (!sThreadsToMeter.containsKey(key)) {
          sThreadsToMeter.put(key, new Meter());
        }
      }
    }

    return sThreadsToMeter.get(key);
  }

	/* [ NESTED DECLARATIONS ] ===================================================================================== */

  /**
   * Flags that we use for identifying measurement steps. First part of the long value is used
   * for custom data attaching, like: size of the array, index in array, etc.
   * Please do not use first 32 bits for any state flags.
   * Note: all fields declared in interface by default become "public final static".
   */
  private interface Bits {
    /** Time stamp included into statistics. */
    long INCLUDE = 0x000100000000L;
    /** Time stamp excluded into statistics. */
    long EXCLUDE = 0x000200000000L;
    /** Time stamp of Loop point creation. */
    long LOOP = 0x000400000000L;
    /** Time stamp of exiting from loop. */
    long UNLOOP = 0x000800000000L;
    /** Time stamp of loop iteration. */
    long RECAP = 0x001000000000L;
    /** The loop is endless. */
    long ENDLESS = 0x002000000000L;
    /** Time stamp start of statistics collecting. */
    long START = 0x100000000000L;
    /** Time stamp ends of statistics collecting. */
    long END = 0x200000000000L;
    /** Bits cleanup mask. */
    long MASK = 0xffffffffL;
  }

  /** Constants of time units calculated in Nanos. */
  public interface Nanos {
    /** One millisecond in nanos. */
    long ONE_MILLIS = 1L /*millis*/ * 1000L /*micros*/ * 1000L /*nanos*/;
    /** One second in nanos. */
    long ONE_SECOND = 1L /*sec*/ * 1000L /*millis*/ * ONE_MILLIS;
    /** One minute in nanos. */
    long ONE_MINUTE = 1L /*min*/ * 60L /*sec*/ * ONE_SECOND;
    /** One hour in nanos. */
    long ONE_HOUR = 1L /*hour*/ * 60L /*min*/ * ONE_MINUTE;
  }

  /** Output interface. */
  public interface Output {
    /**
     * Log measure message with defined Level and tag.
     *
     * @param level the level of logging. (Mostly used for coloring the output)
     * @param tag   the tag (tag of the output)
     * @param msg   the message to display.
     */
    void log(final Level level, final String tag, final String msg);
  }

  /** Statistics output and Tracking behavior configuration. */
  public final static class Config {
    /** Default path used for trace DUMPs. */
    public static final String getDefaultTraceFilePath() {
      // NOTE: for making Meter compatible with JVM tests - I expect exception from
      // runner side: "java.lang.RuntimeException: Method setUp in android.test.AndroidTestCase
      // not mocked. See https://sites.google.com/a/android.com/tools/tech-docs/unit-testing-support
      // for details."

      try {
        return Environment.getExternalStorageDirectory().getPath() + "/";
      } catch (final RuntimeException ignored) {
        return "/";
      }
    }

    /** Output tag for logs used by meter class. */
    public String OutputTag = "meter";
    /**
     * Default DUMP trace file name. Used only when {@link Config#DoMethodsTrace} is set to <code>true</code>. Field
     * initialized by android default dump file name.
     */
    public String MethodsTraceFilePath = getDefaultTraceFilePath() + "dmtrace.trace";
    /**
     * <code>true</code> - in addition do Android default methods tracing, otherwise <code>false</code>. {@link
     * Config#MethodsTraceFilePath}* defines the output file name for trace info.
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

  /** Calibration results holder. */
  public final static class Calibrate {
    /**
     * The Start.
     */
    public long Start;
    /**
     * The Beat.
     */
    public long Beat;
    /**
     * The Log.
     */
    public long Log;
    /**
     * The Skip.
     */
    public long Skip;
    /**
     * The Loop.
     */
    public long Loop;
    /**
     * The Recap.
     */
    public long Recap;
    /**
     * The Un loop.
     */
    public long UnLoop;
    /**
     * The End.
     */
    public long End;
    /**
     * The Pop.
     */
    public long Pop;

    @Override
    public String toString() {
      return String.format(Locale.US,
          "Calibrate [St/Be/Lg/Sk/Lo/Re/Un/En/Po]: %.3f/%.3f/%.3f/%.3f/%.3f/%.3f/%.3f/%.3f/%.3f/%.3f/%.3f ms",
          toMillis(Start), toMillis(Beat), toMillis(Log), toMillis(Skip),
          toMillis(Loop), toMillis(Recap), toMillis(UnLoop), toMillis(End), toMillis(Pop));
    }
  }

  /** Internal class for storing measurement statistics. */
  private final static class Measure {
    /** Unique identifier of the measurement instance. */
    public final int Id;
    /** Unique identifier of the thread which instantiate the class. */
    @SuppressWarnings("unused")
    public final long ThreadId;
    /** The start time of tracking. */
    public final long Start;
    /** Stored timestamp of each benchmarking call. */
    public final long[] Ranges = new long[PREALLOCATE];
    /** Stored flags for each corresponding timestamp in {@link #Ranges}. */
    public final long[] Flags = new long[PREALLOCATE];
    /** Current position in the benchmarking array {@link #Ranges}. */
    public final AtomicInteger Position = new AtomicInteger();
    /** Stack of loop's executed during benchmarking. */
    public final Queue<Integer> LoopsQueue = new ArrayDeque<Integer>(PREALLOCATE);
    /** Step index - to - Loop. */
    public final SparseArray<Loop> Loops = new SparseArray<Loop>();
    /** Step index - to - Log message. */
    public final SparseArray<String> Logs = new SparseArray<String>(PREALLOCATE);
    /** Reference on parent class instance. */
    public final Meter Parent;

		/* [ CONSTRUCTOR ] ============================================================================================ */

    /**
     * Instantiates a new Measure.
     *
     * @param meter the meter
     */
    public Measure(final Meter meter) {
      Parent = meter;

      synchronized (Parent.mMeasures) {
        Id = Parent.mMeasures.size();
      }

      ThreadId = Thread.currentThread().getId();
      Start = Parent.timestamp();

      add(Start, Bits.INCLUDE | Bits.START);
    }

    /** Get the last timestamp (maximum) of the measurement.  @return the long */
    public long theEnd() {
      final int totalTimes = Position.get();

      return Ranges[totalTimes - 1];
    }

    /** Get total time of measurement.  @return the long */
    public long total() {
      return theEnd() - Start;
    }

    /**
     * Add int.
     *
     * @param time  the time
     * @param flags the flags
     * @return the int
     */
    public int add(final long time, final long flags) {
      final boolean isIteration = (flags & Bits.RECAP) == Bits.RECAP;
      final boolean isLoopStart = (flags & Bits.LOOP) == Bits.LOOP;
      final boolean isLoopEnd = (flags & Bits.UNLOOP) == Bits.UNLOOP;

      final int index;

      if (isLoopStart) {
        final int counter = (int) (flags & Bits.MASK);
        final long onlyFlags = flags & (~Bits.MASK);
        index = addLoop(time, onlyFlags, counter);
      } else if (isLoopEnd) {
        // into first part of bits we store step index for easier loop begin identifying
        final Integer loopIndex = LoopsQueue.poll();
        final int order = (null == loopIndex) ? 0 : loopIndex;
        index = addStep(time, flags | order);
      } else if (isIteration) {
        index = addIteration(time, flags);
      } else {
        index = addStep(time, flags);
      }

      return index;
    }

    private int addStep(final long time, final long flags) {
      final int index = Position.getAndIncrement();

      Ranges[index] = time;
      Flags[index] = flags;

      return index;
    }

    private int addIteration(final long time, final long flags) {
      final int index = Position.get();
      final Integer loop;
      final Loop loopInfo;

      if (null != (loop = LoopsQueue.peek())) {
        if (null != (loopInfo = Loops.get(loop))) {
          loopInfo.add(time, flags);
        }
      }

      return index;
    }

    private int addLoop(final long time, final long flags, final int size) {
      final int index = addStep(time, flags);
      final boolean isEndless = (flags & Bits.ENDLESS) == Bits.ENDLESS;

      Loops.append(index, new Loop(time, (isEndless ? -1 : 1) * size));
      LoopsQueue.add(index);

      return index;
    }

    /**
     * Format string.
     *
     * @return the string
     */
    public String format() {
      final StringBuilder format = new StringBuilder(PREALLOCATE * 4);

      if (Parent.getConfig().ShowTableStart) {
        format.append("| ");
      }

      if (Parent.getConfig().ShowStepsGrid) {
        final String grid = new String(new char[Position.get()]).replace("\0", "%d | ");
        format.append(grid);
      }

      if (Parent.getConfig().ShowStepCostPercents) {
        format.append("%5.2f%% | ");
      }

      if (Parent.getConfig().ShowStepCostTime) {
        format.append("%8.3f ms | ");
      }

      if (Parent.getConfig().ShowAccumulatedTime) {
        format.append("%8.3f ms | ");
      }

      if (Parent.getConfig().ShowLogMessage) {
        format.append("%s");
      }

      return format.toString();
    }

    /**
     * Prepare log output part for a specific step.
     *
     * @param index - step position.
     * @return extracted log message for a step.
     */
    public String log(final int index) {
      final String log = Logs.get(index);

      final boolean isLoop = (Flags[index] & Bits.LOOP) == Bits.LOOP;
      final boolean isUnLoop = (Flags[index] & Bits.UNLOOP) == Bits.UNLOOP;
      final long custom = (Flags[index] & 0xffffffff);
      final String name = ((isLoop) ? "loop" : "step");

      // DONE: loop statistics should be displayed on the loop exit, not at the beginning
      final Loop loopInfo = (isUnLoop ? Loops.get((int) custom) : null);
      final String prefix = (isUnLoop && null != loopInfo) ? loopInfo.stats() : "";
      final String body = (TextUtils.isEmpty(log) ? name + " #" + index : log);
      final String suffix = "";

      return (prefix + body + suffix);
    }
  }

  /** Loops iterations tracking. */
  private final static class Loop {
    /** Timestamp's of each iteration. */
    public final long[] Iterations;
    /** Flags storage for each time stamp. */
    public final long[] Flags;
    /** Index of first element in Iterations array. */
    public int Position;
    /** Quantity of stored iterations. */
    public int Counter;
    /** Total number of captured iterations. */
    public int TotalCaptured;
    /** <code>true</code> indicates endless loop tracking, otherwise number of iterations is known. */
    @SuppressWarnings("unused")
    public final boolean IsEndless;
    /** Start time of the loop statistics . */
    public final long Start;

    /**
     * Create class with preallocated space for timestamp's on each iteration.
     *
     * @param time    the time
     * @param maxSize Number of expected iterations. If less than zero - class switch own mode to endless loops
     *                tracking.
     */
    public Loop(final long time, final int maxSize) {
      final int size = Math.abs(maxSize);

      Start = time;
      IsEndless = (maxSize < 0);
      Iterations = new long[size];
      Flags = new long[size];
    }

    /**
     * Add time stamp of a new iteration.
     *
     * @param time  time stamp.
     * @param flags time stamp flags.
     * @return index of iteration.
     */
    public int add(final long time, final long flags) {
      int index = Position;
      Iterations[index] = time;
      Flags[index] = flags;

      // cycled iteration pointer
      if (Iterations.length <= (++Position)) {
        Position = 0;
      }

      // if we in endless loop and overwriting old values
      if (Iterations.length < (++Counter)) {
        Counter = Iterations.length;
      }

      TotalCaptured++;

      return index;
    }

    /**
     * Calculate loop statistics.
     *
     * @return string with loop metrics.
     */
    public String stats() {
      final long loopStart = Start;
      final int endPoint = (Position - 1 < 0) ? Iterations.length - 1 : Position - 1;
      long loopTotal = Iterations[endPoint] - loopStart;

      long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
      long avg, total = 0, iteration, stepN = loopStart, stepM;

      for (int i = 0; i < Counter; i++) {
        int index = toArrayIndex(i, Position, Counter, Iterations.length);

        stepM = Iterations[index];
        iteration = stepM - stepN;

        min = Math.min(min, iteration);
        max = Math.max(max, iteration);
        total += iteration;

        stepN = stepM;
      }

      // NOTE: http://en.wikipedia.org/wiki/Measurement_uncertainty
      avg = (total - min - max) / Math.max(1, Counter - 2);

      // normalize output for empty Loops. make number good looking for output
      if (Counter == 0) {
        avg = min = max = loopTotal = 0;
      }

      // "avg: %.3fms min: %.3fms max: %.3fms total:%.3fms calls:%d / "
      // "avg/min/max/total: %.3f/%.3f/%.3f/%.3f ms - calls:%d / "

      return String.format(Locale.US, "avg/min/max/total: %.3f/%.3f/%.3f/%.3f ms - calls:%d / ",
          toMillis(avg), toMillis(min), toMillis(max), toMillis(loopTotal), TotalCaptured);
    }

    /**
     * Convert range [0..counter] to position in cycled array.
     *
     * @param index    index to convert.
     * @param position current cycled position in array.
     * @param count    quantity of elements stored in array.
     * @param length   length of the array.
     * @return converted index;
     */
    public static int toArrayIndex(final int index, final int position, final int count, final int length) {
      if (count > length) {
        throw new IllegalArgumentException();
      }

      // array is not filled and cycling is not enabled yet
      if (count < length) {
        return index;
      }
      // item is in a left range
      else if (index > length - position) {
        return index - (length - position);
      }
      // item is in a right range
      else {
        return position + index;
      }
    }
  }

  /** Statistics step. */
  private final static class Step {
    /** Compare steps by cost of execution. {@link #Total} */
    public final static Comparator<Step> Comparator = new Comparator<Step>() {
      /** {@inheritDoc} */
      @Override
      public int compare(final Step lhs, final Step rhs) {
        // output: left less right  == -1
        // output: left more right  == 1
        // output: left equal right == 0

        // first exclude "bad data" situations
        if (null == lhs || null == rhs) {
          return (null == lhs) ? -1 : 1;
        }

        // for skipped element shift there time to 0, that will make them last in list
        return -1 * Long.valueOf(lhs.Total - lhs.Skipped).compareTo(rhs.Total - rhs.Skipped);
      }
    };

    /** Is Step contains skipped data or not. */
    public final boolean IsSkipped;
    /** How much time to skip. Steps may exclude cost of methods call captured by calibration. */
    public final long Skipped;
    /** Step start time. */
    public final long Start;
    /** Step total time. */
    public final long Total;
    /** Accumulated total time. */
    public final long AccumulatedTotal;
    /** Cost of step in percents. */
    public final double CostPercents;
    /** Time grid row. */
    public final long[] Times;
    /** Format string for output. */
    public final String Format;
    /** Log message. */
    public final String Log;
    /** reference on configuration. */
    private final Config mConfig;

    /**
     * Instantiates a new statistics Step.
     *
     * @param config current configuration
     * @param m      current measure instance
     * @param index  the step index
     */
    public Step(final Config config, final Measure m, final int index) {
      mConfig = config;

      final long prevEndTime = m.Ranges[Math.max(0, index - 1)];

      Start = m.Ranges[index];

      // grid of steps
      Times = new long[m.Position.get()];
      Times[index] = Start;

      // calculate length of step
      Total = Start - prevEndTime;
      AccumulatedTotal = Start - m.Start;

      IsSkipped = ((m.Flags[index] & Bits.EXCLUDE) == Bits.EXCLUDE);
      Skipped = IsSkipped ? Total : 0;

      CostPercents = percent(Start, m.Start, m.theEnd()) - percent(prevEndTime, m.Start, m.theEnd());

      Format = m.format();
      Log = m.log(index);
    }

    /** Convert all statistics data to collection of parameters for string format.  @return the list */
    public List<Object> toParams() {
      final List<Object> params = (mConfig.ShowStepsGrid) ?
          Meter.toParams(Times) :
          new ArrayList<Object>(PREALLOCATE);

      if (mConfig.ShowStepCostPercents) {
        params.add(CostPercents);
      }

      if (mConfig.ShowStepCostTime) {
        params.add(toMillis(Total));
      }

      if (mConfig.ShowAccumulatedTime) {
        params.add(toMillis(AccumulatedTotal));
      }

      if (mConfig.ShowLogMessage) {
        params.add(Log);
      }

      return params;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return String.format(Locale.US, Format, toParams().toArray());
    }
  }
}
