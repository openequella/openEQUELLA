package com.tle.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ListCallback
{
	private int current = 0;
	private boolean finished;
	private final List<String> errors;
	private final List<String> traces;
	private MessageCallback messageCallback;
	private String forwardUrl;

	public String getForwardUrl()
	{
		return forwardUrl;
	}

	public void setForwardUrl(String forwardUrl)
	{
		this.forwardUrl = forwardUrl;
	}

	public ListCallback()
	{
		errors = new ArrayList<String>();
		traces = new ArrayList<String>();
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

	public synchronized void setFinished()
	{
		this.finished = true;
	}

	public List<String> getTraces()
	{
		return traces;
	}

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
}
