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

package com.tle.web.api.institution;

import com.google.common.collect.Lists;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.api.acl.interfaces.AclResource;
import com.tle.web.api.interfaces.beans.security.TargetListBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * @author doolse
 */
@Bind(AclResource.class)
@Singleton
public class AclResourceImpl implements AclResource
{
	@Inject
	private TLEAclManager aclManager;

	@Override
	public TargetListBean getEntries()
	{
		checkPrivs("VIEW_SECURITY_TREE", "EDIT_SECURITY_TREE");
		TargetListBean targetListBean = new TargetListBean();
		TargetList allAcls = aclManager.getTargetList(Node.INSTITUTION, null);
		List<TargetListEntryBean> tBeanList = Lists.newArrayList();
		for( TargetListEntry ae : allAcls.getEntries() )
		{
			TargetListEntryBean tBean = new TargetListEntryBean();
			tBean.setGranted(ae.isGranted());
			tBean.setOverride(ae.isOverride());
			tBean.setPrivilege(ae.getPrivilege());
			tBean.setWho(ae.getWho());
			tBeanList.add(tBean);
		}
		targetListBean.setEntries(tBeanList);

		return targetListBean;
	}

	public void checkPrivs(String... privs)
	{
		if( aclManager.filterNonGrantedPrivileges(privs).isEmpty() )
		{
			throw new PrivilegeRequiredException(privs);
		}
	}
	@Override
	public Response setEntries(TargetListBean bean)
	{
		checkPrivs("EDIT_SECURITY_TREE");
		List<TargetListEntry> tle = new ArrayList<>();
		for (TargetListEntryBean eb : bean.getEntries())
		{
			tle.add(new TargetListEntry(eb.isGranted(), eb.isOverride(), eb.getPrivilege(), eb.getWho()));
		}
		aclManager.setTargetList(Node.INSTITUTION, null, new TargetList(tle));
		return Response.status(Status.OK).build();
	}

}
