package retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class FailureRetryAnalyzer implements IRetryAnalyzer {
  int currentRetry = 0;
  Logger LOGGER = LoggerFactory.getLogger(FailureRetryAnalyzer.class.getName());

  @Override
  public boolean retry(ITestResult result) {
    MaxRetryCount failureRetryCount =
        result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(MaxRetryCount.class);
    int maxRetryCount = (failureRetryCount == null) ? 0 : failureRetryCount.value();
    if (++currentRetry > maxRetryCount) {
      currentRetry = 0;
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
