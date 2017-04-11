package com.tle.common.filters;

public interface Filter<T>
{
	boolean include(T t);
}