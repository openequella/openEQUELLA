package com.tle.plugins.ump;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.edge.common.valuebean.RoleBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.core.user.ModifiableUserState;
import com.tle.core.user.UserState;

/**
 * This interface has many similarities with {@link UserDirectory}, but with a
 * couple of differences.
 * <ol>
 * <li>Some method results are just lists rather than pair containing a chain
 * control value and list. The chain control stuff is only interesting to the
 * chain handler itself, not to other objects using the chain.</li>
 * <li>Unlike UserDirectory implementations, any returned lists or maps from the
 * chain must not be null.</li>
 * </ol>
 * 
 * @author nick
 */
public interface UserDirectoryChain
{
	void purgeFromCaches(String id);
	
	void purgeGroupFromCaches(String groupId);

	ModifiableUserState authenticateToken(String token);

	ModifiableUserState authenticateUser(String username, String password);

	ModifiableUserState authenticateUserFromUsername(String username, String privateData);

	ModifiableUserState authenticateRequest(HttpServletRequest request);

	void close() throws Exception;

	String getGeneratedToken(String secretId, String username);

	List<GroupBean> getGroupsContainingUser(String userId);

	List<UserBean> getUsersInGroup(String groupId, boolean recursive);

	GroupBean getInformationForGroup(String groupId);

	Map<String, GroupBean> getInformationForGroups(Collection<String> groupIds);

	RoleBean getInformationForRole(String roleId);

	Map<String, RoleBean> getInformationForRoles(Collection<String> roleIds);

	UserBean getInformationForUser(String userId);

	Map<String, UserBean> getInformationForUsers(Collection<String> userIds);

	GroupBean getParentGroupForGroup(String groupId);

	List<RoleBean> getRolesForUser(String userId);

	List<String> getTokenSecretIds();

	void initGuestUserState(ModifiableUserState state);

	void initUserState(ModifiableUserState state);

	void initSystemUserState(ModifiableUserState state);

	void keepAlive();

	void logout(UserState state);

	List<GroupBean> searchGroups(String query);

	List<GroupBean> searchGroups(String query, String parentId);

	List<RoleBean> searchRoles(String query);

	List<UserBean> searchUsers(String query);

	List<UserBean> searchUsers(String query, String parentGroupId, boolean recursive);

	boolean verifyUserStateForToken(UserState userState, String token);

	void clearUserSearchCache();
}