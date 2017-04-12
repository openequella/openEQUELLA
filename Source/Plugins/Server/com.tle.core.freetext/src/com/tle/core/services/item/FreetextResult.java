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
