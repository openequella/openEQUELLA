package com.tle.core.progress;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.tle.common.Check;
import com.tle.common.Utils;

public class ListProgressCallback implements ProgressCallback
{
	private static final long serialVersionUID = 1L;

	private int current = 0;
	private boolean finished;
	private final List<String> errors;
	private final List<String> traces;
	private MessageCallback messageCallback;
	private String forwardUrl;

	public ListProgressCallback()
	{
		errors = new ArrayList<String>();
		traces = new ArrayList<String>();
	}

	@Override
	public ProgressResponse getResponse()
	{
		ListJSONObject json = new ListJSONObject();
		json.setCurrent(getCurrent());
		json.setMessage(getMessage());
		json.setFinished(isFinished());
		json.setForwardUrl(getForwardUrl());
		json.setErrors(getErrors());
		json.setTraces(getTraces());
		return json;
	}

	public String getForwardUrl()
	{
		return forwardUrl;
	}

	public void setForwardUrl(String forwardUrl)
	{
		this.forwardUrl = forwardUrl;
	}

	public void incrementCurrent()
	{
		current++;
	}

	public int getCurrent()
	{
		return current;
	}

	public void addError(boolean critial, String message, Throwable t)
	{
		errors.add((critial ? "FATAL ERROR" : "NOTE") + ": " + message);
		String trace = null;
		if( t != null )
		{
			StringWriter writer = new StringWriter();
			PrintWriter pwriter = new PrintWriter(writer);
			t.printStackTrace(pwriter);
			pwriter.close();
			trace = writer.toString();
		}
		traces.add(trace);
	}

	public List<String> getErrors()
	{
		return errors;
	}

	@Override
	public synchronized void setFinished()
	{
		this.finished = true;
	}

	public List<String> getTraces()
	{
		return traces;
	}

	@Override
	public synchronized boolean isFinished()
	{
		return finished;
	}

	public String getMessage()
	{
		if( messageCallback != null )
		{
			return messageCallback.getMessage();
		}
		return null;
	}

	public interface MessageCallback
	{
		String getMessage();
	}

	public void setMessageCallback(MessageCallback messageCallback)
	{
		this.messageCallback = messageCallback;
	}

	public static class ListJSONObject implements ProgressResponse
	{
		int current;
		String message;
		boolean finished;
		String forwardUrl;
		List<String> errors;
		List<String> traces;

		public int getCurrent()
		{
			return current;
		}

		public void setCurrent(int current)
		{
			this.current = current;
		}

		public String getMessage()
		{
			return message;
		}

		public void setMessage(String message)
		{
			this.message = message;
		}

		@Override
		public boolean isFinished()
		{
			return finished;
		}

		public void setFinished(boolean finished)
		{
			this.finished = finished;
		}

		public String getForwardUrl()
		{
			return forwardUrl;
		}

		public void setForwardUrl(String forwardUrl)
		{
			this.forwardUrl = forwardUrl;
		}

		public List<String> getErrors()
		{
			return errors;
		}

		public void setErrors(List<String> errors)
		{
			this.errors = errors;
		}

		public List<String> getTraces()
		{
			return traces;
		}

		public void setTraces(List<String> traces)
		{
			this.traces = traces;
		}

		@Override
		public String getErrorMessage()
		{
			// summarise the errors
			if( !Check.isEmpty(errors) )
			{
				return Utils.join(errors.toArray(), ", "); //$NON-NLS-1$
			}
			return null;
		}
	}
}