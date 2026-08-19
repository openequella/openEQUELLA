package testng.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark tests that should only run with the new UI enabled. Can be applied to classes
 * or methods.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NewUIOnly {
  boolean value() default true;
}
