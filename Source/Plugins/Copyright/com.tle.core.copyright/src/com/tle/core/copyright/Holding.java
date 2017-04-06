package com.tle.core.copyright;

import java.util.Date;
import java.util.List;

import com.tle.beans.item.Item;

/**
 * @author Aaron
 */
public interface Holding
{
	Date getIssueDate();

	long getId();

	Item getItem();

	List<? extends Portion> getPortions();

	List<String> getIds();

	List<String> getAuthors();

	String getTitle();

	String getPublisher();

	String getPubDate();

	String getDescription();

	String getComments();

	String getLength();

	String getType();

	boolean isOutOfPrint();

	String getAuthorList();

	String getIdList();

	String getVolume();

	String getIssueNumber();

	void setItem(Item item);

	void setPortions(List<? extends Portion> portions);
}
