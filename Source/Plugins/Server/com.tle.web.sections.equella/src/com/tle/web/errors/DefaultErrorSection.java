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

package com.tle.web.errors;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.common.LockedException;
import com.dytech.edge.common.valuebean.UserBean;
import com.dytech.edge.exceptions.BadRequestException;
import com.dytech.edge.exceptions.DRMException;
import com.dytech.edge.exceptions.WebException;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.UrlService;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class DefaultErrorSection extends AbstractErrorSection<DefaultErrorSection.DefaultErrorModel>
{
	private static Log LOGGER = LogFactory.getLog(DefaultErrorSection.class);

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private UserService userService;
	@Inject
	private UrlService urlService;
	@Inject
	private MimeTypeService mimeTypeService;
	@PlugKey("errors.part.actions.goback")
	@Component
	private Button action;

	@ResourceHelper
	private static PluginResourceHelper urlHelper;

	@Override
	public SectionResult renderErrorHtml(DefaultErrorModel model, RenderEventContext context) throws Exception
	{
		context.preRender(JQueryCore.PRERENDER);

		Pair<Integer, String> result = handleException(context, model, model.getException());
		context.getResponse().setStatus(result.getFirst());

		String titleKey = urlHelper.key("errors.title." + result.getSecond());
		model.setTitleKey(titleKey);
		Decorations.getDecorations(context).setTitle(new KeyLabel(titleKey));

		action.setClickHandler(context, new OverrideHandler(new ScriptStatement("history.back();")));

		return viewFactory.createTemplateResult("errors/defaulterror.ftl", context);
	}

	protected Pair<Integer, String> handleException(SectionInfo info, DefaultErrorModel model, Throwable ex)
	{
		HttpServletRequest request = info.getRequest();
		String requestURI = (String) request.getAttribute("javax.servlet.error.request_uri");
		if( requestURI == null )
		{
			StringBuffer requestURL = request.getRequestURL();
			String queryString = request.getQueryString();
			if( queryString == null )
			{
				requestURI = requestURL.toString();
			}
			else
			{
				requestURI = requestURL.append('?').append(queryString).toString();
			}
		}
		int status = 500;
		String message = null;
		String titleKey = "defaulterror";

		final ErrorPart urlPart = createUrlPart(requestURI);
		final String url = urlPart.getText();
		if( ex instanceof WebException )
		{
			WebException webex = (WebException) ex;
			model.addPart(createUserPart());
			model.addPart(urlPart);
			model.addPart(createExceptionDescriptionPart(ex, null));
			return new Pair<Integer, String>(webex.getCode(), mapErrorCode(webex.getCode()));
		}

		if( ex instanceof com.dytech.edge.exceptions.NotFoundException )
		{
			if( ((com.dytech.edge.exceptions.NotFoundException) ex).isFromRequest() )
			{
				// cool, standard 404
				return generic404(model, ex, requestURI);
			}
		}
		else if( ex instanceof LockedException )
		{
			LockedException ile = (LockedException) ex;
			model.addPart(createLockedPart(ile));
			model.addPart(urlPart);
			return new Pair<Integer, String>(200, "itemlocked");
		}
		else if( ex instanceof AccessDeniedException || ex instanceof DRMException )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Access denied to user: "
					+ userService.convertUserStateToString(CurrentUser.getUserState()));
			}

			// TODO: do we need this any more???
			String filename = request.getParameter("filename");
			if( !Check.isEmpty(filename) )
			{
				// if mimetype is image, forward to 'denied' image
				String mime = mimeTypeService.getMimeTypeForFilename(filename);
				if( mime.startsWith("image") )
				{
					info.forwardToUrl(urlService.institutionalise("images/denied.png"));
					return null;
				}
			}

			model.addPart(createAccessDeniedPart(ex));
			model.addPart(urlPart);
			model.addPart(createUserPart());
			return new Pair<Integer, String>(403, "accessdenied");
		}
		else if( ex instanceof BadRequestException )
		{
			BadRequestException bad = (BadRequestException) ex;
			status = bad.getCode();
			message = resolve("part.badrequest.message", bad.getParameter());
			titleKey = "badrequest";
		}

		if( model.isNoLog() )
		{
			//Nothing
		}
		else if( model.isNoStack() )
		{
			if( model.isWarnOnly() )
			{
				LOGGER.warn("Warning at " + url + " :" + ex.getMessage());
			}
			else
			{
				LOGGER.error("Error at " + url + " :" + ex.getMessage());
			}
		}
		else
		{
			if( model.isWarnOnly() )
			{
				LOGGER.warn("Warning at " + url + " :", ex);
			}
			else
			{
				LOGGER.error("Error at " + url + " :", ex);
			}
		}

		model.addPart(createUserPart());
		model.addPart(urlPart);
		model.addPart(createExceptionDescriptionPart(ex, message));
		return new Pair<Integer, String>(status, titleKey);
	}

	private String mapErrorCode(int code)
	{
		switch( code )
		{
			case 403:
				return "accessdenied";
			default:
				return "defaulterror";
		}
	}

	private Pair<Integer, String> generic404(DefaultErrorModel model, Throwable t, String requestUri)
	{
		model.addPart(createNotFoundPart(t));
		model.addPart(createUrlPart(requestUri));
		return new Pair<Integer, String>(404, "notfound");
	}

	private ErrorPart createUserPart()
	{
		try
		{
			String value = null;
			final UserState userState = CurrentUser.getUserState();
			if( userState.isGuest() )
			{
				value = resolve("part.user.notloggedin");
			}
			else
			{
				value = resolve("part.user.loggedinas", userState.getUserBean(), userState.getIpAddress());
			}

			return new ErrorPart("user", resolve("part.user.title"), value);
		}
		catch( Exception e )
		{
			return new ErrorPart("user", resolve("part.user.title"), resolve("part.user.unknown"));
		}
	}

	private ErrorPart createExceptionDescriptionPart(Throwable ex, String messageOverride)
	{
		String message = (messageOverride == null ? ex == null ? "" : ex.getMessage() : messageOverride);
		if( Check.isEmpty(message) && ex != null )
		{
			message = ex.getClass().toString();
		}
		return new ErrorPart("description", resolve("part.exceptiondescription.title"), message);
	}

	private ErrorPart createNotFoundPart(Throwable ex)
	{
		return new ErrorPart("notfound", resolve("part.notfound.title"), ex.getMessage());
	}

	private ErrorPart createAccessDeniedPart(Throwable ex)
	{
		return new ErrorPart("denied", resolve("part.accessdenied.title"), ex.getMessage());
	}

	private ErrorPart createUrlPart(String requestUri)
	{
		return new ErrorPart("url", resolve("part.url.title"), requestUri);
	}

	private ErrorPart createLockedPart(LockedException ex)
	{
		UserBean locker = userService.getInformationForUser(ex.getUserID());
		return new ErrorPart("locked", resolve("part.itemlocked.title", locker), resolve("part.itemlocked.message"));
	}

	private String resolve(String text, Object... values)
	{
		return CurrentLocale.get("com.tle.web.sections.equella.errors." + text, values);
	}

	@Override
	public Class<DefaultErrorModel> getModelClass()
	{
		return DefaultErrorModel.class;
	}

	public static class DefaultErrorModel extends AbstractErrorSection.ErrorModel
	{
		private String titleKey;
		private final List<ErrorPart> parts = new ArrayList<ErrorPart>();

		public String getTitleKey()
		{
			return titleKey;
		}

		public void setTitleKey(String titleKey)
		{
			this.titleKey = titleKey;
		}

		public List<ErrorPart> getParts()
		{
			return parts;
		}

		public void addPart(ErrorPart part)
		{
			parts.add(part);
		}
	}

	public static class ErrorPart
	{
		private final String id;
		private final String title;
		private final String text;

		protected ErrorPart(String id, String title, String text)
		{
			this.id = id;
			this.title = title;
			this.text = text;
		}

		public String getId()
		{
			return id;
		}

		public String getTitle()
		{
			return title;
		}

		public String getText()
		{
			return text;
		}
	}

	public Button getAction()
	{
		return action;
	}
}
