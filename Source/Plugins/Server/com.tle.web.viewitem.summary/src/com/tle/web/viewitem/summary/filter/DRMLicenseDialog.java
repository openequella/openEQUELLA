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

package com.tle.web.viewitem.summary.filter;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.DrmService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.ExecuteReady;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.viewitem.DRMFilter;
import com.tle.web.viewitem.I18nDRM;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.summary.filter.DRMLicenseDialog.DRMDialogModel;
import com.tle.web.viewurl.ItemUrlExtender;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class DRMLicenseDialog extends EquellaDialog<DRMDialogModel>
{
	@PlugKey("drm.dialog.title")
	private static Label LABEL_TITLE;

	@Inject
	private DrmService drmService;
	@TreeLookup
	private DRMFilter drmFilter;
	@TreeLookup
	private RootItemFileSection rootFileSection;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Component
	private Button acceptButton;
	@Component
	private Button previewButton;
	@Component
	private Button rejectButton;
	private JSCallable licenseFunc;

	@Override
	public DRMDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new DRMDialogModel();
	}

	@Override
	public String getWidth()
	{
		return "700px";
	}

	@Override
	public String getHeight()
	{
		return "auto";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		setAjax(true);
		super.registered(id, tree);
		rejectButton.setClickHandler(getCloseFunction());
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		licenseFunc = addParentCallable(tree.lookupSection(DRMFilter.class, null).getLicenseFunction());
		super.treeFinished(id, tree);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		Item item = rootFileSection.getViewableItem(context).getItem();
		DrmSettings rights = drmService.requiresAcceptance(item, false, false);
		DRMDialogModel model = getModel(context);
		if( rights != null )
		{
			JSHandler previewHandler = new OverrideHandler(licenseFunc, "preview", model.getLinkId(),
				getCloseFunction());
			drmFilter.initAcceptButton(context, acceptButton, events.getNamedHandler("accept"), previewHandler);
			previewButton.setClickHandler(context, previewHandler);
			I18nDRM drm = new I18nDRM(rights);
			model.setCanpreview(drm.canPreview());
			model.setDrm(drm);
			return viewFactory.createResult("viewitem/drm/license.ftl", this);
		}
		return new ExecuteReady(licenseFunc, "accept", model.getLinkId(), getCloseFunction());
	}

	@EventHandlerMethod
	public void accept(SectionInfo info)
	{
		DRMDialogModel model = getModel(info);
		drmService.acceptLicense(rootFileSection.getViewableItem(info).getItem());
		getModel(info).setAfterCloseStatements(
			new FunctionCallStatement(licenseFunc, "accept", model.getLinkId(), getCloseFunction()));
	}

	public static class LicenseOnlyUrl implements ItemUrlExtender
	{
		private static final long serialVersionUID = 1L;

		private final String linkId;

		public LicenseOnlyUrl(String id)
		{
			this.linkId = id;
		}

		@Override
		public void execute(SectionInfo info)
		{
			DRMLicenseDialog filterSection = info.lookupSection(DRMLicenseDialog.class);
			DRMDialogModel model = filterSection.getModel(info);
			model.setLinkId(linkId);
		}
	}

	public static class DRMDialogModel extends DialogModel
	{
		@Bookmarked(name = "li")
		private String linkId;
		private I18nDRM drm;
		private boolean canpreview;

		public String getLinkId()
		{
			return linkId;
		}

		public void setLinkId(String linkId)
		{
			this.linkId = linkId;
		}

		public I18nDRM getDrm()
		{
			return drm;
		}

		public void setDrm(I18nDRM drm)
		{
			this.drm = drm;
		}

		public boolean isCanpreview()
		{
			return canpreview;
		}

		public void setCanpreview(boolean canpreview)
		{
			this.canpreview = canpreview;
		}
	}

	public Button getAcceptButton()
	{
		return acceptButton;
	}

	public Button getPreviewButton()
	{
		return previewButton;
	}

	public Button getRejectButton()
	{
		return rejectButton;
	}
}
