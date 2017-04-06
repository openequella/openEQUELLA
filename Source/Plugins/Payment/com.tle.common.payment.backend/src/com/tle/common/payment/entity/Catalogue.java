package com.tle.common.payment.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.google.common.collect.Sets;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.DynaCollection;

@SuppressWarnings("nls")
@Entity
@AccessType("field")
public class Catalogue extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	public static final String ENTITY_TYPE = "CATALOGUE";

	private boolean regionRestricted;

	@ManyToMany(fetch = FetchType.LAZY)
	private Set<Region> regions = Sets.newHashSet();

	@ManyToOne
	@Index(name = "cat_dyncol")
	private DynaCollection dynamicCollection;

	public boolean isRegionRestricted()
	{
		return regionRestricted;
	}

	public void setRegionRestricted(boolean regionRestricted)
	{
		this.regionRestricted = regionRestricted;
	}

	public Set<Region> getRegions()
	{
		return regions;
	}

	public void setRegions(Set<Region> regions)
	{
		this.regions = regions;
	}

	public DynaCollection getDynamicCollection()
	{
		return dynamicCollection;
	}

	public void setDynamicCollection(DynaCollection dynamicCollection)
	{
		this.dynamicCollection = dynamicCollection;
	}
}
