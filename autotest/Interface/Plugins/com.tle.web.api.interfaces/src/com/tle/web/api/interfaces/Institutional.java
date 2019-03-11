package com.tle.web.api.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Institutional {
  enum Type {
    BOTH,
    INSTITUTIONAL,
    NON_INSTITUTIONAL
  }

  Type value() default Type.INSTITUTIONAL;
}
