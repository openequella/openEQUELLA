package com.tle.core.services;

import java.io.Serializable;
import java.util.List;

import com.tle.common.Pair;

public interface TaskStatus
{
	int getDoneWork();

	int getMaxWork();

	String getErrorMessage();

	boolean isFinished();

	<T extends Serializable> List<T> getTaskLog();

	<T extends Serializable> Pair<Integer, List<T>> getTaskLog(int offset, int maxSize);

	String getInternalId();

	String getTitleKey();

	String getStatusKey();

	int getPercentage();

	<T> T consumeTransient(String key);

	<T> T getTaskSubStatus(String key);

	String getNodeIdRunning();
}
