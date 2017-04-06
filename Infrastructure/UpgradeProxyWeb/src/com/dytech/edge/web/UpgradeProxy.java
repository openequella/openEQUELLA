package com.dytech.edge.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UpgradeProxy extends HttpServlet {

	private String proxyPath = null;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String path = request.getServletPath();
		String qs = request.getQueryString();
		if (qs != null)
		{
			path += "?" + qs;
		}

		URLConnection conn = new URL("http://" + proxyPath + path).openConnection ();

		int i = 0;
		boolean done = false;
		while (!done)
		{
			String name = conn.getHeaderFieldKey (i);
			String value = conn.getHeaderField (i);
			if (value == null) // test value rather than name as Javadocs suggest that name may be null for 0 on some implementations
			{
				done = true;
			}
			else if (name != null) // see comment above
			{
				response.setHeader(name, value);
			}
			i += 1;
		}

		OutputStream os = response.getOutputStream ();
		InputStream is = conn.getInputStream ();

		byte [] buffer = new byte [8192];
		int read = is.read (buffer);
		while (read >= 0)
		{
			os.write (buffer, 0, read);
			read = is.read (buffer);
		}
		os.flush ();
		os.close ();
		is.close ();
	}

	public void init() throws ServletException {
		this.proxyPath = this.getInitParameter("proxy.path");
	}

}
