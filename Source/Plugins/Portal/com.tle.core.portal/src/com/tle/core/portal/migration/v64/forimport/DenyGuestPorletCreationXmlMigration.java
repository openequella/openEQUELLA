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

package com.tle.core.portal.migration.v64.forimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

/**
 * @author Aaron
 *
 */
@Bind
@Singleton
public class DenyGuestPorletCreationXmlMigration implements PostReadMigrator<AclPostReadMigratorParams>
{
	private static final String LOGGED_IN_USER_ROLE_EXPRESSION = SecurityConstants.getRecipient(Recipient.ROLE,
		SecurityConstants.LOGGED_IN_USER_ROLE_ID);
	private static final String EVERYONE_EXPRESSION = SecurityConstants.getRecipient(Recipient.EVERYONE);

	@Inject
	private AccessExpressionDao accessExpressionDao;

	@Override
	public void migrate(AclPostReadMigratorParams acls) throws IOException
	{
		AccessExpression expression = null;
		final Iterator<AccessEntry> iterator = acls.iterator();
		final List<AccessEntry> additions = new ArrayList<>();
		while( iterator.hasNext() )
		{
			final AccessEntry entry = iterator.next();
			if( entry.getPrivilege().equals("CREATE_PORTLET") && entry.getTargetObject().equals("*") )
			{
				final Long exprId = entry.getExpression().getId();
				final AccessExpression expr = acls.getExpressionsFromXml().get(exprId);
				if( expr != null && expr.getExpression().trim().equals(EVERYONE_EXPRESSION) )
				{
					if( expression == null )
					{
						expression = accessExpressionDao.retrieveOrCreate(LOGGED_IN_USER_ROLE_EXPRESSION);
					}
					// remove this entry from XML entries and push into new list so that expression is not attempted 
					// to be mapped to an XML expression
					iterator.remove();
					additions.add(entry);
					entry.setExpression(expression);
				}
			}
		}
		for( AccessEntry addition : additions )
		{
			acls.addAdditionalEntry(addition);
		}
	}
}
