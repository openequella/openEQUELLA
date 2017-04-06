package com.tle.common.payment.storefront.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;

// Friggen comment formatting
/**
 * Used for a few purposes: <br>
 * 1. Easily identify bought items <br>
 * 2. Easily identify items bought by a particular user <br>
 * 3. Check to see if we should harvest an item based on it being deleted on the
 * store front
 * 
 * @author Aaron
 */
@Entity
@AccessType("field")
// WTF? item_uuid and item_version 'not found'??
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames =
// {"institution_id", "item_uuid", "item_version"})})
public class PurchasedContent implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "pcInstitutionIndex")
	private Institution institution;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "pcStoreIndex")
	private Store store;

	@Column(length = 40, nullable = false)
	@Index(name = "pcSrcItemUuidIndex")
	private String sourceItemUuid;

	@Min(0)
	@Index(name = "pcSrcItemVersionIndex")
	private int sourceItemVersion;

	@Column(length = 40, nullable = false)
	@Index(name = "pcItemUuidIndex")
	private String itemUuid;

	@Min(0)
	@Index(name = "pcItemVersionIndex")
	private int itemVersion;

	/**
	 * True if the item was purged and we don't want to harvest it again
	 */
	@Column
	private boolean deleted;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getItemUuid()
	{
		return itemUuid;
	}

	public void setItemUuid(String itemUuid)
	{
		this.itemUuid = itemUuid;
	}

	public int getItemVersion()
	{
		return itemVersion;
	}

	public void setItemVersion(int itemVersion)
	{
		this.itemVersion = itemVersion;
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	public Store getStore()
	{
		return store;
	}

	public void setStore(Store store)
	{
		this.store = store;
	}

	public String getSourceItemUuid()
	{
		return sourceItemUuid;
	}

	public void setSourceItemUuid(String sourceItemUuid)
	{
		this.sourceItemUuid = sourceItemUuid;
	}

	public int getSourceItemVersion()
	{
		return sourceItemVersion;
	}

	public void setSourceItemVersion(int sourceItemVersion)
	{
		this.sourceItemVersion = sourceItemVersion;
	}
}
