package com.tle.common.customlinks.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.i18n.CurrentLocale;

@Entity
@AccessType("field")
public class CustomLink extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	@Lob
	private String url;

	@Column(nullable = false)
	private int order;

	public CustomLink()
	{
		super();
	}

	public CustomLink(long id)
	{
		setId(id);
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public int getOrder()
	{
		return order;
	}

	@Override
	@SuppressWarnings("nls")
	public String toString()
	{
		return CurrentLocale.get(getName(), "") + " - " + url;
	}
}
