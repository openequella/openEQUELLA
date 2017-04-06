package com.tle.core.usermanagement.leap.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.valuebean.DefaultGroupBean;
import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.edge.common.valuebean.RoleBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.leap.wrapper.LeapUserWrapperSettings;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.core.services.HttpService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.user.DefaultUserState;
import com.tle.core.user.ModifiableUserState;
import com.tle.plugins.ump.AbstractUserDirectory;
import com.tle.plugins.ump.UserDirectoryUtils;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class LeapUserWrapper extends AbstractUserDirectory
{
	private static final Logger LOGGER = Logger.getLogger(LeapUserWrapper.class);

	private static final String PARAM_USERID = "userId";
	private static final String PARAM_USERNAME = "loginId";
	private static final String PARAM_METHOD = "method";
	private static final String PARAM_SUB_METHOD = "asMethod";

	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;

	private String endpointUrl;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		// http://acs-dev.ilongman.com/acs-web/App/ACSGateway.do
		endpointUrl = ((LeapUserWrapperSettings) settings).getEndpointUrl();
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Endpoint: " + endpointUrl);
		}
		return false;
	}

	private PropBagEx invoke(String method, String asMethod, String... params)
	{
		final Request request = new Request(endpointUrl);
		for( int i = 0; i < params.length; i += 2 )
		{
			request.addParameter(params[i], params[i + 1]);
		}
		request.addParameter(PARAM_METHOD, method);
		if( !Check.isEmpty(asMethod) )
		{
			request.addParameter(PARAM_SUB_METHOD, asMethod);
		}

		try( Response response = httpService.getWebContent(request, configService.getProxyDetails()) )
		{
			if( !response.isOk() )
			{
				LOGGER.fatal("Could not invoke LEAP webservice: " + response.getCode() + " " + response.getMessage());
				return null;
			}
			return new PropBagEx(response.getBody());
		}
		catch( Exception message )
		{
			LOGGER.fatal(message.getMessage());
			return null;
		}
	}

	/**
	 * Authenticates a user from a username.
	 * 
	 * @param userId Note! User ID, not username!
	 * @return information about the currently logged in user
	 */
	@Override
	public ModifiableUserState authenticateUserFromUsername(String userId, String privateData)
	{
		// if we can get any details on this user from LEAP then it's all good
		// (users have to jump through hoops to get access to an external
		// LMS, that LMS passes us a token so we can safely get any user we
		// want)

		final UserBean bean = getInformationForUser(userId);
		if( bean == null )
		{
			return null;
		}

		DefaultUserState state = new DefaultUserState();
		state.setLoggedInUser(bean);
		return state;
	}

	/**
	 * Called after successful user authentication to setup various extra
	 * UserState variables, such as Roles.
	 * 
	 * @return
	 */
	@Override
	public void initUserState(ModifiableUserState state)
	{
		final String userId = state.getUserBean().getUniqueID();

		Set<String> roles = state.getUsersRoles();
		Pair<ChainResult, Collection<RoleBean>> rolesForUser = getRolesForUser(userId);
		if( rolesForUser != null )
		{
			for( RoleBean b : rolesForUser.getSecond() )
			{
				roles.add(b.getUniqueID());
			}
		}

		Set<String> groups = state.getUsersGroups();
		for( GroupBean group : getGroupBeansContainingUser(userId) )
		{
			groups.add(group.getUniqueID());
		}
	}

	/**
	 * Retrieve basic information regarding a user ID.
	 * 
	 * @param userID the user ID to query
	 * @return a UserBean object corresponding to the userID parameter
	 */
	@Override
	public UserBean getInformationForUser(final String userId)
	{
		final PropBagEx xml = invoke("getProfile", "get", /* PARAM_USERID */PARAM_USERNAME, userId);

		if( xml == null )
		{
			return null;
		}

		final PropBagEx userXml = xml.getSubtree("Profile");

		if( userXml != null && userXml.nodeExists("UserID") )
		{
			return new DefaultUserBean(userXml.getNode("UserID"), userXml.getNode("LoginID"),
				userXml.getNode("ChiName"), userXml.getNode("Name"), userXml.getNode("Email"));
		}
		return null;
	}

	/**
	 * Retrieve basic information regarding a group ID.
	 */
	@Override
	public GroupBean getInformationForGroup(final String groupID)
	{
		final PropBagEx xml = invoke("getServices", "getService", "serviceCode", groupID);

		if( xml == null )
		{
			return null;
		}

		final PropBagEx groupXml = xml.getSubtree("Services/Service");
		if( groupXml == null )
		{
			return null;
		}

		return new DefaultGroupBean(groupXml.getNode("ServiceCode"), groupXml.getNode("Name"));
	}

	private List<GroupBean> getAllGroups()
	{
		return null;

		// LEAP getAllServices endpoint returns incomplete XML. Dodgy.
		// final List<GroupBean> groups = Lists.newArrayList();
		// final PropBagEx xml = invoke("getServices", "getAllServices");
		// for( PropBagEx groupXml : xml.iterateAll("Services/Service") )
		// {
		// groups.add(new DefaultGroupBean(groupXml.getNode("ServiceCode"),
		// groupXml.getNode("Name")));
		// }
		// return groups;
	}

	@Override
	public Map<String, GroupBean> getInformationForGroups(Collection<String> groupIds)
	{
		return UserDirectoryUtils.getMultipleGroupInfosFromSingleInfos(this, groupIds);
	}

	/**
	 * Retrieve the groups for the specified user.
	 * 
	 * @param userID the user to query
	 * @return a group
	 */
	@Override
	public Pair<ChainResult, Collection<GroupBean>> getGroupsContainingUser(String userId)
	{
		return new Pair<ChainResult, Collection<GroupBean>>(ChainResult.CONTINUE, getGroupBeansContainingUser(userId));
	}

	private Collection<GroupBean> getGroupBeansContainingUser(String userId)
	{
		final PropBagEx xml = invoke("getServices", "getServicesByUser", PARAM_USERID, userId);
		final List<GroupBean> groups = new ArrayList<GroupBean>();

		if( xml == null )
		{
			return groups;
		}

		for( PropBagEx service : xml.iterateAll("Services/Service") )
		{
			groups.add(new DefaultGroupBean(service.getNode("ServiceCode"), service.getNode("Name")));
		}
		return groups;
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(String query)
	{
		// exact username match only i.e. query MUST be a username
		String q = query;
		// need to un-adorn the query
		if( q.startsWith("*") )
		{
			q = q.substring(1);
		}

		if( q.endsWith("*") )
		{
			q = Utils.safeSubstring(q, 0, -1);
		}

		final UserBean user = getInformationForUsername(q);
		if( user == null )
		{
			return null;
		}

		return new Pair<ChainResult, Collection<UserBean>>(ChainResult.CONTINUE, Collections.singleton(user));
	}

	@Override
	public Pair<ChainResult, Collection<UserBean>> searchUsers(final String query, String parentGroupID,
		boolean recursive)
	{
		// parentGroupID is ignored since we are not supporting groups
		return searchUsers(query);
	}

	/**
	 * Used by search
	 * 
	 * @param username
	 * @return
	 */
	private UserBean getInformationForUsername(final String username)
	{
		final PropBagEx xml = invoke("getProfile", "get", PARAM_USERNAME, username);

		if( xml == null )
		{
			return null;
		}

		final PropBagEx userXml = xml.getSubtree("Profile");

		if( userXml != null && userXml.nodeExists("UserID") )
		{
			return new DefaultUserBean(userXml.getNode("UserID"), userXml.getNode("LoginID"),
				userXml.getNode("ChiName"), userXml.getNode("Name"), userXml.getNode("Email"));
		}
		return null;
	}

	/**
	 * Perform a free-text search for groups deemed relevant by the external
	 * system.
	 * 
	 * @param query a string representing the free-text query.
	 * @return a collection of String objects matching the query
	 */
	@Override
	public Collection<GroupBean> searchGroups(final String query)
	{
		// exact group ID match only i.e. query MUST be a group ID

		String q = query;
		// need to un-adorn the query
		if( q.startsWith("*") )
		{
			q = q.substring(1);
		}
		if( q.endsWith("*") )
		{
			q = Utils.safeSubstring(q, 0, -1);
		}
		if( q.isEmpty() )
		{
			return getAllGroups();
		}

		final GroupBean group = getInformationForGroup(q);
		if( group == null )
		{
			return null;
		}

		return Collections.singleton(group);
	}
}
