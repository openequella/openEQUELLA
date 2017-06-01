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

package com.tle.upgrademanager.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.tle.upgrademanager.JSONService;

@SuppressWarnings("nls")
public class HttpExchangeUtils
{
	private static final Map<String, String> EXT_TO_MIME = new HashMap<String, String>();
	private static final String DEFAULT_MIME = "text/plain";

	private static final String CONTENT_TYPE_HEADER = "Content-type";
	private static final String MULTIPART = "multipart/";

	private static final String METHOD_POST = "POST";

	static
	{
		EXT_TO_MIME.put("html", "text/html");
		EXT_TO_MIME.put("css", "text/css");
		EXT_TO_MIME.put("js", "text/javascript");

		EXT_TO_MIME.put("gif", "image/gif");
		EXT_TO_MIME.put("png", "image/png");
		EXT_TO_MIME.put("jpeg", "image/jpeg");
	}

	public static String getContentTypeForUri(String uri)
	{
		String mime = null;

		int i = uri.lastIndexOf('.');
		if( i >= 0 && i < uri.length() - 1 )
		{
			mime = EXT_TO_MIME.get(uri.substring(i + 1));
		}

		return mime != null ? mime : DEFAULT_MIME;
	}

	public static String getContentType(HttpExchange exchange)
	{
		return exchange.getRequestHeaders().getFirst(CONTENT_TYPE_HEADER);
	}

	public static void setContentType(HttpExchange exchange, String contentType)
	{
		exchange.getResponseHeaders().set(CONTENT_TYPE_HEADER, contentType);
	}

	public static void setNoCaching(HttpExchange exchange)
	{
		exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store");
	}

	public static boolean isPost(HttpExchange exchange)
	{
		return exchange.getRequestMethod().equals(METHOD_POST);
	}

	public static boolean isMultipartContent(HttpExchange exchange)
	{
		if( !"post".equals(exchange.getRequestMethod().toLowerCase()) )
		{
			return false;
		}

		String contentType = getContentType(exchange);
		if( contentType != null && contentType.toLowerCase().startsWith(MULTIPART) )
		{
			return true;
		}
		return false;
	}

	public static void respondRedirect(HttpExchange exchange, String whereTo) throws IOException
	{
		String hostPort = exchange.getRequestHeaders().get("HOST").get(0);
		exchange.getResponseHeaders().set("Location", "http://" + hostPort + whereTo);
		exchange.sendResponseHeaders(303, -1);
		exchange.close();
	}

	public static void respondFileNotFound(HttpExchange exchange) throws IOException
	{
		StringWriter writer = new StringWriter();
		writeCommonStart(writer, "404: File not found");
		writer.write("<h1>File not found</h1>");
		writer.write(exchange.getRequestURI().toString());
		writeCommonEnd(writer);

		respondHtmlMessage(exchange, 404, writer.toString());
	}

	public static void respondApplicationError(HttpExchange exchange, Throwable th) throws IOException
	{
		th.printStackTrace();

		StringWriter writer = new StringWriter();
		writeCommonStart(writer, "500: Application Error");
		writer.write("<pre>");
		th.printStackTrace(new PrintWriter(writer));
		writer.write("</pre>");
		writeCommonEnd(writer);

		respondHtmlMessage(exchange, 500, writer.toString());
	}

	private static void writeCommonStart(StringWriter writer, String title)
	{
		writer.write("<html><head><title>");
		writer.write(title);
		writer.write("</title></head><body>");
	}

	private static void writeCommonEnd(StringWriter writer)
	{
		writer.write("</body></html>");
	}

	public static void respondHtmlMessage(HttpExchange exchange, int code, String html) throws IOException
	{
		setNoCaching(exchange);
		respondWithContent(exchange, code, "text/html", html);
	}

	public static void respondJSONMessage(HttpExchange exchange, int code, Object json) throws IOException
	{
		setNoCaching(exchange);
		respondWithContent(exchange, code, "text/json", JSONService.toString(json));
	}

	public static void respondWithContent(HttpExchange exchange, int code, String mimeType, String content)
		throws IOException
	{
		setContentType(exchange, mimeType);

		byte[] bytes = content.getBytes();

		exchange.sendResponseHeaders(code, bytes.length);
		exchange.getResponseBody().write(bytes);
		exchange.close();
	}

}
