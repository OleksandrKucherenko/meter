package com.artfulbits.benchmark.sample;

import com.artfulbits.benchmark.Meter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Basic unit tests of the library.
 *
 * @see <a href="http://www.vogella.com/tutorials/JUnit/article.html">Unit Testing with JUnit - Tutorial</a>
 */
public class MeterTests {

  private transient Meter mAnotherThreadInstance = null;

  @Before
  public void setUp() {
    mAnotherThreadInstance = null;
  }

  @After
  public void tearDown() {
    mAnotherThreadInstance = null;
  }

  @Test
  public void test_00_Instance() {
    final Meter meter = Meter.getInstance();

    assertNotNull("Expected instance of Meter class", meter);
  }

  @Test
  public void test_01_Instance_Threads() {
    assertNull("Reference should be null", mAnotherThreadInstance);

    final Meter meter = Meter.getInstance();
    assertNotNull("Instance for current thread expected", meter);

    final Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        mAnotherThreadInstance = Meter.getInstance();

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

    assertNotNull("Expected another instance of the Meter class", mAnotherThreadInstance);
    assertNotEquals("Expected different instances for each thread.", meter, mAnotherThreadInstance);
  }
}