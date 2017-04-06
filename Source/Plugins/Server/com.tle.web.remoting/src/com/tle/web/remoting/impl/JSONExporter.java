package com.tle.web.remoting.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;

import com.google.gson.Gson;
import com.tle.web.remoting.JSONService;

public class JSONExporter implements HttpRequestHandler
{
	private final Gson gson;
	private final JSONService service;

	public JSONExporter(Gson gson, JSONService service)
	{
		this.gson = gson;
		this.service = service;
	}

	@Override
	@SuppressWarnings("nls")
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		Object result = service.getResult(request);
		String json = gson.toJsonTree(result).toString();
		response.setContentType("text/json");
		response.setContentLengthLong(json.getBytes().length);
		response.getWriter().write(json);
	}
}
