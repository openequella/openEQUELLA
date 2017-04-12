package com.tle.web.sections.equella.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tle.web.sections.result.util.IconLabel.Icon;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PlugKey
{
	String value();

	boolean html() default true;

	boolean global() default false;

	Icon icon() default Icon.NONE;
}
