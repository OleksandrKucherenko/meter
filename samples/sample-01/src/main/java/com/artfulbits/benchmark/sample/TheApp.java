package com.artfulbits.benchmark.sample;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * Instance of application that automatically enables Multi Dex support if specific
 * BuildConfig constant is set to true.
 */
public class TheApp extends Application {
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);

    // enabled multi-dex
    if (BuildConfig.USED_MULTIDEX) {
      try {
        // emulate call: MultiDex.install(this);

        final Class info = Class.forName("android.support.multidex.MultiDex");
        final Method method = info.getMethod("install", Context.class);

        method.invoke(this);
      } catch (final Throwable ignored) {
        // expected: ClassNotFoundException | NoSuchMethodException |
        //    InvocationTargetException | IllegalAccessException
      }
    }
  }
}
