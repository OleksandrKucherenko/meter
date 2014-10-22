package com.artfulbits.benchmark;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

/** jUnit tests for Meter class. */
public class MeterTests extends TestCase {

  @Test
  public void test_00_Initialization() {
    final Meter meter = Meter.getInstance();

    // for the same thread we should always get the same instance of Meter
    Assert.assertEquals(meter, Meter.getInstance());
  }
}
