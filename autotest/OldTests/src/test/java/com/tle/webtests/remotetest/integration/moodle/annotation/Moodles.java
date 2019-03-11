package com.tle.webtests.remotetest.integration.moodle.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Moodles {
  String[] value() default {};

  // lower order is run first
  int order() default 1000;
}
