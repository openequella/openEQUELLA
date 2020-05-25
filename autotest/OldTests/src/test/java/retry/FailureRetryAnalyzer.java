package retry;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class FailureRetryAnalyzer implements IRetryAnalyzer {
  int currentRetry = 0;
  Logger LOGGER = Logger.getLogger(FailureRetryAnalyzer.class.getName());

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
    LOGGER.warning("Test with retry analyser failed.\n\n");
    // print failure stack trace
    LOGGER.log(Level.WARNING, result.getThrowable().getMessage(), result.getThrowable());
    LOGGER.warning(
        String.format(
            "Running retry %d/%d for test '%s' in class %s",
            currentRetry,
            maxRetryCount,
            result.getName(),
            result.getTestClass().getRealClass().getSimpleName()));
  }
}
