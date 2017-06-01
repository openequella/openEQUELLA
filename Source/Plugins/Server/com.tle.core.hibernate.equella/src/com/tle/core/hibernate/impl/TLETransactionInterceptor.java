/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
