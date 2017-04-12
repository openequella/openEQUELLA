package com.tle.common.taxonomy;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public class Taxonomy extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Column(length = 100)
	private String dataSourcePluginId;

	public Taxonomy()
	{
		super();
	}

	public Taxonomy(long id)
	{
		setId(id);
	}

	public String getDataSourcePluginId()
	{
		return dataSourcePluginId;
	}

	public void setDataSourcePluginId(String dataSourcePluginId)
	{
		this.dataSourcePluginId = dataSourcePluginId;
	}
}
