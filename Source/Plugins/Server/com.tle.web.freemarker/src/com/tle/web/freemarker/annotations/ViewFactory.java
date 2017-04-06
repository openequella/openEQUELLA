package com.tle.web.freemarker.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ViewFactory
{
	String name() default "";

	boolean fixed() default true;

	boolean optional() default false;
}
