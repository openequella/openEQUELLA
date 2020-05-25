package retry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class TestAnnotationTransformer implements IAnnotationTransformer {

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (testMethod == null) return;
    MaxRetryCount maxRetryCount = testMethod.getAnnotation(MaxRetryCount.class);
    if (maxRetryCount == null) return;
    annotation.setRetryAnalyzer(FailureRetryAnalyzer.class);
  }
}
