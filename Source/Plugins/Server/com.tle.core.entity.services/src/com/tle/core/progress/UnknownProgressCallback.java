package com.tle.core.progress;

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
