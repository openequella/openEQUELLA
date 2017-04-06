package com.tle.blackboard.buildingblock.lti;

import static com.tle.blackboard.common.BbUtil.COURSE_ID;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import blackboard.base.FormattedText;
import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.platform.blti.BasicLTIConstants;
import blackboard.platform.blti.BasicLTILauncher;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManagerFactory;

import com.google.common.base.Throwables;
import com.tle.blackboard.buildingblock.Configuration;
import com.tle.blackboard.buildingblock.data.WrappedContent;
import com.tle.blackboard.buildingblock.data.WrappedUser;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.PathUtils;

/**
 * Blackboard doesn't sign query string params!! This needs to be implemented in
 * such a way so that if BB fixes the issue we aren't double fixing the problem
 * when BB sort their shizzle out.
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
// @NonNullByDefault
public class FixedBasicLtiLauncher extends BasicLTILauncher
{
	// Sigh, BasicLTILauncher._parameters is private in SP11
	private final Map<String, String> params2;
	private String ourUrl;

	private FixedBasicLtiLauncher(String fullUrl, String clientId, String clientSecret, /*
																						 * @
																						 * Nullable
																						 */
		String resourceLinkId)
	{
		super(fullUrl, clientId, clientSecret, resourceLinkId);

		ourUrl = fullUrl;

		// Ugh, _parameters is protected in SP13 but *private* in SP11
		try
		{
			final Field paramsField = BasicLTILauncher.class.getDeclaredField("_parameters");
			paramsField.setAccessible(true);
			params2 = (Map<String, String>) paramsField.get(this);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}

		// Put the query string values into the parameters (does this double
		// post?)
		final int question = fullUrl.indexOf('?');
		if( question > 0 )
		{
			final String qs = fullUrl.substring(question + 1);

			final String[] params = qs.split("&");
			for( String param : params )
			{
				final String[] nameVal = param.split("=");
				if( nameVal.length == 2 )
				{
					params2.put(decodePercent(nameVal[0]), decodePercent(nameVal[1]));
				}
				else
				{
					params2.put(decodePercent(nameVal[0]), null);
				}
			}
		}
	}

	private String decodePercent(/* @Nullable */String s)
	{
		if( s == null )
		{
			return "";
		}
		try
		{
			return URLDecoder.decode(s, "UTF-8");
		}
		catch( UnsupportedEncodingException wow )
		{
			throw new RuntimeException(wow.getMessage(), wow);
		}
	}

	@Override
	public FixedBasicLtiLauncher addResourceLinkInformation(
	/* @Nullable */String title, /* @Nullable */
		String description)
	{
		// If a description contains a newline or a br tag then Blackboard
		// doesn't sign correctly.
		// We don't use it anyway, so stop sending it.
		final String usedDescription = "";
		// If a title contains certain special characters, such as &#2018; ,
		// then Blackboard doesn't sign correctly.
		// QTI is the only functionality using the title, and should default to
		// the 'local' Equella title for the item when this title is empty.
		final String usedTitle = "";
		return (FixedBasicLtiLauncher) super.addResourceLinkInformation(usedTitle, usedDescription);
	}

	public FixedBasicLtiLauncher addGradingInformation(HttpServletRequest request, WrappedContent content)
	{
		WrappedUser user = null;
		try
		{
			user = WrappedUser.getUser(request);
			final Id courseId = BbUtil.getCourseId(request.getParameter(COURSE_ID));
			try
			{
				final CourseMembership courseMembership = user.getCourseMembership(courseId);
				return (FixedBasicLtiLauncher) super.addGradingInformation(content.getContent(), courseMembership);
			}
			catch( KeyNotFoundException knf )
			{
				// No enrollment, don't add anything
				return this;
			}
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
		finally
		{
			if( user != null )
			{
				user.clearContext();
			}
		}
	}

	@Override
	public BasicLTILauncher addCurrentUserInformation(boolean includeRoles, boolean includeName, boolean includeEmail,
		IdTypeToSend idTypeToSend)
	{
		// add username
		Context ctx = ContextManagerFactory.getInstance().getContext();
		User user = ctx.getUser();
		addPostData("lis_person_sourcedid", user.getUserName());

		return super.addCurrentUserInformation(includeRoles, includeName, includeEmail, idTypeToSend);
	}

	@Override
	public BasicLTILauncher addUserInformation(User user, CourseMembership membership, boolean includeRoles,
		boolean includeName, boolean includeEmail, IdTypeToSend idTypeToSend)
	{
		addPostData("lis_person_sourcedid", user.getUserName());
		return super.addUserInformation(user, membership, includeRoles, includeName, includeEmail, idTypeToSend);
	}

	public FixedBasicLtiLauncher addPostData(String key, /* @Nullable */String value)
	{
		params2.put(key, value);
		return this;
	}

	public FixedBasicLtiLauncher addReturnUrl(String returnUrl)
	{
		final Map<String, String> launchPresentation = new HashMap<String, String>();
		launchPresentation.put(BasicLTIConstants.PARAM_LAUNCH_PRESENTATION_DOCUMENT_TARGET,
			BasicLTIConstants.PARAM_LAUNCH_PRESENTATION_TARGET_FRAME);
		launchPresentation.put(BasicLTIConstants.PARAM_LAUNCH_PRESENTATION_RETURN_URL, returnUrl);
		return (FixedBasicLtiLauncher) super.addLaunchPresentationInformation(launchPresentation);
	}

	@Override
	public void launch(HttpServletRequest request, HttpServletResponse response, boolean useSplashScreen,
		FormattedText splashScreenMessage)
	{
		try
		{
			Map<String, String> params = prepareParameters();

			request.setAttribute("toolUrl", ourUrl);
			request.setAttribute("bltiParams", params);
			request.setAttribute("useSplashScreen", false);
			request.setAttribute("splashScreenMessage", null);
			request.setAttribute("shouldOpenInFrame", true);
			request.getServletContext().getContext("/webapps/blackboard").getRequestDispatcher("/blti/launch.jsp")
				.forward(request, response);
		}
		catch( RuntimeException re )
		{
			throw re;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public static FixedBasicLtiLauncher newLauncher(Configuration configuration, String urlPath,
	/* @Nullable */String resourceLinkId)
	{
		final boolean appendSlash = urlPath.endsWith("/");
		final String url = PathUtils.urlPath(configuration.getEquellaUrl(), urlPath);
		final String clientId = configuration.getOauthClientId();
		final String clientSecret = configuration.getOauthClientSecret();

		if( url == null || clientId == null || clientSecret == null )
		{
			throw new RuntimeException("One of URL, clientId or clientSecret not configured: " + url + ", " + clientId
				+ ", " + clientSecret);
		}

		final FixedBasicLtiLauncher launcher = new FixedBasicLtiLauncher(appendSlash ? url + "/" : url, clientId,
			clientSecret, resourceLinkId);
		return launcher;
	}
}
