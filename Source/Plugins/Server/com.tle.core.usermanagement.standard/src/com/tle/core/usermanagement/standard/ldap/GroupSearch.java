/*
 * Created on Mar 1, 2005
 */
package com.tle.core.usermanagement.standard.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.common.Check;
import com.tle.core.usermanagement.standard.ldap.LDAP.AndFilter;
import com.tle.core.usermanagement.standard.ldap.LDAP.Filter;
import com.tle.core.usermanagement.standard.ldap.LDAP.GroupBeanHitsCollector;
import com.tle.core.usermanagement.standard.ldap.LDAP.LDAPResult;
import com.tle.core.usermanagement.standard.ldap.LDAP.SingleFilter;

public abstract class GroupSearch
{
	protected LDAP ldap;

	public GroupSearch(LDAP ldap)
	{
		this.ldap = ldap;
	}

	public abstract List<GroupBean> getGroupsContainingUser(DirContext ctx, String userID);

	public abstract List<UserBean> getUsersInGroup(DirContext ctx, String query, String groupID, boolean recurse);

	public abstract List<GroupBean> searchGroupsInGroup(DirContext ctx, String query, String groupID, boolean recurse);

	public abstract GroupBean getParentGroupForGroup(DirContext ctx, String groupID);

	public List<GroupBean> getGroupsFromNames(DirContext ctx, Collection<Name> ldapgroupnames)
	{
		List<GroupBean> alist = new ArrayList<GroupBean>();

		for( Name groupname : ldapgroupnames )
		{
			Attributes gattr = ldap.getAttributes(ctx, groupname, ldap.getGroupAttributes());
			GroupBean gbean = ldap.getGroupBeanFromResult(new LDAPResult(groupname, gattr));
			if( gbean != null )
			{
				alist.add(gbean);
			}
		}
		return alist;
	}

	@SuppressWarnings("nls")
	public List<GroupBean> search(DirContext ctx, String query)
	{
		if( Check.isEmpty(ldap.getGroupObject()) )
		{
			return null;
		}

		SingleFilter nv1 = new SingleFilter(LDAP.OBJECTCLASS, ldap.getGroupObject());
		nv1.setLimit(ldap.config.getSearchLimit());

		Filter f = nv1;
		if( query.length() > 0 && !query.equals("*") )
		{
			SingleFilter nv2 = new SingleFilter(ldap.getGroupNameField(), query, false);
			f = new AndFilter(nv1, nv2);
		}

		return ldap.searchAllBases(ctx, f, new GroupBeanHitsCollector(), true);
	}
}
