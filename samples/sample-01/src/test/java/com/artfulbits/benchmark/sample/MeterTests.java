package com.artfulbits.benchmark.sample;

import android.os.SystemClock;

import com.artfulbits.benchmark.sample.junit.PerformanceTests;
import com.artfulbits.benchmark.sample.junit.Sampling;

/** Performance library tests. */
public class MeterTests extends PerformanceTests {
  @Override
  protected void warmUp() {
    // do nothing

    meter().getConfig().ShowStepsGrid = true;
    meter().getConfig().ShowAccumulatedTime = true;
  }

  public void test_00_Meter() {

    // sleep 1 seconds
    SystemClock.sleep(1 * 1000);

    meter().beat("test_00_Meter");
  }

  public void test_01_TryCatchVsPlain(){

    final String value = "1234567890";
    final int iterations = Sampling.ITERATIONS_XL;
    final StringBuilder sb = new StringBuilder(iterations * value.length() + 1000);

    for( int i = 0; i < iterations; i++ ) {
      try {
        long lValue = Long.parseLong(value);

        sb.append(lValue);
      }
      catch(final NumberFormatException ignored){
        // do nothing
      }
    }
    meter().skip("preparations");

    sb.setLength(0);
    meter().loop(iterations, "try/catch after warm up");
    for( int i = 0; i < iterations; i++ ) {
      try {
        long lValue = Long.parseLong(value);

        sb.append(lValue);
      }
      catch(final NumberFormatException ignored){
        // do nothing
      } finally {
        meter().recap();
      }
    }
    meter().unloop("done.");

    sb.setLength(0);
    meter().loop(iterations, "No try/catch");
    for( int i = 0; i < iterations; i++ ) {
      long lValue = Long.parseLong(value);

      sb.append(lValue);
      meter().recap();
    }
    meter().unloop("done.");

    meter().getConfig().ShowTopNLongest = 2;
  }
}
