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

package com.tle.web.remoterepo;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.Check;
import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.web.itemlist.MetadataEntry;
import com.tle.web.itemlist.StdMetadataEntry;
import com.tle.web.itemlist.item.AbstractListEntry;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author aholland
 */
public abstract class RemoteRepoListEntry<R extends RemoteRepoSearchResult> extends AbstractListEntry
{
	protected int index;
	protected R result;
	protected Bookmark view;
	protected SectionInfo info;

	@Override
	public HtmlBooleanState getCheckbox()
	{
		return new HtmlBooleanState();
	}

	@Override
	public Label getDescription()
	{
		return new TextLabel(result.getDescription());
	}

	@Override
	public HtmlLinkState getTitle()
	{
		HtmlLinkState state = new HtmlLinkState();
		state.setLabel(new TextLabel(result.getTitle()));
		state.setBookmark(view);
		return state;
	}

	@Override
	public List<MetadataEntry> getMetadata()
	{
		return new ArrayList<MetadataEntry>();
	}

	/**
	 * To be used inside getMetadata.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<MetadataEntry> contents, String key, String value)
	{
		addField(contents, getLabel(key), value);
	}

	/**
	 * To be used inside getMetadata.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<MetadataEntry> contents, String key, int value)
	{
		addField(contents, getLabel(key), Integer.toString(value));
	}

	/**
	 * To be used inside getMetadata.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<MetadataEntry> contents, Label label, int value)
	{
		addField(contents, label, Integer.toString(value));
	}

	/**
	 * To be used inside getMetadata.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<MetadataEntry> contents, Label label, String value)
	{
		if( !Check.isEmpty(value) )
		{
			addLabelField(contents, label, new TextLabel(value));
		}
	}

	/**
	 * To be used inside getMetadata.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addLabelField(List<MetadataEntry> contents, Label label, Label value)
	{
		addField(contents, label, new LabelRenderer(value));
	}

	/**
	 * To be used inside getMetadata.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<MetadataEntry> contents, String key, SectionRenderable renderable)
	{
		contents.add(new StdMetadataEntry(getLabel(key), renderable));
	}

	/**
	 * To be used inside getMetadata.
	 * 
	 * @param contents
	 * @param key
	 * @param value
	 */
	protected void addField(List<MetadataEntry> contents, Label label, SectionRenderable renderable)
	{
		contents.add(new StdMetadataEntry(label, renderable));
	}

	protected Label getLabel(String key)
	{
		return new KeyLabel(getKeyPrefix() + key);
	}

	protected abstract String getKeyPrefix();

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public void setResult(R result)
	{
		this.result = result;
	}

	public R getResult()
	{
		return result;
	}

	public void setView(Bookmark view)
	{
		this.view = view;
	}

	public SectionInfo getInfo()
	{
		return info;
	}

	@Override
	public void setInfo(SectionInfo info)
	{
		this.info = info;
	}
}
