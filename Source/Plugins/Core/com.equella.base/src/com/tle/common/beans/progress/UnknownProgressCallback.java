/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.beans.progress;

/**
 * @author Aaron
 */
public class UnknownProgressCallback implements ProgressCallback
{
	private static final long serialVersionUID = 1L;

	private String errorMessage;
	private boolean finished;
	private String forwardUrl;

	@Override
	public ProgressResponse getResponse()
	{
		return new UnknownProgressJSONObject(errorMessage, isFinished(), forwardUrl);
	}

	@Override
	public synchronized void setFinished()
	{
		finished = true;
	}

	@Override
	public synchronized boolean isFinished()
	{
		return finished;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getForwardUrl()
	{
		return forwardUrl;
	}

	public void setForwardUrl(String forwardUrl)
	{
		this.forwardUrl = forwardUrl;
	}

	public static class UnknownProgressJSONObject implements ProgressResponse
	{
		private final String errorMessage;
		private final boolean finished;
		private final String forwardUrl;

		protected UnknownProgressJSONObject(String errorMessage, boolean finished, String forwardUrl)
		{
			this.errorMessage = errorMessage;
			this.finished = finished;
			this.forwardUrl = forwardUrl;
		}

		@Override
		public String getErrorMessage()
		{
			return errorMessage;
		}

		@Override
		public boolean isFinished()
		{
			return finished;
		}

		public String getForwardUrl()
		{
			return forwardUrl;
		}
	}
}
