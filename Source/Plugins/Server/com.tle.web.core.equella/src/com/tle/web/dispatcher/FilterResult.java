package com.tle.web.dispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FilterResult
{
	public static final FilterResult FILTER_CONTINUE = new FilterResult();

	private String forwardUrl;
	private HttpServletRequest filteredRequest;
	private HttpServletResponse filteredResponse;
	private WebFilterCallback callback;
	private boolean stop;

	public FilterResult()
	{
		// nothing
	}

	public FilterResult(String path)
	{
		this.forwardUrl = path;
	}

	public FilterResult(WebFilterCallback callback)
	{
		this.callback = callback;
	}

	public FilterResult(HttpServletResponse response)
	{
		this.filteredResponse = response;
	}

	public FilterResult(HttpServletRequest request)
	{
		this.filteredRequest = request;
	}

	public FilterResult(String path, HttpServletRequest request)
	{
		this.forwardUrl = path;
		this.filteredRequest = request;
	}

	public FilterResult(boolean stop)
	{
		this.stop = stop;
	}

	public WebFilterCallback getCallback()
	{
		return callback;
	}

	public void setCallback(WebFilterCallback callback)
	{
		this.callback = callback;
	}

	public String getForwardUrl()
	{
		return forwardUrl;
	}

	public void setForwardUrl(String forwardUrl)
	{
		this.forwardUrl = forwardUrl;
	}

	public HttpServletRequest getFilteredRequest()
	{
		return filteredRequest;
	}

	public void setFilteredRequest(HttpServletRequest filteredRequest)
	{
		this.filteredRequest = filteredRequest;
	}

	public HttpServletResponse getFilteredResponse()
	{
		return filteredResponse;
	}

	public void setFilteredResponse(HttpServletResponse filteredResponse)
	{
		this.filteredResponse = filteredResponse;
	}

	public boolean isStop()
	{
		return stop;
	}

	public void setStop(boolean stop)
	{
		this.stop = stop;
	}
}
