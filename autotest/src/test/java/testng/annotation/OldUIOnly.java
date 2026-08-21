package testng.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.scalatest.TagAnnotation;

/**
 * Annotation to mark tests that should only run with the old UI enabled. The counterpart to {@link
 * NewUIOnly}; see that annotation for how the two frameworks consume it.
 */
@TagAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface OldUIOnly {
  boolean value() default true;
}
