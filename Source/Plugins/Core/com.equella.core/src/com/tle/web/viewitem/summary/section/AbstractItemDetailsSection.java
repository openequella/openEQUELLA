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

package com.tle.web.viewitem.summary.section;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummaryDisplayTemplate;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.navigation.BreadcrumbService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.ItemStatusKeys;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.HideableFromDRMSection;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.VersionsContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public abstract class AbstractItemDetailsSection<M extends AbstractItemDetailsSection.ItemDetailsModel>
	extends
		AbstractParentViewItemSection<M>
	implements HideableFromDRMSection
{
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;
	@Inject
	private BreadcrumbService breadcrumbService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@TreeLookup(mandatory = false)
	protected ItemSummaryContentSection contentSection;
	@TreeLookup(mandatory = false)
	private VersionsContentSection versionsContentSection;

	@Component(name = "sv")
	@PlugKey("summary.sidebar.itemdetailsgroup.display")
	private Link showVersionsLink;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			return null;
		}
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		final Item item = itemInfo.getItem();
		final ItemDefinition itemDefinition = item.getItemDefinition();
		final SummaryDisplayTemplate summaryConfig = itemDefinition.getItemSummaryDisplayTemplate();
		final ItemDetailsModel model = getModel(context);

		if( !summaryConfig.isHideOwner() )
		{
			model.setOwnerLink(userLinkSection.createLink(context, item.getOwner()));
		}

		if( !summaryConfig.isHideCollaborators() )
		{
			Set<String> collabs = item.getCollaborators();
			if( !Check.isEmpty(collabs) )
			{
				model.setCollaboratorLinks(userLinkSection.createLinks(context, collabs));
			}
		}

		TagState col = breadcrumbService.getSearchCollectionCrumb(context, itemDefinition.getUuid());
		if( col instanceof HtmlLinkState )
		{
			((HtmlLinkState) col).setDisabled(isForPreview(context) || isInIntegration(context));
		}

		model.setCollectionLink(col);

		model.setStatus(CurrentLocale.get(ItemStatusKeys.get(item.getStatus())));
		model.setVersion(new NumberLabel(item.getVersion()));

		List<SectionRenderable> sections = renderChildren(context, new ResultListCollector()).getResultList();
		model.setSections(sections);
		return view.createResult(getTemplate(context), context);
	}

	protected abstract String getTemplate(RenderEventContext context);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		userLinkSection = userLinkService.register(tree, id);
		showVersionsLink.setClickHandler(events.getNamedHandler("showVersions"));
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return !getModel(info).isHide();
	}

	@EventHandlerMethod
	public void showVersions(SectionInfo info)
	{
		contentSection.setSummaryId(info, versionsContentSection);
	}

	@Override
	public ItemDetailsModel instantiateModel(SectionInfo info)
	{
		return new ItemDetailsModel();
	}

	@Override
	public void showSection(SectionInfo info, boolean show)
	{
		getModel(info).setHide(!show);
	}

	public Link getShowVersionsLink()
	{
		return showVersionsLink;
	}

	public void setContentSection(ItemSummaryContentSection contentSection)
	{
		this.contentSection = contentSection;
	}

	public void setVersionsContentSection(VersionsContentSection versionsContentSection)
	{
		this.versionsContentSection = versionsContentSection;
	}

	public static class ItemDetailsModel
	{
		private HtmlLinkState ownerLink;
		private List<HtmlLinkState> collaboratorLinks;
		private TagState collectionLink;
		private String status;
		private Label version;
		private List<SectionRenderable> sections;
		private boolean hide;

		public HtmlLinkState getOwnerLink()
		{
			return ownerLink;
		}

		public void setOwnerLink(HtmlLinkState ownerLink)
		{
			this.ownerLink = ownerLink;
		}

		public List<HtmlLinkState> getCollaboratorLinks()
		{
			return collaboratorLinks;
		}

		public void setCollaboratorLinks(List<HtmlLinkState> collaboratorLinks)
		{
			this.collaboratorLinks = collaboratorLinks;
		}

		public TagState getCollectionLink()
		{
			return collectionLink;
		}

		public void setCollectionLink(TagState collectionLink)
		{
			this.collectionLink = collectionLink;
		}

		public String getStatus()
		{
			return status;
		}

		public void setStatus(String status)
		{
			this.status = status;
		}

		public Label getVersion()
		{
			return version;
		}

		public void setVersion(Label version)
		{
			this.version = version;
		}

		public List<SectionRenderable> getSections()
		{
			return sections;
		}

		public void setSections(List<SectionRenderable> sections)
		{
			this.sections = sections;
		}

		public boolean isHide()
		{
			return hide;
		}

		public void setHide(boolean hide)
		{
			this.hide = hide;
		}
	}
}