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

package com.tle.beans.item.attachments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.IdCloneable;
import com.tle.beans.item.ForeignItemKey;
import com.tle.beans.item.Item;
import com.tle.common.DoNotSimplify;

@Entity
@AccessType("field")
public class ItemNavigationNode implements Serializable, Cloneable, IdCloneable, ForeignItemKey, IItemNavigationNode
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "itemNavNodeItem")
	private Item item;

	@Column(length = 40)
	private String uuid;

	@Lob
	private String name;

	@Column(length = 100)
	private String icon;

	@ManyToOne
	@DoNotSimplify
	@Index(name = "itemNavNodeParent")
	private ItemNavigationNode parent;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@IndexColumn(name = "tabindex", nullable = false)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "node_id", nullable = false)
	private List<ItemNavigationTab> tabs;

	@Column(length = 200)
	private String identifier;

	@Column(name = "`index`")
	private int index;

	public ItemNavigationNode()
	{
		// nothing
	}

	public ItemNavigationNode(Item item)
	{
		this.item = item;
		this.uuid = UUID.randomUUID().toString();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ItemNavigationNode getParent()
	{
		return parent;
	}

	public void setParent(ItemNavigationNode parent)
	{
		this.parent = parent;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Item getItem()
	{
		return item;
	}

	@Override
	public void setItem(Item item)
	{
		this.item = item;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public List<ItemNavigationTab> getTabs()
	{
		return tabs;
	}

	public void setTabs(List<ItemNavigationTab> tabs)
	{
		this.tabs = tabs;
	}

	public List<ItemNavigationTab> ensureTabs()
	{
		if( tabs == null )
		{
			tabs = new ArrayList<ItemNavigationTab>();
		}
		return tabs;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		if( identifier != null && identifier.length() > 199 )
		{
			identifier = identifier.substring(0, 199);
		}
		this.identifier = identifier;
	}

}
