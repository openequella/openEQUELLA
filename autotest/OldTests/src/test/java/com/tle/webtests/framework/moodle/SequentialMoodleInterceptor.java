package com.tle.webtests.framework.moodle;

import com.tle.common.Check;
import com.tle.webtests.remotetest.integration.moodle.AbstractSequentialMoodleTest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Sets up group dependencies so that each class will run one after the other but each moodle
 * instance (19, 20, 21, 22) can be run in parallel
 *
 * @author will
 */
public class SequentialMoodleInterceptor implements IAnnotationTransformer {
  private final Map<String, String> map;
  private Class<?> lastClass;

  public SequentialMoodleInterceptor() {
    super();
    map = new HashMap<String, String>();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (testMethod != null) {
      Class<?> clazz = testMethod.getDeclaringClass();
      if (AbstractSequentialMoodleTest.class.isAssignableFrom(clazz)) {
        annotation.setGroups(new String[] {clazz.getSimpleName()});
        annotation.setAlwaysRun(true);
        annotation.setSingleThreaded(true);
        if (lastClass == null) {
          map.put(clazz.getSimpleName(), "");
        } else if (!map.containsKey(clazz.getSimpleName())) {
          map.put(clazz.getSimpleName(), lastClass.getSimpleName());
        }

        String depends = map.get(clazz.getSimpleName());
        if (!Check.isEmpty(depends)) {
          annotation.setDependsOnGroups(new String[] {depends});
        }
        lastClass = clazz;
      }
    }
  }
}
