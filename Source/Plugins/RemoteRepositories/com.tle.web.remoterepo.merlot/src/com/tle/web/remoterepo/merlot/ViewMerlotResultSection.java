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

package com.tle.web.remoterepo.merlot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.merlot.service.MerlotService;
import com.tle.core.remoterepo.merlot.service.impl.MerlotSearchResult;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * @author agibb
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class ViewMerlotResultSection
	extends
		RemoteRepoViewResultSection<ViewMerlotResultSection.ViewMerlotResultModel, MerlotListEntry, MerlotSearchResult>

{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ViewMerlotResultSection.class);

	@Inject
	private MerlotService merlotService;
	@Inject
	private MerlotWebService merlotWebService;
	@Inject
	private DateRendererFactory dateRendererFactory;
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public Class<ViewMerlotResultModel> getModelClass()
	{
		return ViewMerlotResultModel.class;
	}

	@Override
	protected void clearResult(SectionInfo info)
	{
		getModel(info).setView(false);
	}

	@Override
	public boolean isShowing(SectionInfo info)
	{
		return getModel(info).isView();
	}

	@Override
	protected Label getTitle(SectionInfo info, ViewMerlotResultModel model)
	{
		return new TextLabel(model.getResult().getTitle());
	}

	@Override
	protected void getContents(SectionInfo info, ViewMerlotResultModel model, List<SectionRenderable> contents)
	{
		final MerlotSearchResult result = model.getResult();

		addField(contents, "result.description", result.getDescription());

		addUrlField(contents, "result.detailurl", result.getDetailUrl());
		addUrlField(contents, "result.url", result.getUrl());

		if( result.getPublishedDate() != null )
		{
			addField(contents, "result.creationdate", dateRendererFactory.createDateRenderer(result.getPublishedDate()));
		}
		if( result.getModifiedDate() != null )
		{
			addField(contents, "result.modifieddate", dateRendererFactory.createDateRenderer(result.getModifiedDate()));
		}
		addField(contents, "result.categories", result.getCategories() == null ? null : result.getCategories()
			.toArray());
		addField(contents, "result.community", result.getCommunity());
		addField(contents, "result.creativecommons", result.getCreativeCommons());
		addField(contents, "result.audiences", result.getAudiences() == null ? null : result.getAudiences().toArray());

		final List<String> langs = result.getLanguages();
		final List<String> niceLangs = new ArrayList<String>();
		if( langs != null )
		{
			for( String lang : langs )
			{
				final String lookedUp = merlotWebService.lookupLanguage(info, lang);
				if( !Check.isEmpty(lookedUp) )
				{
					niceLangs.add(lookedUp);
				}
			}
			addField(contents, "result.languages", niceLangs.toArray());
		}

		if( result.getTechnicalRequirements() != null )
		{
			addField(contents, "result.technicalrequirements",
				new LabelRenderer(new TextLabel(result.getTechnicalRequirements(), true)));
		}
		addField(contents, "result.copyright", result.getCopyright());

		final String cost = result.getCost();
		final String free;
		if( cost == null )
		{
			free = "unknown";
		}
		else if( cost.equals("no") )
		{
			free = "yes";
		}
		else if( cost.equals("yes") )
		{
			free = "no";
		}
		else
		{
			free = "unknown";
		}
		addField(contents, "result.free", free);

		addField(contents, "result.materialtype", result.getMaterialType());
		addField(contents, "result.section508compliant", result.getSection508Compliant());
		addField(contents, "result.sourceavailable", result.getSourceAvailable());
		addField(contents, "result.submitter", result.getSubmitter());

		addUrlField(contents, "result.comments", result.getCommentsUrl());
		// addField(contents, "result.comments.rating",
		// result.getCommentsRating());
		// addField(contents, "result.comments.count",
		// result.getCommentsCount());
		addUrlField(contents, "result.personalcollections", result.getPersonalCollectionsUrl());
		// addField(contents, "result.personalcollections.count",
		// result.getPersonalCollectionsCount());
		addUrlField(contents, "result.learningexercises", result.getLearningExercisesUrl());
		// addField(contents, "result.learningexercises.count",
		// result.getLearningExercisesCount());
		addUrlField(contents, "result.peerreview", result.getPeerReviewUrl());
		// addField(contents, "result.peerreview.rating",
		// result.getPeerReviewRating());

		contents.add(view.createResult("viewmerlotfooter.ftl", this));
	}

	private void addUrlField(List<SectionRenderable> contents, String key, String url)
	{
		if( url != null )
		{
			final HtmlLinkState state = new HtmlLinkState(new TextLabel(url), new SimpleBookmark(url));
			state.setTarget(HtmlLinkState.TARGET_BLANK);
			addField(contents, key, new LinkRenderer(state));
		}
	}

	@Override
	public Bookmark getViewHandler(SectionInfo info, MerlotListEntry listItem)
	{
		final MerlotSearchResult result = listItem.getResult();
		return new BookmarkAndModify(info, events.getNamedModifier("viewResult", result.getIndex()));
	}

	@EventHandlerMethod
	public void viewResult(SectionInfo info, int resultIndex)
	{
		final ViewMerlotResultModel model = getModel(info);
		model.setView(true);
		model.setIndex(resultIndex);
	}

	@Override
	protected PropBagEx getImportXml(SectionInfo info, FederatedSearch search)
	{
		return resolveRecord(info, search, getModel(info).getIndex()).getXml();
	}

	@Override
	protected void setupModel(SectionInfo info, ViewMerlotResultModel model, FederatedSearch search)
	{
		model.setResult(resolveRecord(info, search, model.getIndex()));
	}

	private MerlotSearchResult resolveRecord(SectionInfo info, FederatedSearch search, int index)
	{
		final MerlotRemoteRepoSearchEvent merlot = new MerlotRemoteRepoSearchEvent(getRootRemoteRepoSection(), search);
		info.processEvent(merlot);
		return merlotService.getResult(merlot, index);
	}

	@Override
	protected String getKeyPrefix()
	{
		return resources.pluginId() + ".";
	}

	public static class ViewMerlotResultModel
		extends
			RemoteRepoViewResultSection.RemoteRepoViewResultModel<MerlotSearchResult>
	{
		@Bookmarked(name = "v")
		private boolean view;
		@Bookmarked(name = "i")
		private int index;

		public boolean isView()
		{
			return view;
		}

		public void setView(boolean view)
		{
			this.view = view;
		}

		public int getIndex()
		{
			return index;
		}

		public void setIndex(int index)
		{
			this.index = index;
		}
	}
}
