package com.artfulbits.benchmark;

import android.os.SystemClock;

import com.artfulbits.benchmark.junit.Sampling;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * jUnit tests for Meter class.
 *
 * @see <a href="http://www.vogella.com/tutorials/JUnit/article.html">Unit Testing with JUnit - Tutorial</a>
 */
@SuppressWarnings("PMD")
public class MeterTests {
  private static final String EMPTY_LOG = "";

  /* [ STATIC MEMBERS ] ============================================================================================ */

  private static Comparator<Object> sObjectComparator;
  private static Comparator<Method> sMethodComparator;

  /* [ INJECTIONS ] ================================================================================================ */
  @Rule
  public TestName mTestName = new TestName();

  /* [ MEMBERS ] =================================================================================================== */

  private Meter.Output mOutput;
  private transient Meter mAnotherInstance = null;

	/* [ IMPLEMENTATION & HELPERS ] ================================================================================== */

  //region Setup and TearDown
  @BeforeClass
  public static void setUpClass() {
    // sort by name
    sMethodComparator = new Comparator<Method>() {
      @Override
      public int compare(final Method lhs, final Method rhs) {
        return lhs.getName().compareTo(rhs.getName());
      }
    };

    // find by name
    sObjectComparator = new Comparator<Object>() {
      @Override
      public int compare(final Object lhs, final Object rhs) {
        if (lhs instanceof Method) {
          if (rhs instanceof Method) {
            return ((Method) lhs).getName().compareTo(((Method) rhs).getName());
          }

          return ((Method) lhs).getName().compareTo((String) rhs);
        }

        if (rhs instanceof Method) {
          return ((String) lhs).compareTo(((Method) rhs).getName());
        }

        return ((String) lhs).compareTo((String) rhs);
      }
    };
  }

  @Before
  public void setUp() {
    mAnotherInstance = null;

    mOutput = new Meter.Output() {
      private StringBuilder mLog = new StringBuilder(64 * 1024).append("\r\n");

      @Override
      public void log(final Level level, final String tag, final String msg) {
        mLog.append(level.toString().charAt(0)).append(" : ")
            .append(tag).append(" : ")
            .append(msg).append("\r\n");
      }

      @Override
      public String toString() {
        return mLog.toString();
      }
    };

    mOutput.log(Level.INFO, "→", mTestName.getMethodName());

    // reset to initial configuration
    Meter.getInstance().getConfig().reset();
  }

  @After
  public void tearDown() {
    mAnotherInstance = null;

    mOutput.log(Level.INFO, "←", mTestName.getMethodName());
    System.out.append(mOutput.toString());
  }

  @AfterClass
  public static void tearDownClass() {
    // do nothing for now
  }

  //endregion

  /* [ TESTS ] ===================================================================================================== */

  @Test
  public void test_00_Instance() {
    final Meter meter = Meter.getInstance();

    assertNotNull("Expected instance of Meter class", meter);
  }

  @Test
  public void test_01_Instance_Threads() {
    assertNull("Reference should be null", mAnotherInstance);

    final Meter meter = Meter.getInstance();
    assertNotNull("Instance for current thread expected", meter);

    final Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        mAnotherInstance = Meter.getInstance();

        // notify that instance extracted
        synchronized (meter) {
          meter.notifyAll();
        }
      }
    });

    try {
      t.start();

      synchronized (meter) {
        meter.wait();
      }
    } catch (final Throwable ignored) {
    }

    assertNotNull("Expected another instance of the Meter class", mAnotherInstance);
    assertNotEquals("Expected different instances for each thread.", meter, mAnotherInstance);
  }

  @Test
  public void test_02_Calibrate() {
    final Meter meter = Meter.getInstance();

    final Meter.Calibrate results = meter.calibrate();

    assertNotNull("Expected instance", results);

    final String values = "Nanos → Start/Beat/Log/Skip/Loop/Recap/UnLoop/End/Pop → " +
        results.Start + "/" + results.Beat + "/" + results.Log + "/" +
        results.Skip + "/" + results.Loop + "/" + results.Recap + "/" +
        results.UnLoop + "/" + results.End + "/" + results.Pop;

    // we expect NON ZERO results
    assertThat("Expected at least one Non Zero metric in " + values,
        new Long[]{results.Start, results.Beat, results.Log, results.Skip, results.Loop,
            results.Recap, results.UnLoop, results.End, results.Pop},
        hasItemInArray(greaterThan(0L)));

    // flaky! several methods may take no time at all (JVM can be extra fast)
    // assertNotEquals(values, 0L, results.Skip);
    // assertNotEquals(values, 0L, results.End);
    // assertNotEquals(values, 0L, results.Recap);

    // check that output contains some relative data
    mOutput.log(Level.INFO, "test-02", results.toString());

    final String result = String.format(Locale.US, "%.3f", Meter.toMillis(results.Start));
    assertTrue("Output should contains calibration results",
        mOutput.toString().contains(result));
  }

  @Test
  public void test_03_CommonRun() {
    final Meter meter = Meter.getInstance();
    assertNotNull("Expected instance.", meter);

    assertFalse("Meter instance is marked as tracking", meter.isTracking());

    final Meter.Calibrate timing = meter.calibrate();
    assertNotNull("Expected instance.", timing);

    assertFalse("Calibrate should not change tracking state", meter.isTracking());
    meter.start("→ Smoke test");

    // TODO: warm up java classes
    meter.skip("warming up");

    assertTrue("Should indicate tracking state", meter.isTracking());
    meter.beat("initialization");

    meter.loop(Sampling.ITERATIONS_L, EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      meter.recap();
    }
    meter.unloop(EMPTY_LOG);

    meter.finish("← Smoke test");
  }

  @Test
  public void test_04_NestedRun() throws NoSuchMethodException {
    final Meter meter = Meter.getInstance();
    assertNotNull("Expected instance.", meter);

    final Meter.Calibrate timing = meter.calibrate();
    assertNotNull("Expected instance.", timing);

    meter.start("→ Smoke test");

    SystemClock.sleep(100);
    meter.skip("warming up");

    SystemClock.sleep(150);
    meter.beat("initialization");

    final int id = meter.start("→→ Sub measurements");
    meter.loop(Sampling.ITERATIONS_L, EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method m = DummyPojo.class.getMethod("getName");
      meter.recap();
    }
    meter.unloop();
    meter.end("←← Sub measurements"); // No statistics printing!!!
    meter.pop();

    meter.beat("testing loop");

    SystemClock.sleep(100);

    meter.finish("← Smoke test");

    assertTrue("Nested measurement should have ID bigger zero", 0 < id);
  }

  @Test
  public void test_05_CustomOutput() {
    final Meter meter = Meter.getInstance();

    // register custom output provider
    meter.setOutput(mOutput);

    assertNotNull("Expected instance of the output provider", meter.getOutput());
    assertEquals("Expected same instance", mOutput, meter.getOutput());

    // try custom logger
    meter.start("→ Custom output");
    SystemClock.sleep(100);
    meter.finish("← Custom output");

    assertTrue("Expected not empty logs", mOutput.toString().length() > 0);
  }

  @Test
  public void test_05_ReflectionSpeed() throws NoSuchMethodException {
    final Meter meter = Meter.getInstance();

    // register custom output provider
    meter.setOutput(mOutput);

    meter.start("→ Reflection");

    final Method[] methods = DummyPojo.class.getMethods();
    meter.beat("extract all methods");

    Arrays.sort(methods, sMethodComparator);
    meter.beat("optimize search");

    meter.loop(EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method methodGet = DummyPojo.class.getMethod("getMemo");

      if (null != methodGet) {
        meter.recap();
      }
    }
    meter.unloop("single GET Method by name");

    meter.loop(EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method methodSet = DummyPojo.class.getMethod("setMemo", String.class);

      if (null != methodSet) {
        meter.recap();
      }
    }
    meter.unloop("single SET Method by name");

    meter.loop(EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method methodGet = DummyPojo.class.getMethod("getMemo");
      final Method methodSet = DummyPojo.class.getMethod("setMemo", String.class);

      if (null != methodGet && null != methodSet) {
        meter.recap();
      }
    }
    meter.unloop("single GET/SET Method by name");

    meter.loop(EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final int indexGet = Arrays.binarySearch(methods, "getMemo", sObjectComparator);
      final int indexSet = Arrays.binarySearch(methods, "setMemo", sObjectComparator);
      final Method methodGet = methods[indexGet];
      final Method methodSet = methods[indexSet];

      if (null != methodGet && null != methodSet) {
        meter.recap();
      }
    }
    meter.unloop("methods binary search");

    meter.finish("← Reflection");

    // always fail
    assertTrue("output should contain lines with our output tag", mOutput.toString().contains(meter.getConfig().OutputTag));
  }

  @Test
  public void test_06_AllConfigOptionsOn() {
    final String CustomTag = "test-06";
    final Meter meter = Meter.getInstance();

    // register custom output provider
    meter.setOutput(mOutput);

    // do configuration
    meter.getConfig().OutputTag = CustomTag;
    meter.getConfig().DoMethodsTrace = true;
    meter.getConfig().ShowAccumulatedTime = true;
    meter.getConfig().ShowLogMessage = true;
    meter.getConfig().ShowStepCostPercents = true;
    meter.getConfig().ShowStepCostTime = true;
    meter.getConfig().ShowStepsGrid = true;
    meter.getConfig().ShowSummary = true;
    meter.getConfig().ShowTableStart = true;
    meter.getConfig().UseSystemNanos = true;
    meter.getConfig().ShowAccumulatedTime = true;

    meter.start("→ Reflection");

    final Method[] methods = DummyPojo.class.getMethods();
    meter.beat("extract all methods");

    Arrays.sort(methods, sMethodComparator);
    meter.skip("optimize search");

    meter.loop(EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_XXL; i++) {
      final int indexGet = Arrays.binarySearch(methods, "getMemo", sObjectComparator);
      final int indexSet = Arrays.binarySearch(methods, "setMemo", sObjectComparator);
      final Method methodGet = methods[indexGet];
      final Method methodSet = methods[indexSet];

      if (null != methodGet && null != methodSet) {
        meter.recap();
      }
    }
    meter.unloop("cycled array");

    meter.finish("← Reflection");

    assertTrue("Output should contain our custom output tag", mOutput.toString().contains(CustomTag));
  }

  @Test
  public void test_07_AllConfigOptionsOff() {
    final String CustomTag = "test-07";
    final Meter meter = Meter.getInstance();

    // register custom output provider
    meter.setOutput(mOutput);

    // do configuration
    meter.getConfig().OutputTag = CustomTag;
    meter.getConfig().DoMethodsTrace = false;
    meter.getConfig().ShowAccumulatedTime = false;
    meter.getConfig().ShowLogMessage = false;
    meter.getConfig().ShowStepCostPercents = false;
    meter.getConfig().ShowStepCostTime = false;
    meter.getConfig().ShowStepsGrid = false;
    meter.getConfig().ShowSummary = false;
    meter.getConfig().ShowTableStart = false;
    meter.getConfig().UseSystemNanos = false;
    meter.getConfig().ShowAccumulatedTime = false;

    meter.start("→ Reflection");

    final Method[] methods = DummyPojo.class.getMethods();
    meter.beat("extract all methods");

    Arrays.sort(methods, sMethodComparator);
    meter.skip("optimize search");

    meter.loop(EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_XXL; i++) {
      final int indexGet = Arrays.binarySearch(methods, "getMemo", sObjectComparator);
      final int indexSet = Arrays.binarySearch(methods, "setMemo", sObjectComparator);
      final Method methodGet = methods[indexGet];
      final Method methodSet = methods[indexSet];

      if (null != methodGet && null != methodSet) {
        meter.recap();
      }
    }
    meter.unloop("cycled array");

    meter.finish("← Reflection");

    assertTrue("Output should contain our custom output tag", mOutput.toString().contains(CustomTag));
  }

  @Test
  public void test_08_DefaultOutputLogger() {
    final Meter meter = Meter.getInstance();

    meter.setOutput(null); // reset to default
    final Meter.Output output = meter.getOutput();

    assertNotNull("Expected default instance of the output", output);

    // custom
    output.log(Level.INFO, "test-07", "Info");
    output.log(Level.WARNING, "test-07", "Warning");
    output.log(Level.SEVERE, "test-07", "Sever");
    output.log(Level.FINE, "test-07", "Fine");

    // all other
    output.log(Level.CONFIG, "test-07", "Config");
    output.log(Level.FINER, "test-07", "Finer");
    output.log(Level.FINEST, "test-07", "Finest");
    output.log(Level.ALL, "test-07", "All");
    output.log(Level.OFF, "test-07", "Off");
  }

  @Test
  public void test_09_RecapWithLogMessage() throws Exception {
    final Meter meter = Meter.getInstance();

    // register custom output provider
    meter.setOutput(mOutput);

    meter.start("→ Reflection");

    meter.loop(EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method methodGet = DummyPojo.class.getMethod("getMemo");

      if (null != methodGet) {
        meter.recap(EMPTY_LOG);
      }
    }
    meter.unloop("single GET Method by name");

    meter.loop(EMPTY_LOG);
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method methodSet = DummyPojo.class.getMethod("setMemo", String.class);

      if (null != methodSet) {
        meter.recap("i=" + i);
      }
    }
    meter.unloop("single SET Method by name");

    meter.finish("← Reflection");

    final String search = "i=" + (Sampling.ITERATIONS_L - 1);
    assertTrue("In log should be last iteration counter", mOutput.toString().contains(search));
  }

  @Test
  public void test_10_NestedWithFinish() throws Exception {
    final Meter meter = Meter.getInstance();
    meter.setOutput(mOutput);

    meter.start("→ Smoke test");

    SystemClock.sleep(10);
    meter.skip("warming up");

    SystemClock.sleep(15);
    meter.beat("initialization");

    final int id;

    // just for highlighting the nested scope
    {
      id = meter.start("→→ Sub measurements");
      meter.loop(Sampling.ITERATIONS_L, EMPTY_LOG);
      for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
        final Method m = DummyPojo.class.getMethod("getName");
        meter.recap(null != m ? "got" : "miss");
      }
      meter.unloop();
      meter.finish(); // end() + stats() + pop()
    }

    meter.beat("testing loop");

    SystemClock.sleep(10);
    meter.finish("← Smoke test");

    assertTrue("Nested measurement should have ID bigger zero", 0 < id);
  }

  @Test
  public void test_11_Clear() throws Exception {
    final Meter m = Meter.getInstance();
    m.setOutput(mOutput);

    m.clear(); // clear call without tracking

    m.start("test-11");
    assertTrue("tracking is started", m.isTracking());

    m.start("nested #1");
    assertTrue("tracking is started", m.isTracking());

    m.start("nested #2");
    assertTrue("tracking is started", m.isTracking());

    m.start("nested #3");
    assertTrue("tracking is started", m.isTracking());

    m.start("nested #4");
    assertTrue("tracking is started", m.isTracking());

    m.clear();
    assertTrue("tracking initial state expected. Nested #4 stay on top.", m.isTracking());

    m.finish();

    assertFalse("tracking stopped", m.isTracking());
  }

  @Test
  public void test_12_CycledArray() throws Exception {

    int index = Meter.toArrayIndex(10, 10, 25, 100);
    assertEquals("Small array inside bigger one", 20, index);

    // initial array filling - 0,1,2,3,4
    int index0 = Meter.toArrayIndex(0, 0, 5, 5);
    assertEquals("Small array inside bigger one", 0, index0);
    int index1 = Meter.toArrayIndex(1, 0, 5, 5);
    assertEquals("Small array inside bigger one", 1, index1);
    int index2 = Meter.toArrayIndex(2, 0, 5, 5);
    assertEquals("Small array inside bigger one", 2, index2);
    int index3 = Meter.toArrayIndex(3, 0, 5, 5);
    assertEquals("Small array inside bigger one", 3, index3);
    int index4 = Meter.toArrayIndex(4, 0, 5, 5);
    assertEquals("Small array inside bigger one", 4, index4);

    // cycling started - 5,1,2,3,4
    index1 = Meter.toArrayIndex(0, 1, 5, 5);
    assertEquals("Cycling testing", 1, index1);
    index2 = Meter.toArrayIndex(1, 1, 5, 5);
    assertEquals("Cycling testing", 2, index2);
    index3 = Meter.toArrayIndex(2, 1, 5, 5);
    assertEquals("Cycling testing", 3, index3);
    index4 = Meter.toArrayIndex(3, 1, 5, 5);
    assertEquals("Cycling testing", 4, index4);
    index0 = Meter.toArrayIndex(4, 1, 5, 5);
    assertEquals("Cycling testing", 0, index0);

    // cycling started - 5,6,7,3,4
    index3 = Meter.toArrayIndex(0, 3, 5, 5);
    assertEquals("Cycling testing", 3, index3);
    index4 = Meter.toArrayIndex(1, 3, 5, 5);
    assertEquals("Cycling testing", 4, index4);
    index0 = Meter.toArrayIndex(2, 3, 5, 5);
    assertEquals("Cycling testing", 0, index0);
    index1 = Meter.toArrayIndex(3, 3, 5, 5);
    assertEquals("Cycling testing", 1, index1);
    index2 = Meter.toArrayIndex(4, 3, 5, 5);
    assertEquals("Cycling testing", 2, index2);

    // negative test
    try {
      Meter.toArrayIndex(0, 0, 10, 5);

      fail("wrong parameters forward to method");
    } catch (final Throwable ignored) {
    }
  }

  @Test
  public void test_13_WrongConfig() throws Exception {
    final String CustomTag = "test-13";
    final Meter meter = Meter.getInstance();

    // register custom output provider
    meter.setOutput(mOutput);

    // do configuration
    meter.getConfig().OutputTag = CustomTag;
    meter.getConfig().ShowTopNLongest = -1; // wrong value, should be from 1 and upper
    meter.getConfig().DoMethodsTrace = true;
    meter.getConfig().ShowAccumulatedTime = true;
    meter.getConfig().ShowLogMessage = true;
    meter.getConfig().ShowStepCostPercents = true;
    meter.getConfig().ShowStepCostTime = true;
    meter.getConfig().ShowStepsGrid = true;
    meter.getConfig().ShowSummary = true;
    meter.getConfig().ShowTableStart = true;
    meter.getConfig().UseSystemNanos = true;
    meter.getConfig().ShowAccumulatedTime = true;

    meter.start("→ Reflection");

    final Method[] methods = DummyPojo.class.getMethods();
    meter.beat("extract all methods");

    Arrays.sort(methods, sMethodComparator);
    meter.skip("optimize search");

    // empty loop
    meter.loop(EMPTY_LOG);
    meter.unloop(EMPTY_LOG);

    meter.finish("← Reflection");

    assertTrue("Output should contain our custom output tag", mOutput.toString().contains(CustomTag));
  }

  /* [ NESTED DECLARATIONS ] ======================================================================================= */

  public class DummyPojo {
    private String mId;
    private String mName;
    private String mExtra;

    private String mMemo;

    public String getId() {
      return mId;
    }

    /* package */ void setId(final String id) {
      mId = id;
    }

    public String getName() {
      return mName;
    }

    private void setName(final String name) {
      mName = name;
    }

    public String getExtra() {
      return mExtra;
    }

    protected void setExtra(final String extra) {
      mExtra = extra;
    }

    public String getMemo() {
      return mMemo;
    }

    public void setMemo(final String memo) {
      mMemo = memo;
    }
  }
}
