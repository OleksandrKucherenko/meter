package com.artfulbits.benchmark.sample.junit;

/** Traditional Sampling constants. */
public interface Sampling {
  /** Default: 10. Small. */
  int ITERATIONS_S = 10;
  /** Default: 100. Medium. */
  int ITERATIONS_M = ITERATIONS_S * 10;
  /** Default: 1000. Large. */
  int ITERATIONS_L = ITERATIONS_M * 10;
  /** Default: 10000. Extra large. */
  int ITERATIONS_XL = ITERATIONS_L * 10;
  /** Default: 100000. Extra extra large. */
  int ITERATIONS_XXL = ITERATIONS_XL * 10;
  /** 1 second in millis resolution. */
  long MILLIS = 1000;
  /** 5 seconds on millis resolution. */
  long SECONDS_5 = 5 * MILLIS;
  /** 10 seconds on millis resolution. */
  long SECONDS_10 = 10 * MILLIS;
  /** 15 seconds in millis resolution. */
  long SECONDS_15 = 15 * MILLIS;
}
