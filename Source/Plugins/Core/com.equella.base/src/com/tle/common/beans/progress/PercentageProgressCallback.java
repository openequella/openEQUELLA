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
 * @author Nicholas Read
 */
public class PercentageProgressCallback implements ProgressCallback
{
	private static final long serialVersionUID = 1L;

	private long totalSize = 0;
	private long bytesRead = 0;
	private boolean finished;
	private String forwardUrl;
	private String errorMessage;

	public PercentageProgressCallback()
	{
		super();
	}

	public String getForwardUrl()
	{
		return forwardUrl;
	}

	public void setForwardUrl(String forwardUrl)
	{
		this.forwardUrl = forwardUrl;
	}

	public void incrementBytesRead(long amount)
	{
		bytesRead += amount;
	}

	public long getBytesRead()
	{
		return bytesRead;
	}

	@Override
	public synchronized void setFinished()
	{
		this.finished = true;
	}

	@Override
	public synchronized boolean isFinished()
	{
		return finished;
	}

	public long getTotalSize()
	{
		return totalSize;
	}

	public void setTotalSize(long totalSize)
	{
		this.totalSize = totalSize;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	@Override
	public ProgressResponse getResponse()
	{
		return new PercentageJSONObject(isFinished(), getPercent(), getForwardUrl(), errorMessage);
	}

	public static class PercentageJSONObject implements ProgressResponse
	{
		private final boolean finished;
		private final int percent;
		private final String forwardUrl;
		private final String errorMessage;

		public PercentageJSONObject(boolean finished, int percent, String forwardUrl, String errorMessage)
		{
			this.percent = percent;
			this.forwardUrl = forwardUrl;
			this.finished = finished;
			this.errorMessage = errorMessage;
		}

		public int getPercent()
		{
			return percent;
		}

		public String getForwardUrl()
		{
			return forwardUrl;
		}

		@Override
		public boolean isFinished()
		{
			return finished;
		}

		@Override
		public String getErrorMessage()
		{
			return errorMessage;
		}
	}

	public void setBytesRead(long bytesRead)
	{
		this.bytesRead = bytesRead;
	}

	public int getPercent()
	{
		return (int) ((bytesRead / (double) totalSize) * 100.0D);
	}
}
