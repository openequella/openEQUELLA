package com.tle.core.hibernate.impl;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.google.inject.Provider;

public class TLETransactionInterceptor extends TransactionInterceptor
{
	private static final long serialVersionUID = 1L;
	private AnnotationTransactionAttributeSource attributes;
	private final String factoryName;
	private final boolean system;
	private TLETransactionManager transactionManager;
	private Provider<TLETransactionManager> managerProvider;

	public TLETransactionInterceptor(AnnotationTransactionAttributeSource attributes,
		Provider<TLETransactionManager> managerProvider, String factoryName, boolean system)
	{
		this.attributes = attributes;
		this.factoryName = factoryName;
		this.system = system;
		this.managerProvider = managerProvider;
	}

	@Override
	public TransactionAttributeSource getTransactionAttributeSource()
	{
		return attributes;
	}

	@Override
	public synchronized PlatformTransactionManager getTransactionManager()
	{
		if( transactionManager == null )
		{
			transactionManager = managerProvider.get();
			transactionManager.setFactoryName(factoryName);
			transactionManager.setSystem(system);
		}
		return transactionManager;
	}

}
