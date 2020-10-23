package testng;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import testng.annotation.RetryTest;
import testng.annotation.SkipTest;

public class TestAnnotationTransformer implements IAnnotationTransformer {
  private static final String OLD_TEST_NEWUI = "OLD_TEST_NEWUI";

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (testMethod == null) return;

    RetryTest maxRetryCount = testMethod.getAnnotation(RetryTest.class);
    if (maxRetryCount != null && maxRetryCount.value() > 1) {
      annotation.setRetryAnalyzer(FailureRetryAnalyzer.class);
    }

    SkipTest skipTest = testMethod.getAnnotation(SkipTest.class);
    // Read the configuration of using new UI or not from environment variable.
    boolean isNewUIEnabled = Boolean.parseBoolean(System.getProperty(OLD_TEST_NEWUI));
    // Skip tests that should not run against Old UI when CI is running in Old UI.
    if (skipTest != null && skipTest.skipOldUI() && !isNewUIEnabled) {
      annotation.setEnabled(false);
    }
  }
}
