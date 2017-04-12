package com.tle.web.pss.viewer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemService;
import com.tle.web.sections.equella.annotation.PlugKey;

@Bind
@Singleton
@SuppressWarnings("nls")
public class PearsonScormServicesReturnUrlServlet extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(PearsonScormServicesReturnUrlServlet.class);

	@PlugKey("error.launching")
	private static String KEY_ERROR_LAUNCHING;

	@Inject
	private ItemService itemService;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		try
		{
			final String ltiMsg = request.getParameter("lti_msg");
			final String ltiLog = request.getParameter("lti_log");
			final String ltiErrorMsg = request.getParameter("lti_errormsg");
			final String ltiErrorLog = request.getParameter("lti_errorlog");

			final Item item = getItem(request);
			String itemName = CurrentLocale.get(item.getName());
			StringBuilder htmlOutput = new StringBuilder("<!DOCTYPE html><h3>").append(
				CurrentLocale.get(KEY_ERROR_LAUNCHING, itemName, item.getUuid(), item.getVersion())).append("</h3>");

			if( !Check.isEmpty(ltiErrorLog) )
			{
				LOGGER.warn(ltiErrorLog);
			}
			if( !Check.isEmpty(ltiLog) )
			{
				LOGGER.info(ltiLog);
			}

			if( !Check.isEmpty(ltiErrorMsg) )
			{
				htmlOutput.append("<br><br>").append(ltiErrorMsg);
			}
			if( !Check.isEmpty(ltiMsg) )
			{
				htmlOutput.append("<br><br>").append(ltiErrorMsg);
			}
			ServletOutputStream outputStream = response.getOutputStream();
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			outputStream.write(htmlOutput.toString().getBytes(Charset.forName("UTF-8")));
			outputStream.close();
		}
		catch( Exception e )
		{
			LOGGER.error("Error in return URL servlet", e);
		}
	}

	private Item getItem(HttpServletRequest request) throws Exception
	{
		final String originalUrl = request.getPathInfo();
		final List<String> partList = new ArrayList<String>();
		for( String part : originalUrl.split("/") )
		{
			if( !Check.isEmpty(part) )
			{
				partList.add(part);
			}
		}
		if( partList.size() < 2 )
		{
			throw new ParseException("Invalid URL missing UUID and/or version: " + originalUrl, 1);
		}
		final String uuid = partList.get(0);
		final int version = Integer.parseInt(partList.get(1));
		final Item item = itemService.get(new ItemId(uuid, version));
		return item;
	}
}
