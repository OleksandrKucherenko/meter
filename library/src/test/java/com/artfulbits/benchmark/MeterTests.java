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
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * jUnit tests for Meter class.
 *
 * @see <a href="http://www.vogella.com/tutorials/JUnit/article.html">Unit Testing with JUnit - Tutorial</a>
 */
public class MeterTests {
  /* [ STATIC MEMBERS ] ============================================================================================ */

  private static Meter sAnother = null;
  private static Comparator<Object> sObjectComparator;
  private static Comparator<Method> sMethodComparator;

  @Rule
  public TestName mTestName = new TestName();
  private Meter.Output mOutput;

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
    sAnother = null;

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
  }

  @After
  public void tearDown() {
    sAnother = null;

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
    assertNull("Reference should be null", sAnother);

    final Meter meter = Meter.getInstance();
    assertNotNull("Instance for current thread expected", meter);

    final Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        sAnother = Meter.getInstance();

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

    assertNotNull("Expected another instance of the Meter class", sAnother);
    assertNotEquals("Expected different instances for each thread.", meter, sAnother);
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

    assertNotEquals(values, 0L, results.Start);
    assertNotEquals(values, 0L, results.Beat);
    assertNotEquals(values, 0L, results.Log);
    assertNotEquals(values, 0L, results.Loop);
    assertNotEquals(values, 0L, results.UnLoop);
    assertNotEquals(values, 0L, results.Pop);

    // flaky! skip method may take no time at all
    // assertNotEquals(values, 0L, results.Skip);
    // assertNotEquals(values, 0L, results.End);
    // assertNotEquals(values, 0L, results.Recap);
  }

  @Test
  public void test_03_CommonRun() {
    final Meter meter = Meter.getInstance();
    assertNotNull("Expected instance.", meter);

    final Meter.Calibrate timing = meter.calibrate();
    assertNotNull("Expected instance.", timing);

    meter.start("→ Smoke test");

    // TODO: warm up java classes
    meter.skip("warming up");

    // TODO:
    meter.beat("initialization");

    meter.loop(Sampling.ITERATIONS_L, "");
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      meter.recap();
    }
    meter.unloop("");

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
    meter.loop(Sampling.ITERATIONS_L, "");
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
  public void test_05_CustomOutput() throws Exception {
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
  public void test_05_ReflectionSpeed() throws Exception {
    final Meter meter = Meter.getInstance();

    // register custom output provider
    meter.setOutput(mOutput);

    meter.start("→ Reflection");

    final Method[] methods = DummyPojo.class.getMethods();
    meter.beat("extract all methods");

    Arrays.sort(methods, sMethodComparator);
    meter.beat("optimize search");

    meter.loop("");
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method methodGet = DummyPojo.class.getMethod("getMemo");

      if (null != methodGet) {
        meter.recap();
      }
    }
    meter.unloop("single GET Method by name");

    meter.loop("");
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method methodSet = DummyPojo.class.getMethod("setMemo", String.class);

      if (null != methodSet) {
        meter.recap();
      }
    }
    meter.unloop("single SET Method by name");

    meter.loop("");
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {
      final Method methodGet = DummyPojo.class.getMethod("getMemo");
      final Method methodSet = DummyPojo.class.getMethod("setMemo", String.class);

      if (null != methodGet && null != methodSet) {
        meter.recap();
      }
    }
    meter.unloop("single GET/SET Method by name");

    meter.loop("");
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
    //assertTrue(mOutput.toString(), false);
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
