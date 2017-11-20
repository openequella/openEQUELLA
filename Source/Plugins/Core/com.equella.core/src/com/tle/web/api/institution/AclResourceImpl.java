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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.beans.security.AccessEntry;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.AclService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.acl.interfaces.AclResource;
import com.tle.web.api.interfaces.beans.security.TargetListBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;

/**
 * @author larry
 */
@Bind(AclResource.class)
@Singleton
public class AclResourceImpl implements AclResource
{
	@Inject
	private AclService aclService;
	@Inject
	private TLEAclManager aclManager;

	@Override
	public TargetListBean getEntries()
	{
		return doGetEntries();
	}

	@Override
	public Response setEntries(TargetListBean bean)
	{
		TargetList targetList = getTargetList(bean);
		if( targetList != null )
		{
			doSetEntries(targetList);
			return Response.ok().build();
		}
		return Response.status(Status.BAD_REQUEST).build();
	}

	protected TargetListBean doGetEntries()
	{
		if( aclManager.filterNonGrantedPrivileges("VIEW_SECURITY_TREE", "EDIT_SECURITY_TREE").isEmpty() )
		{
			throw new AccessDeniedException("One of [VIEW_SECURITY_TREE, EDIT_SECURITY_TREE] is required");
		}

		TargetListBean targetListBean = new TargetListBean();
		List<AccessEntry> allAcls = aclService.listAll();
		List<TargetListEntryBean> tBeanList = Lists.newArrayList();
		for( AccessEntry ae : allAcls )
		{
			TargetListEntryBean tBean = new TargetListEntryBean();
			boolean granted = Character.toUpperCase(ae.isGrantRevoke()) == 'G';
			tBean.setGranted(granted);
			tBean.setOverride(ae.getExpression().isDynamic());
			tBean.setPrivilege(ae.getPrivilege());
			tBean.setWho(ae.getExpression().getExpression());
			tBeanList.add(tBean);
		}
		targetListBean.setEntries(tBeanList);

		return targetListBean;
	}

	protected void doSetEntries(TargetList targetList)
	{
		if( aclManager.filterNonGrantedPrivileges("EDIT_SECURITY_TREE").isEmpty() )
		{
			throw new AccessDeniedException("One of [EDIT_SECURITY_TREE] is required");
		}
		aclManager.setTargetList(Node.INSTITUTION, null, targetList);
	}

	// FIXME: mostly copied and pasted from AbsractBaseEntityEditor
	@Nullable
	protected TargetList getTargetList(@Nullable TargetListBean bean)
	{
		if( bean == null )
		{
			return null;
		}
		final TargetList tl = new TargetList();

		tl.setPartial(false);

		final List<TargetListEntry> tles = new ArrayList<>();
		tl.setEntries(tles);
		final List<TargetListEntryBean> rules = bean.getEntries();
		if( rules != null )
		{
			for( TargetListEntryBean rule : rules )
			{
				TargetListEntry tle = new TargetListEntry(rule.isGranted(), rule.isOverride(), rule.getPrivilege(),
					rule.getWho());
				tles.add(tle);
			}
		}
		return tl;
	}
}
