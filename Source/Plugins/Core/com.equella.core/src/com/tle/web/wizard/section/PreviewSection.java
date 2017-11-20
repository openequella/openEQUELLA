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

package com.tle.web.wizard.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.core.item.service.ItemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.wizard.WizardState;

@NonNullByDefault
@SuppressWarnings("nls")
public class PreviewSection extends EquellaDialog<PreviewSection.PreviewModel>
{
	@Inject
	private ItemService itemService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@PlugKey("previewitem.title")
	private static Label LABEL_TITLE;

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		PreviewModel formData = getModel(context);
		WizardSectionInfo winfo = context.getAttributeForClass(WizardSectionInfo.class);

		WizardState state = winfo.getWizardState();

		List<VersionDetail> items = new ArrayList<VersionDetail>();
		List<Item> versionDetails = itemService.getVersionDetails(state.getItemId().getUuid());
		int version = state.getItemId().getVersion();
		for( Item item : versionDetails )
		{
			if( item.getVersion() != version )
			{
				VersionDetail detail = new VersionDetail(item);
				ViewItemUrl url = urlFactory.createItemUrl(context, item.getItemId(), ViewItemUrl.FLAG_FOR_PREVIEW);
				url.setShowNav(false);
				detail.setHref(url);
				items.add(detail);
			}
		}
		VersionDetail detail = new VersionDetail(state.getItem());
		ViewItemUrl url = urlFactory.createItemUrl(context, winfo.getViewableItem(), ViewItemUrl.FLAG_FOR_PREVIEW);
		url.setShowNav(false);
		detail.setHref(url);
		items.add(detail);
		Collections.sort(items, new Comparator<VersionDetail>()
		{
			@Override
			public int compare(VersionDetail o1, VersionDetail o2)
			{
				return o2.getVersion() - o1.getVersion();
			}
		});
		formData.setVersionDetails(items);
		formData.setCurrentVersion(detail);

		return viewFactory.createResult("preview/preview.ftl", this);
	}

	@Override
	public PreviewModel instantiateDialogModel(SectionInfo info)
	{
		return new PreviewModel();
	}

	@Override
	public String getWidth()
	{
		return "auto";
	}

	@Override
	public String getHeight()
	{
		return "auto";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "wizprv";
	}

	@Override
	public Class<PreviewModel> getModelClass()
	{
		return PreviewModel.class;
	}

	public static class PreviewModel extends DialogModel
	{
		private VersionDetail currentVersion;
		private List<VersionDetail> versionDetails;

		public VersionDetail getCurrentVersion()
		{
			return currentVersion;
		}

		public void setCurrentVersion(VersionDetail currentVersion)
		{
			this.currentVersion = currentVersion;
		}

		public List<VersionDetail> getVersionDetails()
		{
			return versionDetails;
		}

		public void setVersionDetails(List<VersionDetail> versionDetails)
		{
			this.versionDetails = versionDetails;
		}
	}

	public static class VersionDetail
	{
		private final Item item;
		private Bookmark href;
		private String icon;

		public boolean isSuspended()
		{
			return getStatus().equals(ItemStatus.SUSPENDED);
		}

		public VersionDetail(Item item)
		{
			this.item = item;
		}

		public LanguageBundle getName()
		{
			return item.getName();
		}

		public ItemStatus getStatus()
		{
			return item.getStatus();
		}

		public int getVersion()
		{
			return item.getVersion();
		}

		public String getHref()
		{
			return href.getHref();
		}

		public void setHref(Bookmark href)
		{
			this.href = href;
		}

		public String getIcon()
		{
			return icon;
		}

		public void setIcon(String icon)
		{
			this.icon = icon;
		}

		public String getUuid()
		{
			return item.getUuid();
		}
	}
}
