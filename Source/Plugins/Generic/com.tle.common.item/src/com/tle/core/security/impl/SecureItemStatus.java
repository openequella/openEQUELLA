package com.tle.core.security.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tle.beans.item.ItemStatus;

/**
 * @author Nicholas Read
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureItemStatus
{
	ItemStatus[] value();

	boolean not() default false;
}
