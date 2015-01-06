package com.artfulbits.benchmark.sample.espresso;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.artfulbits.benchmark.sample.MainActivity;
import com.artfulbits.benchmark.sample.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/** Default Android tests. */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginFillEspressoTest extends ActivityInstrumentationTestCase2<MainActivity> {
  private MainActivity mActivity;

  public LoginFillEspressoTest() {
    super(MainActivity.class);
  }

  @Override
  @Before
  protected void setUp() throws Exception {
    super.setUp();
    injectInstrumentation(InstrumentationRegistry.getInstrumentation());

    mActivity = getActivity();
  }

  @Test
  public void test_00_CreateActivity() {
    onView(withId(R.id.tv_login))
        .check(matches(withText(R.string.labelLogin)));

    onView(withId(R.id.et_login))
        .perform(typeText("developer"));
  }
}
