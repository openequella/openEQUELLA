/*
 * Created on Jun 28, 2005
 */

package com.tle.beans.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
public class FederatedSearch extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Column(length = 64)
	private String type;
	private int timeout;

	private String collectionUuid;

	@Lob
	private String advancedSearchFields;

	public String getAdvancedSearchFields()
	{
		return advancedSearchFields;
	}

	public void setAdvancedSearchFields(String advancedSearchFields)
	{
		this.advancedSearchFields = advancedSearchFields;
	}

	public FederatedSearch()
	{
		timeout = 30;
	}

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getCollectionUuid()
	{
		return collectionUuid;
	}

	public void setCollectionUuid(String collectionUuid)
	{
		this.collectionUuid = collectionUuid;
	}
}
