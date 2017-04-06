/*
 * Created on 6/12/2005
 */
package com.tle.common.property.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.tle.common.property.ConfigurationProperties;

@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyDataList
{
	String key();

	Class<? extends ConfigurationProperties> type();
}
