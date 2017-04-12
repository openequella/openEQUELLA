package com.tle.core.migration;

import com.tle.beans.DatabaseSchema;

public interface SchemaInfo
{

	DatabaseSchema getDatabaseSchema();

	String getTaskId();

	DatabaseSchema getDuplicateWith();

	String getErrorMessage();

	String getFinishedTaskId();

	boolean isChecking();

	boolean isInitial();

	long getUpdateTime();

	boolean isCanRetry();

	boolean isHasErrors();

	boolean isMigrationRequired();

	boolean isSystem();

	boolean isUp();

}
