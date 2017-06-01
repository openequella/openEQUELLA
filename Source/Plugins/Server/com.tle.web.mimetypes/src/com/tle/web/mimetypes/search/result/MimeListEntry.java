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

package com.tle.web.mimetypes.search.result;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;
import com.tle.web.itemlist.DelimitedMetadata;
import com.tle.web.itemlist.MetadataEntry;
import com.tle.web.itemlist.item.AbstractListEntry;
import com.tle.web.mimetypes.service.WebMimeTypeService;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;

@Bind
public class MimeListEntry extends AbstractListEntry
{
	static
	{
		PluginResourceHandler.init(MimeListEntry.class);
	}

	@PlugKey("list.result.delete")
	private static Label DELETE_LABEL;
	@PlugKey("list.result.edit")
	private static Label EDIT_LABEL;
	@PlugKey("label.extensions")
	private static Label EXTENSIONS_LABEL;

	private final List<MetadataEntry> entries = new ArrayList<MetadataEntry>();

	@Inject
	private WebMimeTypeService webMimeService;

	private JSHandler editHandler;
	private JSHandler deleteHandler;
	private MimeEntry mime;
	private SectionRenderable image;

	@Override
	public HtmlBooleanState getCheckbox()
	{
		return new HtmlBooleanState();
	}

	@Override
	public Label getDescription()
	{
		String desc = mime.getDescription();
		desc = desc == null ? "" : desc; //$NON-NLS-1$
		return new TextLabel(desc);
	}

	@Override
	public List<MetadataEntry> getMetadata()
	{
		return entries;
	}

	public List<SectionRenderable> getActions()
	{
		List<SectionRenderable> actions = new ArrayList<SectionRenderable>();

		if( editHandler != null )
		{
			actions.add(new ButtonRenderer(new HtmlComponentState(EDIT_LABEL, editHandler)).showAs(ButtonType.EDIT));
		}
		if( deleteHandler != null )
		{
			actions.add(new ButtonRenderer(new HtmlComponentState(DELETE_LABEL, deleteHandler))
				.showAs(ButtonType.DELETE));
		}
		return actions;
	}

	@Override
	public HtmlLinkState getTitle()
	{
		return new HtmlLinkState(new TextLabel(mime.getType()), editHandler);
	}

	public void init()
	{
		Collection<String> extensions = mime.getExtensions();
		if( extensions.size() > 0 )
		{
			SectionRenderable[] exts = SectionUtils.convertToRenderers(extensions);
			entries.add(new DelimitedMetadata(EXTENSIONS_LABEL, Arrays.asList(exts)));
		}

		URL mimeIcon = webMimeService.getIconForEntry(mime, false);
		if( mimeIcon != null )
		{
			image = new ImageRenderer(mimeIcon.toString(), new TextLabel(mime.getType()));
		}
	}

	@Override
	public void addMetadata(MetadataEntry entry)
	{
		entries.add(entry);
	}

	public void setEditHandler(JSHandler editHandler)
	{
		this.editHandler = editHandler;
	}

	public void setDeleteHandler(JSHandler deleteHandler)
	{
		this.deleteHandler = deleteHandler;
	}

	public void setMime(MimeEntry mime)
	{
		this.mime = mime;
	}

	public SectionRenderable getImage()
	{
		return image;
	}
}
