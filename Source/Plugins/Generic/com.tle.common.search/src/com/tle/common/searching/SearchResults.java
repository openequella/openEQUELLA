package com.tle.common.searching;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nicholas Read
 */
public interface SearchResults<T> extends Serializable
{
	int getOffset();

	int getCount();

	int getAvailable();

	List<T> getResults();

	String getErrorMessage();

	void setErrorMessage(String errorMessage);
}
