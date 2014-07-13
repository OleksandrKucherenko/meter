package com.artfulbits.benchmark.sample.junit;

import android.util.Log;

import com.artfulbits.benchmark.Meter;


/**
 * Class designed first of all for capturing performance metrics of the tests.<br/>
 * <br/>
 * Logic:<br/>
 * <ul>
 * <li>Inside the test please use {@link Meter} class API for performance metrics capturing. Meter class automatically
 * started in {@link PerformanceTests#setUp()} and finalized in {@link PerformanceTests#tearDown()}.</li>
 * <li>Typical approach is to call {@link Meter#beat(String)} inside the test case.</li>
 * <li>{@link PerformanceTests#warmUp()} - abstract methods for executing warmUp logic. WarmUp excluded from
 * measurement.</li>
 * </ul>
 */
public abstract class PerformanceTests extends TestCase {

  protected final Meter mMeter = Meter.getInstance();

  /** {@inheritDoc} */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    Log.d(TAG, "SetUp - " + getName());

    configMeter();
    meter().start("--> " + getName());

    warmUp();

    meter().skip("warm up classes");
  }

  /** Meter class configuration adaptation. */
  protected void configMeter() {
    meter().getConfig().OutputTag = TAG;
    meter().calibrate();
  }

  /** {@inheritDoc} */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    meter().finish("<-- " + getName());

    Log.d(TAG, "TearDown - " + getName());
  }

  /** Warm up the test before executing it body. */
  protected abstract void warmUp();

  /** Get instance of the benchmark tool. */
  public Meter meter() {
    return mMeter;
  }
}
