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

package com.tle.web.search.actions;

import com.google.common.base.Strings;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author Aaron
 */
@Bind
@SuppressWarnings("nls")
public class StandardShareSearchQuerySection extends AbstractShareSearchQuerySection implements HtmlRenderer
{
	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Component(name = "r")
	private Link rssLink;
	@Component(name = "a")
	private Link atomLink;
	@Component(name = "g")
	@PlugKey("actions.share.dialog.email.guest")
	private Checkbox guest;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		getModel(context).setShowEmail(emailService.hasMailSettings());
		InfoBookmark bookmark = rootSearch.getPermanentUrl(context);
		setupFeeds(bookmark, context);
		setupUrl(bookmark, context);
		return viewFactory.createResult("actions/dialog/sharesearchquery.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_SHARE);

		JSValidator fail = getEmail().createNotBlankValidator().setFailureStatements(Js.alert_s(BLANK_EMAIL_LABEL));
		getSendEmailButton().setClickHandler(events.getNamedHandler("sendEmail").addValidator(fail));
	}

	private void setupFeeds(InfoBookmark bookmark, RenderContext context)
	{
		rssLink.setBookmark(context, new BookmarkAndModify(bookmark, feedServlet.getModifier(context, "rss_2.0", "")));
		rssLink.getState(context).setTarget(HtmlLinkState.TARGET_BLANK);
		atomLink.setBookmark(context,
			new BookmarkAndModify(bookmark, feedServlet.getModifier(context, "atom_1.0", "")));
		atomLink.getState(context).setTarget(HtmlLinkState.TARGET_BLANK);
	}

	protected FreetextSearchResults<?> getResults(SectionInfo info, DefaultSearch searchreq)
	{
		FreetextSearchResults<?> results;
		if( guest.isChecked(info) )
		{
			ShareEmailRunAs shareRunAs = new ShareEmailRunAs(searchreq);
			WebAuthenticationDetails details = userService.getWebAuthenticationDetails(info.getRequest());
			runAsUser.executeAsGuest(CurrentInstitution.get(), shareRunAs, details);
			results = shareRunAs.getResults();
		}
		else
		{
			results = setupResults(searchreq);
		}

		return results;
	}

	protected FreetextSearchResults<?> setupResults(Search searchreq)
	{
		return freeTextService.search(searchreq, 0, RESULTS_CAP);
	}

	protected class ShareEmailRunAs implements Runnable
	{
		private FreetextSearchResults<?> results;
		private final DefaultSearch searchreq;

		public ShareEmailRunAs(DefaultSearch searchreq)
		{
			this.searchreq = searchreq;
		}

		@Override
		public void run()
		{
			results = setupResults(searchreq);
		}

		public FreetextSearchResults<?> getResults()
		{
			return results;
		}
	}

	public Link getRssLink()
	{
		return rssLink;
	}

	public Link getAtomLink()
	{
		return atomLink;
	}

	public Checkbox getGuest()
	{
		return guest;
	}

	@Override
	protected String createEmail(SectionInfo info)
	{
		FreetextSearchEvent event = (FreetextSearchEvent) getSearchResultsSection().createSearchEvent(info);
		info.processEvent(event);

		return buildEmail(info, event);
	}

	private String buildEmail(SectionInfo info, FreetextSearchEvent event)
	{
		StringBuilder email = new StringBuilder();

		email.append(s("intro", getUser(CurrentUser.getDetails())));
		email.append(s("query", Strings.nullToEmpty(event.getQuery())));

		for( Item i : getResults(info, event.getFinalSearch()).getResults() )
		{
			email.append(s("item.name", CurrentLocale.get(i.getName())));
			email.append(s("item.link", urlFactory.createFullItemUrl(i.getItemId()).getHref()));
			email.append(s("item.version", i.getVersion()));
			if( !Check.isEmpty(i.getOwner()) )
			{
				email.append(s("item.owner", getUser(userService.getInformationForUser(i.getOwner()))));
			}
			email.append("\n");
		}
		email.append(s("outro"));

		return email.toString();
	}
}
