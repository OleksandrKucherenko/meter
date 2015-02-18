package com.artfulbits.benchmark;

import com.artfulbits.benchmark.junit.Sampling;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
    assertNotEquals(0L, results.Start);
    assertNotEquals(0L, results.Beat);
    assertNotEquals(0L, results.Log);
    assertNotEquals(0L, results.Skip);
    assertNotEquals(0L, results.Loop);
    assertNotEquals(0L, results.Recap);
    assertNotEquals(0L, results.UnLoop);
    assertNotEquals(0L, results.End);
    assertNotEquals(0L, results.Pop);
  }

  @Test
  public void test_03_SmokeRun() {
    final Meter meter = Meter.getInstance();
    assertNotNull("Expected instance.", meter);

    final Meter.Calibrate timing = meter.calibrate();
    assertNotNull("Expected instance.", timing);

    meter.start("--> Smoke test");

    // TODO: warm up java classes
    meter.skip("warming up");

    meter.loop(Sampling.ITERATIONS_L, "");
    for (int i = 0; i < Sampling.ITERATIONS_L; i++) {

      meter.recap();
    }
    meter.unloop("");

    meter.end("<-- Smoke test");
  }
}
