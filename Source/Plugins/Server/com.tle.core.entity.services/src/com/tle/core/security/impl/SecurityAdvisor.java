package com.tle.core.security.impl;

import java.lang.reflect.Method;

import javax.inject.Inject;

import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

public class SecurityAdvisor extends StaticMethodMatcherPointcutAdvisor
{
	@Inject
	private SecurityAttributeSource attributeSource;

	@Override
	public boolean matches(Method method, @SuppressWarnings("rawtypes") Class targetClass)
	{
		return attributeSource.getAttribute(method, targetClass) != null;
	}

}
