package testng.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.scalatest.TagAnnotation;

/**
 * Annotation to mark tests that should only run with the new UI enabled. Can be applied to classes
 * or methods.
 *
 * <p>Serves both test frameworks. TestNG consumes it via {@link testng.TestAnnotationTransformer},
 * which disables annotated tests based on the {@code OLD_TEST_NEWUI} environment variable. The
 * {@link TagAnnotation} meta-annotation additionally makes it a ScalaTest suite-level tag (named by
 * this annotation's fully qualified class name), excluded via the same environment variable in
 * autotest/build.sbt.
 */
@TagAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface NewUIOnly {
  boolean value() default true;
}
