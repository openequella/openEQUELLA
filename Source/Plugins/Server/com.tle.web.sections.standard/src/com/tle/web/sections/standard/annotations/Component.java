package com.tle.web.sections.standard.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component
{
	boolean stateful() default true;

	String name() default "";

	boolean register() default true;

	String[] onlyForContext() default {};

	String[] ignoreForContext() default {};

	String[] contexts() default {};

	String parameter() default "";

	boolean supported() default false;
}
