package com.tle.core.copyright;

import java.util.List;

import com.tle.beans.item.Item;

/**
 * @author Aaron
 */
public interface Portion
{
	long getId();

	Item getItem();

	Holding getHolding();

	List<String> getAuthors();

	List<? extends Section> getSections();

	String getTitle();

	List<String> getTopics();

	String getAuthorList();

	String getChapter();

	void setItem(Item item);

	void setHolding(Holding holding);
}
