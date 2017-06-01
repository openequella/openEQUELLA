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
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.wizard.WizardState;

@SuppressWarnings("nls")
@NonNullByDefault
public class SelectThumbnailSection extends EquellaDialog<SelectThumbnailSection.SelectThumbnailModel>
{
	@PlugKey("selectthumbnail.title")
	private static Label LABEL_TITLE;
	@PlugKey("thumbnail.option.default")
	private static String STRING_DEFAULT;
	@PlugKey("thumbnail.option.none")
	private static String STRING_NONE;
	@PlugKey("thumbnail.option.custom")
	private static String STRING_CUSTOM;

	@Component
	@PlugKey("selectthumbnail.dialog.button.save")
	private Button save;
	@Component
	@PlugKey("selectthumbnail.dialog.button.cancel")
	private Button cancel;

	@Component(name = "sel")
	private TextField selected;
	@Component
	private SingleSelectionList<NameValue> options;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	public enum ThumbnailOption
	{
		DEFAULT, NONE, CUSTOM;

		@Override
		public String toString()
		{
			return super.toString().toLowerCase();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		SelectThumbnailModel model = getModel(context);

		WizardSectionInfo winfo = context.getAttributeForClass(WizardSectionInfo.class);

		if( !model.isLoaded() )
		{
			setupRadioOptions(context, winfo);
		}

		List<SectionRenderable> thumbs = createAttachmentThumbs(context);
		Option customOption = getCustomOption(context);
		if( customOption != null )
		{
			if( thumbs == null )
			{
				customOption.setDisabled(true);
			}
			else
			{
				customOption.setDisabled(false);
			}
		}

		model.setAttachmentThumbs(thumbs);
		model.setLoaded(true);
		return viewFactory.createResult("wizard/selectthumbnail.ftl", this);
	}

	@SuppressWarnings("rawtypes")
	@Nullable
	private Option getCustomOption(RenderContext context)
	{
		List<Option<NameValue>> allOptions = options.getListModel().getOptions(context);
		for( Option<NameValue> option : allOptions )
		{
			if( option.getValue().equals(ThumbnailOption.CUSTOM.toString()) )
			{
				return option;
			}
		}
		return null;
	}

	private void setupRadioOptions(SectionInfo info, WizardSectionInfo winfo)
	{
		WizardState state = winfo.getWizardState();
		String thumb = state.getThumbnail();
		if( Check.isEmpty(thumb) )
		{
			thumb = state.getItem().getThumb();
		}

		if( thumb.contains(ThumbnailOption.DEFAULT.toString()) )
		{
			options.setSelectedStringValue(info, ThumbnailOption.DEFAULT.toString());
		}
		else if( thumb.contains(ThumbnailOption.NONE.toString()) )
		{
			options.setSelectedStringValue(info, ThumbnailOption.NONE.toString());
		}
		else if( thumb.contains(ThumbnailOption.CUSTOM.toString()) )
		{
			options.setSelectedStringValue(info, ThumbnailOption.CUSTOM.toString());
			String[] split = thumb.split(":");
			if( split.length == 2 )
			{
				String attachmentUuid = split[1];
				selected.setValue(info, attachmentUuid);
			}
			else
			{
				options.setSelectedStringValue(info, ThumbnailOption.DEFAULT.toString());
			}
		}
	}

	@Nullable
	private List<SectionRenderable> createAttachmentThumbs(SectionInfo info)
	{
		WizardSectionInfo winfo = info.getAttributeForClass(WizardSectionInfo.class);
		List<SectionRenderable> thumbs = new ArrayList<>();

		boolean noThumb = true;
		List<Attachment> attachments = winfo.getItem().getAttachments();
		for( Attachment attachment : attachments )
		{
			String thumbnail = attachment.getThumbnail();
			if( thumbnail != null && !thumbnail.equals("suppress") )
			{
				TagRenderer li = new TagRenderer("li", new TagState());
				noThumb = false;
				li.addClass("thumbrow");
				String value = selected.getValue(info);

				if( options.getSelectedValueAsString(info).equals(ThumbnailOption.CUSTOM.toString()) )
				{
					if( Check.isEmpty(value) )
					{
						li.addClass("selected");
						selected.setValue(info, attachment.getUuid());
					}
					else if( value.equals(attachment.getUuid()) )
					{
						li.addClass("selected");
					}
				}

				HtmlLinkState state = new HtmlLinkState();
				state.addClass("thumbnail");
				state.setData("id", attachment.getUuid());
				state.setClickHandler(new OverrideHandler(events.getNamedHandler("selectThumbnail",
					attachment.getUuid())));
				LinkRenderer linkRenderer = new LinkRenderer(state);

				SectionRenderable thumbnailForAttachment = getThumbnailUrlForAttachment(info, winfo.getViewableItem(),
					attachment);

				linkRenderer.setNestedRenderable(thumbnailForAttachment);
				li.setNestedRenderable(linkRenderer);
				thumbs.add(li);
			}
		}

		if( noThumb )
		{
			return null;
		}

		return thumbs;
	}

	@SuppressWarnings("rawtypes")
	public SectionRenderable getThumbnailUrlForAttachment(SectionInfo info, ViewableItem viewableItem,
		Attachment attachment)
	{
		return attachmentResourceService.getViewableResource(info, viewableItem, attachment)
			.createStandardThumbnailRenderer(new TextLabel(attachment.getDescription())).addClass("file-thumbnail");
	}

	@EventHandlerMethod
	public void selectThumbnail(SectionInfo info, String uuid)
	{
		selected.setValue(info, uuid);
		options.setSelectedStringValue(info, ThumbnailOption.CUSTOM.toString());
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);

		SimpleHtmlListModel<NameValue> optionValue = new SimpleHtmlListModel<NameValue>(new NameValue(
			CurrentLocale.get(STRING_DEFAULT), ThumbnailOption.DEFAULT.toString()), new NameValue(
			CurrentLocale.get(STRING_NONE), ThumbnailOption.NONE.toString()), new NameValue(
			CurrentLocale.get(STRING_CUSTOM), ThumbnailOption.CUSTOM.toString()));
		options.setListModel(optionValue);
		options.setAlwaysSelect(true);
		options.addChangeEventHandler(new ReloadHandler());

		final JSCallable commandExec = events.getSubmitValuesFunction("save");
		final SimpleFunction execFunc = new SimpleFunction("exec", this, StatementBlock.get(Js.call_s(commandExec),
			Js.call_s(getCloseFunction())));

		save.setClickHandler(execFunc);
		save.setComponentAttribute(ButtonType.class, ButtonType.SAVE);
		cancel.setClickHandler(new OverrideHandler(getCloseFunction()));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		WizardSectionInfo wizInfo = info.getAttributeForClass(WizardSectionInfo.class);
		WizardState state = wizInfo.getWizardState();
		String thumb = ThumbnailOption.DEFAULT.toString();
		String option = options.getSelectedValueAsString(info);
		switch( option )
		{
			case "none":
				thumb = ThumbnailOption.NONE.toString();
				break;

			case "custom":
				if( !Check.isEmpty(selected.getValue(info)) )
				{
					thumb = ThumbnailOption.CUSTOM.toString() + ":" + selected.getValue(info);
				}
				break;
		}
		state.setThumbnail(thumb);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		final Collection<Button> buttons = Lists.newArrayList(save, cancel);
		return buttons;
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public String getWidth()
	{
		return "647px";
	}

	@Override
	public String getHeight()
	{
		return "433px";
	}

	@Override
	public SelectThumbnailModel instantiateDialogModel(SectionInfo info)
	{
		return new SelectThumbnailModel();
	}

	@Override
	public Class<SelectThumbnailModel> getModelClass()
	{
		return SelectThumbnailModel.class;
	}

	public SingleSelectionList<NameValue> getOptions()
	{
		return options;
	}

	public TextField getSelected()
	{
		return selected;
	}

	public static class SelectThumbnailModel extends DialogModel
	{
		private List<SectionRenderable> attachmentThumbs;
		@Bookmarked
		private boolean loaded;

		public List<SectionRenderable> getAttachmentThumbs()
		{
			return attachmentThumbs;
		}

		public void setAttachmentThumbs(List<SectionRenderable> attachmentThumbs)
		{
			this.attachmentThumbs = attachmentThumbs;
		}

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}
	}
}
