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

package com.tle.web.controls.universal.handlers.fileupload.options;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.controls.universal.handlers.fileupload.TypeOptions;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author Aaron
 */
@Bind
@SuppressWarnings("nls")
public class ZipOptions extends AbstractPrototypeSection<ZipOptions.ZipOptionsModel>
	implements
		TypeOptions,
		HtmlRenderer
{
	private static final String UNZIP = "unzip";
	private static final String FILE = "file";

	@PlugKey("handlers.file.zipoptions.title")
	private static Label TITLE;

	@ViewFactory
	private FreemarkerFactory view;

	@Component
	private SingleSelectionList<NameValue> zipOptions;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getModel(context).setEditTitle(TITLE);
		return view.createResult("file/file-zipoptions.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		List<NameValue> values = new ArrayList<NameValue>();
		values.add(new BundleNameValue("com.tle.web.wizard.controls.universal.handlers.file.zipoptions.unzipselect",
			UNZIP));
		values.add(new BundleNameValue("com.tle.web.wizard.controls.universal.handlers.file.zipoptions.attach", FILE));
		zipOptions.setListModel(new SimpleHtmlListModel<NameValue>(values));
		zipOptions.setAlwaysSelect(true);
	}

	@Override
	public void loadOptions(SectionInfo info, UploadedFile uploadedFile)
	{
		final String type = uploadedFile.getResolvedType();
		if( type == null || type.equals(FileUploadHandler.FILE_TYPE_ZIP) )
		{
			zipOptions.setSelectedStringValue(info, UNZIP);
		}
		else
		{
			zipOptions.setSelectedStringValue(info, FILE);
		}
	}

	@Override
	public boolean saveOptions(SectionInfo info, UploadedFile uploadedFile)
	{
		String opt = zipOptions.getSelectedValueAsString(info);
		if( opt.equals(UNZIP) )
		{
			uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_ZIP);
		}
		else
		{
			uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_FILE);
		}
		return true;
	}

	public SingleSelectionList<NameValue> getZipOptions()
	{
		return zipOptions;
	}

	@Override
	public Class<ZipOptionsModel> getModelClass()
	{
		return ZipOptionsModel.class;
	}

	public static class ZipOptionsModel
	{
		private Label editTitle;

		public Label getEditTitle()
		{
			return editTitle;
		}

		public void setEditTitle(Label editTitle)
		{
			this.editTitle = editTitle;
		}
	}
}
