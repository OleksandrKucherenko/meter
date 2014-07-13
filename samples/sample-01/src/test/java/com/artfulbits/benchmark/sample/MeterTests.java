package com.artfulbits.benchmark.sample;

import android.os.SystemClock;

import com.artfulbits.benchmark.sample.junit.PerformanceTests;

/** Performance library tests. */
public class MeterTests extends PerformanceTests {
  @Override
  protected void warmUp() {
    // do nothing
  }

  public void test_00_Meter() {

    // sleep 1 seconds
    SystemClock.sleep(1 * 1000);

    meter().beat("test_00_Meter");
  }
}
