package testng;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import testng.annotation.NewUIOnly;
import testng.annotation.OldUIOnly;

public class TestAnnotationTransformer implements IAnnotationTransformer {
  private static final String OLD_TEST_NEWUI = "OLD_TEST_NEWUI";

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (testMethod == null) return;

    checkRetryAnnotation(annotation, testMethod);
    checkSkipTestAnnotation(annotation, testMethod);
  }

  // Check if a method is annotated with 'RetryTest'
  private void checkRetryAnnotation(ITestAnnotation annotation, Method testMethod) {
    if (FailureRetryAnalyzer.getRetryCount(testMethod) > 1) {
      annotation.setRetryAnalyzer(FailureRetryAnalyzer.class);
    }
  }

  // Check if a method is annotated with 'NewUIOnly'
  private void checkSkipTestAnnotation(ITestAnnotation annotation, Method testMethod) {
    NewUIOnly newUIOnly = testMethod.getAnnotation(NewUIOnly.class);
    OldUIOnly oldUIOnly = testMethod.getAnnotation(OldUIOnly.class);
    // Read the configuration of using new UI or not from environment variable.
    boolean isNewUIEnabled = Boolean.parseBoolean(System.getenv(OLD_TEST_NEWUI));
    // Skip tests that should not run against Old UI when CI is running in Old UI
    // and also skip tests that should not run against New UI when CI is running in New UI.
    if ((newUIOnly != null && newUIOnly.value() && !isNewUIEnabled)
        || (oldUIOnly != null && oldUIOnly.value() && isNewUIEnabled)) {
      annotation.setEnabled(false);
    }
  }
}
