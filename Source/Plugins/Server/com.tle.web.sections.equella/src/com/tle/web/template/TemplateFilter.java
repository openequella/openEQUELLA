package com.tle.web.template;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionFilter;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.EventAuthoriser;
import com.tle.web.sections.registry.TreeRegistry;

@SuppressWarnings("nls")
@Bind
@Singleton
public class TemplateFilter implements SectionFilter
{
	@Inject
	private UrlService urlService;
	@Inject
	private TreeRegistry treeRegistry;

	private static final String TEMPLATE_TREE = "$TEMPLATE$";

	@Override
	public void filter(MutableSectionInfo info)
	{
		HttpServletRequest request = info.getRequest();
		if( request != null )
		{
			info.setAttribute(SectionInfo.KEY_BASE_HREF, urlService.getBaseUriFromRequest(request));
		}
		else
		{
			info.setAttribute(SectionInfo.KEY_BASE_HREF, URI.create(urlService.getInstitutionUrl().toString()));
		}
		SectionTree tree = treeRegistry.getTreeForPath(TEMPLATE_TREE);
		info.setAttribute(EventAuthoriser.class, new EventAuthoriser()
		{
			@Override
			public void checkAuthorisation(SectionInfo info)
			{
				// If we are anonymous then there is nothing to prevent (ie no trickery of getting admins to click on destructive links
				// is happening)
				if( !CurrentUser.isGuest()
					&& !CurrentUser.getSessionID().equals(info.getRequest().getParameter(RenderTemplate.XSRF_PARAM)) )
				{
					throw new AccessDeniedException(CurrentLocale
						.get("com.tle.web.sections.equella.templatefilter.error.xsrfmissing"));
				}
			}

			@Override
			public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
			{
				bookmarkState.put(RenderTemplate.XSRF_PARAM, new String[]{CurrentUser.getSessionID()});
			}
		});
		info.addTree(tree);
		info.queueTreeEvents(tree);
	}
}
