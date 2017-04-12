package com.tle.core.hibernate;

import org.hibernate.SessionFactory;

public interface HibernateService
{
	SessionFactory getTransactionAwareSessionFactory(String name, boolean system);
}
