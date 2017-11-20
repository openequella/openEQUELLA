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

package com.tle.beans.cal;

import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.item.Item;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;

@Entity
@AccessType("field")
@Table(name = "cal_holding", uniqueConstraints = {@UniqueConstraint(columnNames = {"item_id"})})
public class CALHolding implements Holding
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@OneToOne(fetch = FetchType.LAZY)
	@Index(name = "holdingItemIndex")
	private Item item;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "holding")
	private List<CALPortion> portions;

	@Column(length = 20)
	private String type;

	@ElementCollection
	@CollectionTable(name = "cal_holding_ids", joinColumns = @JoinColumn(name = "cal_holding_id"))
	@Column(name = "element")
	private List<String> ids;
	@ElementCollection
	@CollectionTable(name = "cal_holding_authors", joinColumns = @JoinColumn(name = "cal_holding_id"))
	@Column(name = "element")
	private List<String> authors;

	@Lob
	private String authorList;
	@Lob
	private String idList;

	@Column(length = 20)
	private String pubDate;
	@Lob
	private String title;
	@Lob
	private String publisher;
	@Lob
	private String description;
	@Lob
	private String comments;
	@Column(length = 512)
	private String length;
	private boolean outOfPrint;

	// Journal specific
	@Column(length = 256)
	private String volume;

	@Column(length = 256)
	private String issueNumber;

	private Date issueDate;

	@Override
	public Date getIssueDate()
	{
		return issueDate;
	}

	public void setIssueDate(Date issueDate)
	{
		this.issueDate = issueDate;
	}

	@Override
	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	@Override
	public Item getItem()
	{
		return item;
	}

	@Override
	public void setItem(Item item)
	{
		this.item = item;
	}

	@Override
	public List<String> getIds()
	{
		return ids;
	}

	public void setIds(List<String> ids)
	{
		this.ids = ids;
	}

	@Override
	public List<String> getAuthors()
	{
		return authors;
	}

	public void setAuthors(List<String> authors)
	{
		this.authors = authors;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Override
	public String getPublisher()
	{
		return publisher;
	}

	public void setPublisher(String publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public String getPubDate()
	{
		return pubDate;
	}

	public void setPubDate(String publishedDate)
	{
		this.pubDate = truncated(publishedDate, 20);
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String getComments()
	{
		return comments;
	}

	public void setComments(String comments)
	{
		this.comments = comments;
	}

	@Override
	public String getLength()
	{
		return length;
	}

	public void setLength(String length)
	{
		this.length = truncated(length, 512);
	}

	private static String truncated(String str, int max)
	{
		if( str.length() > max )
		{
			return str.substring(0, max);
		}
		return str;
	}

	@Override
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	@Override
	public boolean isOutOfPrint()
	{
		return outOfPrint;
	}

	@Override
	public String getAuthorList()
	{
		return authorList;
	}

	public void setAuthorList(String authorList)
	{
		this.authorList = authorList;
	}

	@Override
	public String getIdList()
	{
		return idList;
	}

	public void setIdList(String idList)
	{
		this.idList = idList;
	}

	public void setOutOfPrint(boolean outOfPrint)
	{
		this.outOfPrint = outOfPrint;
	}

	@Override
	public String getVolume()
	{
		return volume;
	}

	public void setVolume(String volume)
	{
		this.volume = truncated(volume, 256);
	}

	@Override
	public String getIssueNumber()
	{
		return issueNumber;
	}

	public void setIssueNumber(String issueNumber)
	{
		this.issueNumber = truncated(issueNumber, 256);
	}

	@Override
	public List<? extends Portion> getPortions()
	{
		return portions;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setPortions(List<? extends Portion> portions)
	{
		this.portions = (List<CALPortion>) portions;
	}

	public List<CALPortion> getCALPortions()
	{
		return portions;
	}

}
