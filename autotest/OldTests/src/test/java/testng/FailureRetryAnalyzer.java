package testng;

import java.lang.reflect.Method;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import testng.annotation.RetryTest;

public class FailureRetryAnalyzer implements IRetryAnalyzer {
  int currentRetry = 0;
  Logger LOGGER = LoggerFactory.getLogger(FailureRetryAnalyzer.class.getName());

  /**
   * Get the retry count from the method annotation. If the method is not annotated, get the retry
   * count from the class annotation.
   */
  public static int getRetryCount(Method method) {
    return Optional.ofNullable(method.getAnnotation(RetryTest.class))
        .or(() -> Optional.ofNullable(method.getDeclaringClass().getAnnotation(RetryTest.class)))
        .map(RetryTest::value)
        .orElse(0);
  }

  @Override
  public boolean retry(ITestResult result) {
    int maxRetryCount = this.getRetryCount(result.getMethod().getConstructorOrMethod().getMethod());

    if (++currentRetry > maxRetryCount) {
      return false;
    } else {
      logRetryInfo(result, maxRetryCount);
      return true;
    }
  }

  private void logRetryInfo(ITestResult result, int maxRetryCount) {
    // print failure stack trace
    LOGGER.debug("Stack trace of failure to be retried:", result.getThrowable());
    LOGGER.warn(
        String.format(
            "Running retry %d/%d for test '%s' in class %s",
            currentRetry,
            maxRetryCount,
            result.getName(),
            result.getTestClass().getRealClass().getSimpleName()));
  }
}
