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

package com.tle.core.services.item;

import java.io.Serializable;

import com.tle.beans.item.ItemIdKey;

public class FreetextResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean matchesPrivilege;
	private final ItemIdKey itemIdKey;
	private final float relevance;
	private final boolean sortByRelevance;
	private boolean keywordFoundInAttachment;

	public FreetextResult(ItemIdKey key, float relevance, boolean sortByRelevance)
	{
		this.itemIdKey = key;
		this.relevance = relevance;
		this.sortByRelevance = sortByRelevance;
	}

	public ItemIdKey getItemIdKey()
	{
		return itemIdKey;
	}

	public float getRelevance()
	{
		return relevance;
	}

	public boolean isSortByRelevance()
	{
		return sortByRelevance;
	}

	public boolean isMatchesPrivilege()
	{
		return matchesPrivilege;
	}

	public void setMatchesPrivilege(boolean matchesPrivilege)
	{
		this.matchesPrivilege = matchesPrivilege;
	}

	public boolean isKeywordFoundInAttachment()
	{
		return keywordFoundInAttachment;
	}

	public void setKeywordFoundInAttachment(boolean keywordFoundInAttachment)
	{
		this.keywordFoundInAttachment = keywordFoundInAttachment;
	}

}
