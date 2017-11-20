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

package com.tle.web.remoterepo.equella;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.equella.EquellaRepoSessionDialog.ItemSelection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.selection.section.CourseListVetoSection;

/**
 * @author Aaron
 */
@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class EquellaRepoStartSection
	extends
		AbstractPrototypeSection<EquellaRepoStartSection.EquellaRepoSelectionSessionModel>
	implements
		HtmlRenderer,
		CourseListVetoSection
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(EquellaRepoStartSection.class);
	private static final String OPTION_ATTACHMENTS = "attachments";
	private static final String OPTION_METADATA = "metadata";

	@Inject
	private BundleCache bundleCache;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@TreeLookup
	private EquellaRepoDownloadProgressSection downloadSection;

	@Inject
	@Component
	private EquellaRepoSessionDialog selectionDialog;
	@Component
	private SingleSelectionList<NameValue> downloadOptions;
	@Component
	private Button startDownloadButton;

	private JSCallable selectionsMadeFunction;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return view.createResult("start.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		List<NameValue> options = new ArrayList<NameValue>();
		options.add(new BundleNameValue(resources.key("start.downloadoption.attachments"), OPTION_ATTACHMENTS,
			bundleCache));
		options.add(new BundleNameValue(resources.key("start.downloadoption.metadata"), OPTION_METADATA, bundleCache));
		downloadOptions.setListModel(new SimpleHtmlListModel<NameValue>(options));
		downloadOptions.setAlwaysSelect(true);

		startDownloadButton.setClickHandler(events.getNamedHandler("startDownload"));

		selectionsMadeFunction = CallAndReferenceFunction.get(events.getSubmitValuesFunction("selectionMade"),
			selectionDialog);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selectionDialog.setOkCallback(selectionsMadeFunction);
	}

	@EventHandlerMethod
	public void startDownload(SectionInfo info)
	{
		final EquellaRepoSelectionSessionModel model = getModel(info);
		final ItemSelection selection = model.getSelection();
		if( selection != null )
		{
			final String attachmentsSelection = downloadOptions.getSelectedValueAsString(info);
			final boolean downloadAttachments = OPTION_ATTACHMENTS.equals(attachmentsSelection);
			downloadSection.setDownloadOptions(info, selection.getUuid(), selection.getVersion(), downloadAttachments);
		}
	}

	@EventHandlerMethod
	public void selectionMade(SectionInfo info, @Nullable ItemSelection selection)
	{
		if( selection != null )
		{
			final EquellaRepoSelectionSessionModel model = getModel(info);
			model.setSelection(selection);
		}
	}

	@Override
	public Class<EquellaRepoSelectionSessionModel> getModelClass()
	{
		return EquellaRepoSelectionSessionModel.class;
	}

	public EquellaRepoSessionDialog getSelectionDialog()
	{
		return selectionDialog;
	}

	public SingleSelectionList<NameValue> getDownloadOptions()
	{
		return downloadOptions;
	}

	public Button getStartDownloadButton()
	{
		return startDownloadButton;
	}

	public JSCallable getSelectionsMadeFunction()
	{
		return selectionsMadeFunction;
	}

	@NonNullByDefault(false)
	public static class EquellaRepoSelectionSessionModel
	{
		@Bookmarked(name = "s")
		private ItemSelection selection;
		private boolean startable;

		public ItemSelection getSelection()
		{
			return selection;
		}

		public void setSelection(ItemSelection selection)
		{
			this.selection = selection;
		}

		public boolean isStartable()
		{
			return startable;
		}

		public void setStartable(boolean startable)
		{
			this.startable = startable;
		}
	}
}
