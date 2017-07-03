package com.tle.common.beans.progress;

import java.io.Serializable;

public interface ProgressCallback extends Serializable
{
	ProgressResponse getResponse();

	void setFinished();

	boolean isFinished();

	interface ProgressResponse
	{
		boolean isFinished();

		String getErrorMessage();
	}
}
