package com.tle.core.security.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * @author Nicholas Read - All {@link SecureOnCall} privs must be at the same
 *         entity level (e.g Item).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureAllOnCall
{
	SecureOnCall[] value();
}
