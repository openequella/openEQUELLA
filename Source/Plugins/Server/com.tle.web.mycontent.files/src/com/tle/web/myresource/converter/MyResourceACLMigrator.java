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

package com.tle.web.myresource.converter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.dao.AclDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.mycontent.MyContentConstants;

@Bind
@Singleton
public class MyResourceACLMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private AccessExpressionDao accessExpressionDao;
	@Inject
	private AclDao aclDao;

	@Override
	@SuppressWarnings("nls")
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		final AccessExpression owner = accessExpressionDao
			.retrieveOrCreate(SecurityConstants.getRecipient(Recipient.OWNER));
		final AccessExpression everyone = accessExpressionDao
			.retrieveOrCreate(SecurityConstants.getRecipient(Recipient.EVERYONE));

		final ItemDefinition collection = itemDefinitionService.getByUuid(MyContentConstants.MY_CONTENT_UUID);
		final String target = "B:" + collection.getId();
		final Institution institution = collection.getInstitution();

		grantPrivilege("CREATE_ITEM", everyone, target, institution);

		grantPrivilege("VIEW_ITEM", owner, target, institution);
		grantPrivilege("DISCOVER_ITEM", owner, target, institution);
		grantPrivilege("EDIT_ITEM", owner, target, institution);
		grantPrivilege("SHARE_ITEM", owner, target, institution);
		grantPrivilege("REASSIGN_OWNERSHIP_ITEM", owner, target, institution);
		grantPrivilege("DELETE_ITEM", owner, target, institution);

		revokePrivilege("NEWVERSION_ITEM", everyone, target, institution);
		revokePrivilege("ARCHIVE_ITEM", everyone, target, institution);
		revokePrivilege("SUSPEND_ITEM", everyone, target, institution);
	}

	private void grantPrivilege(String privilege, AccessExpression expression, String target, Institution institution)
	{
		addEntry(privilege, SecurityConstants.GRANT, expression, target, institution);
	}

	private void revokePrivilege(String privilege, AccessExpression expression, String target, Institution institution)
	{
		addEntry(privilege, SecurityConstants.REVOKE, expression, target, institution);
	}

	private void addEntry(String privilege, char grantRevoke, AccessExpression expression, String target,
		Institution institution)
	{
		AccessEntry newEntry = new AccessEntry();
		newEntry.setGrantRevoke(grantRevoke);
		newEntry.setPrivilege(privilege);
		newEntry.setTargetObject(target);
		newEntry.setAclPriority(SecurityConstants.PRIORITY_COLLECTION);
		newEntry.setAclOrder(0);
		newEntry.setExpression(expression);
		newEntry.setInstitution(institution);

		aclDao.save(newEntry);
		aclDao.flush();
		aclDao.clear();
	}
}
