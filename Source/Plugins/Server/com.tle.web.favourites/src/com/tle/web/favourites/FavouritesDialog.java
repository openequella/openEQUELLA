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

package com.tle.web.favourites;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.RuntimeStatement;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class FavouritesDialog extends AbstractOkayableDialog<FavouritesDialog.MyFavouritesModel>
{
	@PlugKey("dialog.title")
	private static Label TITLE_LABEL;
	@PlugKey("add")
	private static Label ADD_LABEL;
	@PlugKey("latest")
	private static String LATEST;
	@PlugKey("current")
	private static String CURRENT;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private ItemService itemService;

	@Component(name = "tf")
	private TextField tagsField;
	@Component(name = "v")
	private SingleSelectionList<VoidKeyOption> version;

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE_LABEL;
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "favouritesdialog";
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("showItem");
	}

	@EventHandlerMethod
	public void showItem(SectionInfo info, String itemId)
	{
		MyFavouritesModel model = getModel(info);
		model.setItemId(itemId);
		super.showDialog(info);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		MyFavouritesModel model = getModel(context);
		model.setShowVersion(true);
		version.setSelectedStringValue(context, "true");

		// Check if viewing latest version
		if( isNotLatest(getItemId(context)) )
		{
			model.setShowVersion(false);
			version.setDisplayed(context, false);
		}

		return viewFactory.createResult("favouritesdialog.ftl", this);
	}

	@Override
	protected JSStatements createOkCallStatement(SectionTree tree)
	{
		return new RuntimeStatement()
		{
			@Override
			protected JSStatements createStatements(RenderContext context)
			{
				ItemId itemId = getItemId(context);
				return new FunctionCallStatement(getOkCallback(), tagsField.createGetExpression(), isNotLatest(itemId)
					? false : version.createGetExpression(), itemId);
			}
		};
	}

	private ItemId getItemId(SectionInfo info)
	{
		return new ItemId(getModel(info).getItemId());
	}

	@Override
	public MyFavouritesModel instantiateDialogModel(SectionInfo info)
	{
		return new MyFavouritesModel();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		List<VoidKeyOption> opts = new ArrayList<VoidKeyOption>();
		opts.add(new VoidKeyOption(LATEST, "true"));
		opts.add(new VoidKeyOption(CURRENT, "false"));
		version.setListModel(new SimpleHtmlListModel<VoidKeyOption>(opts));
		setAjax(true);
	}

	private boolean isNotLatest(ItemId itemId)
	{
		return (itemId.getVersion() < itemService.getLatestVersion(itemId.getUuid()));
	}

	public static class MyFavouritesModel extends DialogModel
	{
		private boolean showVersion;
		@Bookmarked
		private String itemId;

		public boolean isShowVersion()
		{
			return showVersion;
		}

		public void setShowVersion(boolean showVersion)
		{
			this.showVersion = showVersion;
		}

		public String getItemId()
		{
			return itemId;
		}

		public void setItemId(String itemId)
		{
			this.itemId = itemId;
		}
	}

	public TextField getTagsField()
	{
		return tagsField;
	}

	@Override
	protected Label getOkLabel()
	{
		return ADD_LABEL;
	}

	public SingleSelectionList<VoidKeyOption> getVersion()
	{
		return version;
	}
}
