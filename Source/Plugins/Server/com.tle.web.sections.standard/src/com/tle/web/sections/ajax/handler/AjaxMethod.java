package com.tle.web.sections.ajax.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.tle.web.sections.events.SectionEvent;

@Retention(RetentionPolicy.RUNTIME)
public @interface AjaxMethod
{
	String name() default "";

	String factoryName() default "";

	int priority() default SectionEvent.PRIORITY_EVENTS;
}
