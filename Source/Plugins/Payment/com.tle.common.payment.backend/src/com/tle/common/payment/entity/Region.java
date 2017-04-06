package com.tle.common.payment.entity;

import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public class Region extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@ElementCollection(fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinTable(name = "region_countries")
	private Set<String> countries;

	public Set<String> getCountries()
	{
		return countries;
	}

	public void setCountries(Set<String> countries)
	{
		this.countries = countries;
	}

}
