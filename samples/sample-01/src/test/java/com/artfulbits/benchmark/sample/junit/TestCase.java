package com.artfulbits.benchmark.sample.junit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.test.AndroidTestCase;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * <span style="color:red;">Important!<br/>
 * All test methods inside class should have name prefix <b>test*</b>, otherwise jUnit will not find
 * them.</span><br/>
 * <br/>
 * References:
 * <ul>
 * <li><a
 * href="http://www.vogella.com/articles/AndroidTesting/article.html">http://www.vogella.com/articles/AndroidTesting
 * /article.html</a></li>
 * <li><a
 * href="http://mobile.tutsplus.com/tutorials/android/android-sdk-junit-testing/">http://mobile.tutsplus.com/tutorials
 * /android/android-sdk-junit-testing/</a></li>
 * <li><a
 * href="http://developer.android.com/tools/testing/testing_eclipse.html">http://developer.android.com/tools/testing
 * /testing_eclipse.html</a></li>
 * </ul>
 * Code coverage and CIS:
 * <ul>
 * <li><a
 * href="http://blog.cloudbees.com/2012/11/unit-test-results-code-coverage-and.html">http://blog.cloudbees.com/2012
 * /11/unit-test-results-code-coverage-and.html</a></li>
 * </ul>
 */
public abstract class TestCase
        extends AndroidTestCase {
  /* [ CONSTANTS ] ================================================================================================= */

  /** default class log tag. */
  public static final String TAG = "meter-tests";

  /** Name of the hidden API inside the AndroidTestCase class. */
  private static final String MethodGetTestContextName = "getTestContext";

	/* [ STATIC MEMBERS ] ============================================================================================ */

  /** Cache of reflection information. Used by {@link TestCase#getUnitTestsContext()}. */
  private static Method sGetTestContext;

	/* [ MEMBERS ] =================================================================================================== */

  /** Unit tests project assests. Not assets of the package we are testing. */
  protected AssetManager mAssets;

	/* [ STATIC METHODS ] ============================================================================================ */

  /** Disable exception raising during execution of AsyncTasks that are designed only for background execution. */
  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
  public static void fixMainThreadPolicy() {
    // Dirty fix: NetworkOnMainThreadException
    // http://stackoverflow.com/questions/12650921/quick-fix-for-networkonmainthreadexception
    ThreadPolicy tp = ThreadPolicy.LAX;
    StrictMode.setThreadPolicy(tp);
  }

	/* [ GETTER / SETTER METHODS ] =================================================================================== */

  /**
   * Extract Unit tests project context. This gives access to the embedded resources of unit test project.
   *
   * @return extracted instance of context, otherwise <code>null</code>.
   */
  public Context getUnitTestsContext() {
    Context context = null;

    try {
      // reflection is a heavy operation, cache it results
      if (null == sGetTestContext) {
        sGetTestContext = AndroidTestCase.class.getMethod(MethodGetTestContextName);
        sGetTestContext.setAccessible(true);
      }

      context = (Context) sGetTestContext.invoke(this);
    } catch (final Exception ignored) {
      Log.e(TAG, "Cannot extract unit tests project context.");
    }

    return context;
  }

	/* [ IMPLEMENTATION & HELPERS ] ================================================================================== */

  /** {@inheritDoc} */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // extract Units Tests Project assets manager
    final Context test = getUnitTestsContext();
    mAssets = test.getResources().getAssets();

    assertNotNull("Initialization of the Unit Tests Project assets manager failed.", mAssets);
  }

  /**
   * simple test that validates usage of base class. Rule it checks is:
   * 'in class should be declared at least one test method'. When initial
   * setup of tests is done set @Suppress annotation on this method.
   */
  @Override
  public void testAndroidTestCaseSetupProperly() {
    super.testAndroidTestCaseSetupProperly();

    validateClass();
  }

  /** Validate class and do not allow class without tests. */
  private void validateClass() {
    final Method[] methods = this.getClass().getMethods();
    int found = 0;

    for (Method m : methods) {
      if (m.getName().startsWith("test")) {
        found++;
      }
    }

    assertFalse("Please implement more than one test method.", found < 2);
  }
}
