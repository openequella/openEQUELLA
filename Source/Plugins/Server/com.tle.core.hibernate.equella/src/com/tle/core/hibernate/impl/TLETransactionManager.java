package com.tle.core.hibernate.impl;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

import com.tle.core.hibernate.HibernateService;

public class TLETransactionManager extends HibernateTransactionManager
{
	private static final long serialVersionUID = 1L;

	@Inject
	private HibernateService hibernateService;

	private String factoryName;
	private boolean system;

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public boolean isSystem()
	{
		return system;
	}

	public void setSystem(boolean system)
	{
		this.system = system;
	}

	@Override
	public SessionFactory getSessionFactory()
	{
		return hibernateService.getTransactionAwareSessionFactory(getFactoryName(), isSystem());
	}
}
