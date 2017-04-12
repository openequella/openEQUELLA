package com.tle.core.hibernate;

public interface HibernateFactoryService
{
	HibernateFactory createConfiguration(DataSourceHolder dataSource, Class<?>... clazzes);

	Class<?>[] getDomainClasses(String factory);

}
