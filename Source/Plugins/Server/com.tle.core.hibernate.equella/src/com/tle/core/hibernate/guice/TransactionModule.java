package com.tle.core.hibernate.guice;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.tle.core.hibernate.SystemDatabase;
import com.tle.core.hibernate.impl.TLETransactionInterceptor;
import com.tle.core.hibernate.impl.TLETransactionManager;

public class TransactionModule extends AbstractModule
{
	private static AnnotationTransactionAttributeSource attributeSource = new AnnotationTransactionAttributeSource(
		false);
	private static TLETransactionInterceptor mainInterceptor;
	private static TLETransactionInterceptor systemInterceptor;
	private static TransactionMethodMatcher transactionMatcher = new TransactionMethodMatcher();

	@Override
	protected void configure()
	{
		bind(TLETransactionManager.class);
		bindInterceptor(Matchers.not(Matchers.annotatedWith(SystemDatabase.class)), transactionMatcher,
			getMainInterceptor());
		bindInterceptor(Matchers.annotatedWith(SystemDatabase.class), transactionMatcher, getSystemInterceptor());
	}

	@SuppressWarnings("nls")
	private synchronized MethodInterceptor getMainInterceptor()
	{
		if( mainInterceptor == null )
		{
			mainInterceptor = new TLETransactionInterceptor(attributeSource, getProvider(TLETransactionManager.class),
				"main", false);
		}
		return mainInterceptor;
	}

	@SuppressWarnings("nls")
	private synchronized MethodInterceptor getSystemInterceptor()
	{
		if( systemInterceptor == null )
		{
			systemInterceptor = new TLETransactionInterceptor(attributeSource,
				getProvider(TLETransactionManager.class), "system", true);
		}
		return systemInterceptor;
	}

	private static class TransactionMethodMatcher extends AbstractMatcher<Method>
	{
		@Override
		public boolean matches(Method t)
		{
			return !t.isSynthetic() && t.isAnnotationPresent(Transactional.class);
		}
	}

}
