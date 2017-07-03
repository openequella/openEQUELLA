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

package com.tle.web.remoting.rest.resource;

import java.io.IOException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.jboss.resteasy.core.ResourceMethodInvoker;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.Institutional;

/**
 * An invoking class that is annotated with
 * Institutional.Type.NON_INSTITUTIONAL, requires that the CurrentInstitution be
 * set by the InstitutionFilter.
 * 
 * @author (stolen from) EPS
 */
@Bind
@Singleton
public class InstitutionSecurityFilter implements ContainerRequestFilter
{

	@SuppressWarnings("nls")
	@Override
	public void filter(ContainerRequestContext context) throws IOException
	{
		ResourceMethodInvoker invoker = (ResourceMethodInvoker) context.getProperty(ResourceMethodInvoker.class
			.getName());
		Class<?> clazz = invoker.getResourceClass();
		Institutional.Type instType = Institutional.Type.INSTITUTIONAL;
		Institutional instanno = clazz.getAnnotation(Institutional.class);
		if( instanno != null )
		{
			instType = instanno.value();
		}
		boolean hasInst = CurrentInstitution.get() != null;
		if( instType != Institutional.Type.BOTH && (hasInst != (instType == Institutional.Type.INSTITUTIONAL)) )
		{
			throw new NotFoundException();
		}
		// Class or calling method system restricted?
		SecureOnCallSystem system = clazz.getAnnotation(SecureOnCallSystem.class);
		if( system == null )
		{
			system = invoker.getMethod().getAnnotation(SecureOnCallSystem.class);
		}
		if( system != null && !CurrentUser.getUserState().isSystem() )
		{
			throw new AccessDeniedException("You do not have the privileges to access this endpoint");
		}
	}

}
