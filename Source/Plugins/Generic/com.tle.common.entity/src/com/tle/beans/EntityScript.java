package com.tle.beans;

import java.io.Serializable;

import com.tle.beans.entity.BaseEntity;

public interface EntityScript<T extends BaseEntity> extends Serializable
{
	T getEntity();

	void setEntity(T entity);

	String getScript();

	void setScript(String script);
}