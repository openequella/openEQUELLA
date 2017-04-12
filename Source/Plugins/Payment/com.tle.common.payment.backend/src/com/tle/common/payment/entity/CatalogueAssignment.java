package com.tle.common.payment.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.IdCloneable;
import com.tle.beans.item.ForeignItemKey;
import com.tle.beans.item.Item;

@Entity
@AccessType("field")
public class CatalogueAssignment implements Serializable, IdCloneable, ForeignItemKey
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "catalogueItem")
	private Item item;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.EAGER)
	@Index(name = "catalogueAssignment")
	private Catalogue catalogue;

	private boolean blacklisted;

	public boolean isBlacklisted()
	{
		return blacklisted;
	}

	public void setBlacklisted(boolean blacklisted)
	{
		this.blacklisted = blacklisted;
	}

	public Catalogue getCatalogue()
	{
		return catalogue;
	}

	public void setCatalogue(Catalogue catalogue)
	{
		this.catalogue = catalogue;
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

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	@Override
	public long getId()
	{
		return id;
	}
}
