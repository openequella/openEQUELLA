package com.tle.web.sections.ajax.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AjaxFactory
{
	String name() default "";
}
