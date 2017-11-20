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

package com.tle.web.freetext.extracter.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.TextExtracterExtension;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.mimetypes.MimeEditExtension;
import com.tle.web.mimetypes.section.MimeDetailsSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.AfterTreeLookup;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class MimeFreetextEditSection extends AbstractPrototypeSection<MimeFreetextEditSection.MimeFreetextEditModel>
	implements
		MimeEditExtension,
		HtmlRenderer,
		AfterTreeLookup
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(MimeFreetextEditSection.class);
	private static final NameValue TAB_FREETEXT = new BundleNameValue(resources.key("mimefreetextedit.title"),
		"FreetextExtracters");

	@PlugKey("head.enabled")
	private static Label LABEL_ENABLED;
	@PlugKey("head.name")
	private static Label LABEL_NAME;

	@Inject
	private MimeTypeService mimeTypeService;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private MimeDetailsSection detailsSection;

	@Component(name = "ee")
	private MappedBooleans enabledExtracters;
	@Component(name = "e")
	private Table extractorsTable;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final String mimeType = detailsSection.getType().getValue(context);
		final TableState extractorsTableState = extractorsTable.getState(context);
		final MimeFreetextEditModel model = getModel(context);

		if( !Check.isEmpty(mimeType) )
		{
			List<TextExtracterExtension> extractors = mimeTypeService.getAllTextExtracters();
			for( TextExtracterExtension extractor : extractors )
			{
				if( extractor.isMimeTypeSupported(mimeType) )
				{
					model.setExtractors(true);
					HtmlBooleanState enabledState = enabledExtracters.getBooleanState(context,
						getKeyForExtracter(extractor));
					CheckboxRenderer enabledCheckbox = new CheckboxRenderer(enabledState);

					extractorsTableState.addRow(enabledCheckbox, new KeyLabel(extractor.getNameKey()));
				}
			}
		}

		return viewFactory.createResult("extractors.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		extractorsTable.setColumnHeadings(LABEL_ENABLED, LABEL_NAME);
		extractorsTable.setColumnSorts(Sort.NONE, Sort.PRIMARY_ASC);
	}

	@Override
	public Class<MimeFreetextEditModel> getModelClass()
	{
		return MimeFreetextEditModel.class;
	}

	@Override
	public void loadEntry(SectionInfo info, MimeEntry entry)
	{
		if( entry != null )
		{
			// list all enabled viewers
			List<TextExtracterExtension> extractors = mimeTypeService.getTextExtractersForMimeEntry(entry);

			List<String> enabled = new ArrayList<String>();
			for( TextExtracterExtension extractor : extractors )
			{
				enabled.add(getKeyForExtracter(extractor));
			}
			enabledExtracters.setCheckedSet(info, enabled);
		}
	}

	@Override
	public void saveEntry(SectionInfo info, MimeEntry entry)
	{
		List<TextExtracterExtension> extractors = mimeTypeService.getAllTextExtracters();

		Set<String> enabled = enabledExtracters.getCheckedSet(info);
		for( TextExtracterExtension extractor : extractors )
		{
			extractor.setEnabledForMimeEntry(entry, enabled.contains(getKeyForExtracter(extractor)));
		}
	}

	private String getKeyForExtracter(TextExtracterExtension extractor)
	{
		return extractor.getClass().getSimpleName();
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "ftm";
	}

	@Override
	public NameValue getTabToAppearOn()
	{
		return TAB_FREETEXT;
	}

	@Override
	public boolean isVisible(SectionInfo info)
	{
		return true;
	}

	public Table getExtractorsTable()
	{
		return extractorsTable;
	}

	public static class MimeFreetextEditModel
	{
		private boolean extractors;

		public boolean hasExtractors()
		{
			return extractors;
		}

		public void setExtractors(boolean extractors)
		{
			this.extractors = extractors;
		}
	}

	@Override
	public void afterTreeLookup(SectionTree tree)
	{
		detailsSection.addAjaxId("extractors");
	}
}
