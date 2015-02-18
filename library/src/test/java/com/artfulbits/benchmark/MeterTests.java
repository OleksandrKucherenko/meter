package com.artfulbits.benchmark;

import android.os.SystemClock;

import com.artfulbits.benchmark.junit.Sampling;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

  private static Meter sAnother = null;

  @Before
  public void setUp() {
    sAnother = null;
  }

  @After
  public void tearDown() {
    sAnother = null;
  }

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
    assertNotEquals(values, 0L, results.Skip);
    assertNotEquals(values, 0L, results.Loop);
    assertNotEquals(values, 0L, results.Recap);
    assertNotEquals(values, 0L, results.UnLoop);
    assertNotEquals(values, 0L, results.End);
    assertNotEquals(values, 0L, results.Pop);
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
  public void test_04_NestedRun() {
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
}
