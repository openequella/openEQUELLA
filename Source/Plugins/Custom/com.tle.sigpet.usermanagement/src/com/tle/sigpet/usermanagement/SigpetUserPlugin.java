/*
 * Created on Mar 8, 2005
 */
package com.tle.sigpet.usermanagement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import lcmswebsvc.ArrayOfString;
import lcmswebsvc.LcmsWebService;
import lcmswebsvc.LcmsWebServiceSoap;

import com.dytech.edge.common.valuebean.DefaultGroupBean;
import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.google.common.collect.Maps;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.DefaultUserState;
import com.tle.core.user.ModifiableUserState;
import com.tle.plugins.ump.AbstractUserDirectory;
import com.tle.plugins.ump.UserDirectoryUtils;

@Bind
@Singleton
public class SigpetUserPlugin extends AbstractUserDirectory
{
	private LcmsWebServiceSoap soap;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		try
		{

			soap = new LcmsWebService(getClass().getResource("sigpet.wsdl")).getLcmsWebServiceSoap();
		}
		catch( Exception ex )
		{
			throw new RuntimeException("Could not get soap service: " + ex.getMessage());
		}

		return false;
	}

	@Override
	public ModifiableUserState authenticateUser(final String username, final String password)
	{
		return convert(checkAuthentication(soap.authenticateUser(username, password)));
	}

	@Override
	public ModifiableUserState authenticateUserFromUsername(final String username, final String privateData)
	{
		return convert(checkAuthentication(soap.authenticateUserFromUsername(username, privateData)));
	}

	@Override
	public void initUserState(ModifiableUserState auth)
	{
		UserBean userBean = auth.getUserBean();
		for( GroupBean gb : getGroupsContainingUser(userBean, userBean.getUniqueID()) )
		{
			auth.getUsersGroups().add(gb.getUniqueID());
		}
	}

	@Override
	public UserBean getInformationForUser(final String userId)
	{
		return UserDirectoryUtils.getSingleUserInfoFromMultipleInfo(this, userId);
	}

	@Override
	public Map<String, UserBean> getInformationForUsers(final Collection<String> userIds)
	{
		String[] sids = userIds.toArray(new String[userIds.size()]);
		List<lcmswebsvc.UserBean> lcmsubs = soap.getInformationForUsers(convertCurrentUser(), arrayOfString(sids))
			.getUserBean();
		if( Check.isEmpty(lcmsubs) )
		{
			return null;
		}

		Map<String, UserBean> rv = Maps.newHashMapWithExpectedSize(lcmsubs.size());
		for( lcmswebsvc.UserBean lcmsub : lcmsubs )
		{
			UserBean ub = convert(lcmsub);
			if( ub != null )
			{
				rv.put(ub.getUniqueID(), ub);
			}
		}
		return rv;
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> getUsersForGroup(String groupId, boolean recursive)
	{
		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, getUsersInGroup(groupId, recursive));
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query)
	{
		List<UserBean> rv = convert(soap.searchUsers(convertCurrentUser(), processQuery(query)).getUserBean());
		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, rv);
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(final String query, final String parentGroupID,
		final boolean recursive)
	{
		// Sigpet doesn't allow querying with a group ID
		if( !Check.isEmpty(parentGroupID) )
		{
			return getUsersForGroup(parentGroupID, recursive);
		}
		else
		{
			return searchUsers(query);
		}
	}

	@Override
	public Collection<GroupBean> searchGroups(final String query)
	{
		return convertGroups(soap.searchGroups(convertCurrentUser(), processQuery(query)).getGroupBean());
	}

	@Override
	public GroupBean getInformationForGroup(final String groupId)
	{
		return UserDirectoryUtils.getSingleGroupInfoFromMultipleInfo(this, groupId);
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(final Collection<String> groupIds)
	{
		String[] sids = groupIds.toArray(new String[groupIds.size()]);
		List<lcmswebsvc.GroupBean> lcmsgbs = soap.getInformationForGroups(convertCurrentUser(), arrayOfString(sids))
			.getGroupBean();
		Map<String, GroupBean> rv = Maps.newHashMapWithExpectedSize(groupIds.size());
		for( lcmswebsvc.GroupBean lcmsgb : lcmsgbs )
		{
			GroupBean gb = convert(lcmsgb);
			if( gb != null )
			{
				rv.put(gb.getUniqueID(), gb);
			}
		}
		return rv;
	}

	private static ArrayOfString arrayOfString(String[] sids)
	{
		ArrayOfString array = new ArrayOfString();
		List<String> strings = array.getString();
		for( String string : sids )
		{
			strings.add(string);
		}
		return array;
	}

	List<UserBean> getUsersInGroup(String groupID, @SuppressWarnings("unused") boolean recurse)
	{
		return convert(soap.getUsersInGroup(convertCurrentUser(), groupID).getUserBean());
	}

	@Override
	public Pair<ChainResult, Collection<GroupBean>> getGroupsContainingUser(final String userID)
	{
		return new Pair<ChainResult, Collection<GroupBean>>(ChainResult.CONTINUE, getGroupsContainingUser(
			CurrentUser.getDetails(), userID));
	}

	private List<GroupBean> getGroupsContainingUser(UserBean callingUser, String userID)
	{
		return convertGroups(soap.getGroupsContainingUser(convertToUserState(callingUser), userID).getGroupBean());
	}

	private lcmswebsvc.UserState checkAuthentication(lcmswebsvc.UserState spus)
	{
		if( spus.getUb() != null )
		{
			String id = spus.getUb().getUniqueID();
			if( id != null && !id.equals("null") )
			{
				return spus;
			}
		}
		return null;
	}

	private List<UserBean> convert(Iterable<lcmswebsvc.UserBean> users)
	{
		if( users == null )
		{
			return null;
		}

		List<UserBean> results = new ArrayList<UserBean>();
		for( lcmswebsvc.UserBean userBean : users )
		{
			UserBean ub = convert(userBean);
			if( ub != null )
			{
				results.add(ub);
			}
		}
		return results;
	}

	private List<GroupBean> convertGroups(Iterable<lcmswebsvc.GroupBean> groups)
	{
		if( groups == null )
		{
			return null;
		}

		List<GroupBean> results = new ArrayList<GroupBean>();
		for( lcmswebsvc.GroupBean groupBean : groups )
		{
			if( groupBean != null )
			{
				results.add(convert(groupBean));
			}
		}
		return results;
	}

	private ModifiableUserState convert(lcmswebsvc.UserState spus)
	{
		if( spus == null )
		{
			return null;
		}

		DefaultUserState state = new DefaultUserState();
		state.setLoggedInUser(convert(spus.getUb()));
		return state;
	}

	private UserBean convert(lcmswebsvc.UserBean spub)
	{
		final String uid = spub.getUniqueID();
		if( Check.isEmpty(uid) || "null".equals(uid) ) //$NON-NLS-1$
		{
			return null;
		}
		else
		{
			return new DefaultUserBean(uid, spub.getUsername(), spub.getFirstName(), spub.getLastName(),
				spub.getEmailAddress());
		}
	}

	private GroupBean convert(lcmswebsvc.GroupBean spgb)
	{
		if( spgb == null )
		{
			return null;
		}

		final String uid = spgb.getUniqueID();
		if( Check.isEmpty(uid) || "null".equals(uid) ) //$NON-NLS-1$
		{
			return null;
		}
		else
		{
			return new DefaultGroupBean(uid, spgb.getName());
		}
	}

	private lcmswebsvc.UserState convertCurrentUser()
	{
		return convertToUserState(CurrentUser.getDetails());
	}

	private lcmswebsvc.UserState convertToUserState(UserBean userBean)
	{
		lcmswebsvc.UserState spus = new lcmswebsvc.UserState();
		spus.setUb(convertToUserBean(userBean));
		return spus;
	}

	private lcmswebsvc.UserBean convertToUserBean(UserBean bean)
	{
		lcmswebsvc.UserBean spub = new lcmswebsvc.UserBean();
		spub.setUniqueID(bean.getUniqueID());
		spub.setUsername(bean.getUsername());
		spub.setFirstName(bean.getFirstName());
		spub.setLastName(bean.getLastName());
		spub.setEmailAddress(bean.getEmailAddress());
		return spub;
	}

	private String processQuery(String query)
	{
		return query.replaceAll("\\*", "");
	}
}
