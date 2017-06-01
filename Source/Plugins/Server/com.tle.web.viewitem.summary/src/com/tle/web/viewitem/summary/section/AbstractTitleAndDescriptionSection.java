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

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewable.ViewableItem;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractTitleAndDescriptionSection<I extends IItem<?>, M extends AbstractTitleAndDescriptionSection.TitleAndDescriptionModel>
	extends
		AbstractPrototypeSection<AbstractTitleAndDescriptionSection.TitleAndDescriptionModel> implements HtmlRenderer
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(AbstractTitleAndDescriptionSection.class);
	private static final IncludeFile JS = new IncludeFile(resources.url("scripts/itemsummary.js"));
	private static final JSCallAndReference SUMMARY_CLASS = new ExternallyDefinedFunction("ItemSummary", JS);
	private static final JSCallable SELECT_ITEM_FUNCTION = new ExternallyDefinedFunction(SUMMARY_CLASS, "selectItem", 3);

	@PlugKey("summary.content.select")
	private static Label SELECT_ITEM_LABEL;

	@Inject
	protected SelectionService selectionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	protected abstract ViewableItem<I> getViewableItem(SectionInfo info);

	protected abstract int getMaxTitleLength(SectionInfo info);

	protected abstract int getMaxDescriptionLength(SectionInfo info);

	@Nullable
	protected abstract String getItemExtensionType();

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final TitleAndDescriptionModel model = getModel(context);
		final ViewableItem<I> vitem = getViewableItem(context);
		final I item = vitem.getItem();
		final String description = CurrentLocale.get(item.getDescription(), null);
		model.setName(CurrentLocale.get(item.getName(), item.getUuid()));
		model.setNameLength(getMaxTitleLength(context));
		model.setDescription(description == null ? null : description.trim());
		model.setDescLength(getMaxDescriptionLength(context));
		model.setDataUuid(item.getUuid());
		model.setDataVersion(item.getVersion());
		model.setDataExtensionType(getItemExtensionType());

		// Add a item summary select button if using the skinny or course layout
		final JSCallable selectItemFunction = selectionService.getSelectItemFunction(context, vitem);

		if( selectItemFunction != null )
		{
			final HtmlComponentState button = new HtmlComponentState();
			button.setLabel(SELECT_ITEM_LABEL);

			final ItemKey itemId = item.getItemId();
			final JSHandler selectHandler = new OverrideHandler(SELECT_ITEM_FUNCTION, Jq.$(button), selectItemFunction,
				itemId, getItemExtensionType());
			button.setClickHandler(selectHandler);

			final SectionRenderable dr = new DivRenderer("selectitem", new ButtonRenderer(button).showAs(
				ButtonType.PLUS).addClasses("button-expandable"));
			model.setSelectDiv(dr);
			model.setSelectable(true);
		}

		return viewFactory.createNamedResult("section_nameowner", "viewitem/title_and_description.ftl", context);
	}

	@NonNullByDefault(false)
	public static class TitleAndDescriptionModel
	{
		private String name;
		private String description;
		private int nameLength;
		private int descLength;
		private String dataUuid;
		private int dataVersion;
		private String dataExtensionType;
		private SectionRenderable selectDiv;
		private boolean selectable;

		public int getDataVersion()
		{
			return dataVersion;
		}

		public void setDataVersion(int dataVersion)
		{
			this.dataVersion = dataVersion;
		}

		public String getDataUuid()
		{
			return dataUuid;
		}

		public void setDataUuid(String dataUuid)
		{
			this.dataUuid = dataUuid;
		}

		public String getDataExtensionType()
		{
			return dataExtensionType;
		}

		public void setDataExtensionType(String dataExtensionType)
		{
			this.dataExtensionType = dataExtensionType;
		}

		public void setNameLength(int nameLength)
		{
			this.nameLength = nameLength;
		}

		public void setDescLength(int descLength)
		{
			this.descLength = descLength;
		}

		public int getNameLength()
		{
			return nameLength;
		}

		public int getDescLength()
		{
			return descLength;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public SectionRenderable getSelectDiv()
		{
			return selectDiv;
		}

		public void setSelectDiv(SectionRenderable selectDiv)
		{
			this.selectDiv = selectDiv;
		}

		public boolean isSelectable()
		{
			return selectable;
		}

		public void setSelectable(boolean selectable)
		{
			this.selectable = selectable;
		}
	}

	@Override
	public Class<TitleAndDescriptionModel> getModelClass()
	{
		return TitleAndDescriptionModel.class;
	}
}
