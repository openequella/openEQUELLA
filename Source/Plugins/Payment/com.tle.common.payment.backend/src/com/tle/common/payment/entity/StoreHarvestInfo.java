package com.tle.common.payment.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

/**
 * @author Aaron
 */
@Entity
@AccessType("field")
public class StoreHarvestInfo
{
	/**
	 * For criteria wanting a column name to search on
	 */
	public static final String ITEM_CRITERIA = "itemUuid";

	/**
	 * For criteria wanting a column name to sort on
	 */
	public static final String SORTABLE_DATE = "date";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	/**
	 * You can't harvest content without an associated Sale, even if that Sale
	 * is free
	 */
	@ManyToOne(optional = false)
	@Index(name = "shi_sale")
	private Sale sale;

	@Column(length = 40, nullable = false)
	@Index(name = "harvestinfoItemIndex")
	private String itemUuid;

	@Column(length = 40, nullable = true)
	private String attachmentUuid;

	@Column(nullable = false)
	private Date date;

	@Column(nullable = false)
	private int version;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Sale getSale()
	{
		return sale;
	}

	public void setSale(Sale sale)
	{
		this.sale = sale;
	}

	public String getItemUuid()
	{
		return itemUuid;
	}

	public void setItemUuid(String itemUuid)
	{
		this.itemUuid = itemUuid;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getAttachmentUuid()
	{
		return attachmentUuid;
	}

	public void setAttachmentUuid(String attachmentUuid)
	{
		this.attachmentUuid = attachmentUuid;
	}
}
