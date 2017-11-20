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

package com.tle.core.security.impl;

import static com.tle.common.security.SecurityConstants.GRANT;
import static com.tle.common.security.SecurityConstants.PRIORITY_OBJECT_INSTANCE;
import static com.tle.common.security.SecurityConstants.REVOKE;
import static com.tle.common.security.SecurityConstants.TARGET_EVERYTHING;
import static com.tle.common.security.SecurityConstants.getRecipient;
import static com.tle.common.security.SecurityConstants.Recipient.EVERYONE;
import static com.tle.common.security.SecurityConstants.Recipient.GROUP;
import static com.tle.common.security.SecurityConstants.Recipient.OWNER;
import static com.tle.common.security.SecurityConstants.Recipient.ROLE;
import static com.tle.common.security.SecurityConstants.Recipient.SHARE_PASS;
import static com.tle.common.security.SecurityConstants.Recipient.TOKEN_SECRET_ID;
import static com.tle.common.security.SecurityConstants.Recipient.USER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.common.collections.CombinedCollection;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.security.WorkflowTaskDynamicTarget;
import com.tle.common.security.WorkflowTaskTarget;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.dao.AclDao;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.DomainObjectPrivilegeFilterExtension;
import com.tle.core.security.SecurityPostProcessor;
import com.tle.core.security.SecurityTargetHandler;
import com.tle.core.security.TLEAclManager;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
@Bind(TLEAclManager.class)
@Singleton
public class TLEAclManagerImpl implements TLEAclManager
{
	private static final String OBJECT_PRIORITY_DB_FORMAT = String.format("%04d",
		SecurityConstants.PRIORITY_OBJECT_INSTANCE + SecurityConstants.PRIORITY_MAX);

	@Inject
	private AclDao aclDao;
	@Inject
	private AccessExpressionDao expressionDao;

	private PluginTracker<SecurityTargetHandler> ownerHandlers;
	private PluginTracker<SecurityTargetHandler> labellingHandlers;
	private PluginTracker<SecurityTargetHandler> transformHandlers;
	private PluginTracker<SecurityPostProcessor> postProcessors;
	@Inject
	private PluginTracker<DomainObjectPrivilegeFilterExtension> domainObjectPrivFilters;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		ownerHandlers = new PluginTracker<SecurityTargetHandler>(pluginService, "com.tle.core.security",
			"securityTargetHandler", "handlesOwnershipFor");
		ownerHandlers.setBeanKey("handler");

		labellingHandlers = new PluginTracker<SecurityTargetHandler>(pluginService, "com.tle.core.security",
			"securityTargetHandler", "handlesLabellingFor");
		labellingHandlers.setBeanKey("handler");

		transformHandlers = new PluginTracker<SecurityTargetHandler>(pluginService, "com.tle.core.security",
			"securityTargetHandler", "handlesTransformationOf");
		transformHandlers.setBeanKey("handler");

		postProcessors = new PluginTracker<SecurityPostProcessor>(pluginService, "com.tle.core.security", "securityPostProcessor",
			null).setBeanKey("bean");
	}

	@Override
	@Transactional
	public <T> Collection<T> filterNonGrantedObjects(Collection<String> privileges, Collection<T> domainObjs)
	{
		if( CurrentUser.getUserState().isSystem() )
		{
			return domainObjs;
		}

		Map<T, Map<String, Boolean>> objectToPrivileges = getObjectToPrivileges(privileges, domainObjs);

		// Construct collection with only granted domain objects, retaining the
		// original ordering.
		Collection<T> results;
		if( domainObjs instanceof Set<?> )
		{
			results = new HashSet<T>();
		}
		else
		{
			results = new ArrayList<T>();
		}

		for( T obj : domainObjs )
		{
			Map<String, Boolean> privs = objectToPrivileges.get(obj);

			// If privileges were found, and they contain all the required
			// privileges, and they were all granted...
			if( privs != null && privs.keySet().containsAll(privileges) && !privs.values().contains(Boolean.FALSE) )
			{
				// ... add the object!
				results.add(obj);
			}
		}
		return results;
	}

	@Override
	public <T> boolean checkPrivilege(String privilege, T domainObj)
	{
		return !filterNonGrantedObjects(Collections.singleton(privilege), Collections.singleton(domainObj)).isEmpty();
	}

	@Override
	@Transactional
	public <T> Map<T, Map<String, Boolean>> getPrivilegesForObjects(Collection<String> privileges,
		Collection<T> domainObjs)
	{
		if( CurrentUser.getUserState().isSystem() )
		{
			Builder<String, Boolean> builder = ImmutableMap.builder();
			for( String priv : privileges )
			{
				builder.put(priv, true);
			}
			ImmutableMap<String, Boolean> allPrivs = builder.build();
			Builder<T, Map<String, Boolean>> domainBuilder = ImmutableMap.builder();
			for( T t : domainObjs )
			{
				domainBuilder.put(t, allPrivs);
			}
			return domainBuilder.build();
		}
		return getObjectToPrivileges(privileges, domainObjs);
	}

	/**
	 * Creates a map from each object to a map of each privilege and whether it
	 * is granted or revoked.
	 */
	private <T> Map<T, Map<String, Boolean>> getObjectToPrivileges(Collection<String> privileges,
		Collection<T> domainObjs)
	{
		Map<T, Map<String, Boolean>> result = new LinkedHashMap<T, Map<String, Boolean>>();

		// Retrieve a map of target names to the domain objects
		Pair<Map<String, Set<T>>, Map<String, Set<T>>> allMappings = getNamesToObjectsMapping(domainObjs);

		// Get ACLS for owned objects, and map the objects to privileges
		Collection<Object[]> aclsForOwnerTargets = getAclsForDomainObjects(privileges, allMappings.getFirst(), true);
		mapObjectsToPrivileges(allMappings.getFirst(), aclsForOwnerTargets, result);

		// Get ACLS for non-owned objects, and map the objects to privileges
		Collection<Object[]> aclsForNotOwnerTargets = getAclsForDomainObjects(privileges, allMappings.getSecond(),
			false);
		mapObjectsToPrivileges(allMappings.getSecond(), aclsForNotOwnerTargets, result);

		return result;
	}

	/**
	 * Retrieves the ACLs for the given privileges and domain objects.
	 */
	private <T> Collection<Object[]> getAclsForDomainObjects(Collection<String> privileges,
		Map<String, Set<T>> targetMapping, boolean ownerExpressions)
	{
		UserState currentUser = CurrentUser.getUserState();

		Collection<Long> ownerOrNotOwner = ownerExpressions ? currentUser.getOwnerAclExpressions()
			: currentUser.getNotOwnerAclExpressions();

		Collection<Long> expressions = new CombinedCollection<Long>(currentUser.getCommonAclExpressions(),
			ownerOrNotOwner);

		if( !targetMapping.isEmpty() && !expressions.isEmpty() )
		{
			return aclDao.getPrivilegesForTargets(privileges, targetMapping.keySet(), expressions);
		}
		return Collections.emptyList();
	}

	/**
	 * Adds each object to a map, stating whether each privilege is granted or
	 * revoked.
	 */
	private <T> void mapObjectsToPrivileges(Map<String, Set<T>> targetMapping, Collection<Object[]> acls,
		Map<T, Map<String, Boolean>> objectToPrivileges)
	{
		for( Object[] entry : acls )
		{
			String combinedValue = (String) entry[0];
			String privilege = (String) entry[1];
			String targetName = (String) entry[2];

			boolean isGranted = combinedValue.endsWith(Character.toString(GRANT));

			Set<T> objects = targetMapping.get(targetName);
			if( objects != null )
			{
				for( T obj : objects )
				{
					Map<String, Boolean> privs = objectToPrivileges.get(obj);

					// Create a privilege set if non-existant
					if( privs == null )
					{
						privs = new HashMap<String, Boolean>();
						objectToPrivileges.put(obj, privs);
					}

					// If we have not already determined the privilege...
					if( !privs.containsKey(privilege) )
					{
						privs.put(privilege, isGranted);
					}
				}
			}
		}
		for( DomainObjectPrivilegeFilterExtension ext : domainObjectPrivFilters.getBeanList() )
		{
			ext.filterPrivileges(objectToPrivileges);
		}
	}

	@Override
	@Transactional
	public Set<String> filterNonGrantedPrivileges(Collection<String> privileges)
	{
		return filterNonGrantedPrivileges(privileges, false);
	}

	@Override
	@Transactional
	public Set<String> filterNonGrantedPrivileges(String... privileges)
	{
		return filterNonGrantedPrivileges(Arrays.asList(privileges), false);
	}

	@Override
	@Transactional
	public Set<String> filterNonGrantedPrivileges(Collection<String> privileges, boolean includePossibleOwnerAcls)
	{
		UserState currentUser = CurrentUser.getUserState();
		if( currentUser.isSystem() || privileges.isEmpty() )
		{
			return new HashSet<String>(privileges);
		}

		Collection<Long> exp = currentUser.getCommonAclExpressions();
		exp = new CombinedCollection<Long>(exp, currentUser.getNotOwnerAclExpressions());
		if( includePossibleOwnerAcls )
		{
			exp = new CombinedCollection<Long>(exp, currentUser.getOwnerAclExpressions());
		}

		List<Object[]> acls = aclDao.getPrivileges(privileges, exp);

		final Set<String> granted = new HashSet<String>();
		final Set<String> revokedOverall = new HashSet<String>();

		final Map<String, Set<String>> revokedPerObj = new HashMap<String, Set<String>>();

		// For each of the ACL entries that we have found...
		for( Object[] entry : acls )
		{
			final String privilege = (String) entry[1];
			if( !granted.contains(privilege) && !revokedOverall.contains(privilege) )
			{
				final String combinedValue = (String) entry[0];
				Set<String> revoked;
				if( !combinedValue.substring(0, 4).equals(OBJECT_PRIORITY_DB_FORMAT) )
				{
					revoked = revokedOverall;
				}
				else
				{
					String target = (String) entry[2];
					revoked = revokedPerObj.get(target);
					if( revoked == null )
					{
						revoked = new HashSet<String>();
						revokedPerObj.put(target, revoked);
					}
					else if( revoked.contains(privilege) )
					{
						continue;
					}

				}
				if( combinedValue.endsWith(Character.toString(GRANT)) )
				{
					granted.add(privilege);
				}
				else
				{
					revoked.add(privilege);
				}
			}
		}
		return granted;
	}

	@Override
	@Transactional
	public Set<String> filterNonGrantedPrivileges(Object domainObj, String... privileges)
	{
		return filterNonGrantedPrivileges(domainObj, Arrays.asList(privileges));
	}

	@Override
	@Transactional
	public Set<String> filterNonGrantedPrivileges(Object domainObj, Collection<String> privileges)
	{
		if( CurrentUser.getUserState().isSystem() )
		{
			return new HashSet<String>(privileges);
		}

		// Retrieve a map of target names to the domain objects
		Pair<Map<String, Set<Object>>, Map<String, Set<Object>>> allMappings = getNamesToObjectsMapping(
			Collections.singleton(domainObj));

		// Determine whether we are the owner of the domain object
		boolean owner = !allMappings.getFirst().isEmpty();
		Map<String, Set<Object>> targetMapping = owner ? allMappings.getFirst() : allMappings.getSecond();

		Collection<Object[]> aclsForTarget = getAclsForDomainObjects(privileges, targetMapping, owner);

		final Set<String> granted = new HashSet<String>();
		final Set<String> revoked = new HashSet<String>();

		// For each of the ACL entries that we have found...
		for( Object[] entry : aclsForTarget )
		{
			final String combinedValue = (String) entry[0];
			final String privilege = (String) entry[1];

			if( !granted.contains(privilege) && !revoked.contains(privilege) )
			{
				if( combinedValue.endsWith(Character.toString(GRANT)) )
				{
					granted.add(privilege);
				}
				else
				{
					revoked.add(privilege);
				}
			}
		}
		for( DomainObjectPrivilegeFilterExtension ext : domainObjectPrivFilters.getBeanList() )
		{
			ext.filterPrivileges(domainObj, granted);
		}
		return granted;
	}

	@Override
	@Transactional
	public TargetList getTargetList(Node privilegeNode, Object domainObj)
	{
		String target = SecurityConstants.TARGET_EVERYTHING;
		if( domainObj != null )
		{
			target = getLabelForTarget(domainObj);
		}

		List<Integer> priorities = new ArrayList<Integer>();
		priorities.add(privilegeNode.getOverridePriority());
		priorities.add(-privilegeNode.getOverridePriority());
		if( !privilegeNode.isVirtual() )
		{
			priorities.add(PRIORITY_OBJECT_INSTANCE);
		}

		TargetList list = new TargetList();
		list.setEntries(aclDao.getTargetListEntries(target, priorities));
		return list;
	}

	@Override
	@Transactional
	public void setTargetList(Node privilegeNode, Object domainObj, TargetList targetList)
	{
		final TargetList oldTL = getTargetList(privilegeNode, domainObj);

		String target = SecurityConstants.TARGET_EVERYTHING;
		if( domainObj != null )
		{
			target = getLabelForTarget(domainObj);
		}

		List<Integer> priorities = new ArrayList<Integer>();
		priorities.add(privilegeNode.getOverridePriority());
		priorities.add(-privilegeNode.getOverridePriority());
		if( !privilegeNode.isVirtual() )
		{
			priorities.add(PRIORITY_OBJECT_INSTANCE);
		}
		aclDao.deleteAll(target, targetList != null && targetList.isPartial(), priorities);

		if( targetList != null && targetList.getEntries() != null )
		{
			int order = targetList.getEntries().size() - 1;
			for( TargetListEntry entry : targetList.getEntries() )
			{
				addAccessEntry(target + entry.getPostfix(), privilegeNode, entry.isGranted(), entry.isOverride(),
					order--, entry.getPrivilege(), entry.getWho(), null);
			}
		}

		// If don't flush then they don't return from getAclExpressions()
		aclDao.flush();

		// Update the user's expression list, just to ensure that the new entity
		// works.
		ModifiableUserState userState = (ModifiableUserState) CurrentUser.getUserState();
		userState.setAclExpressions(getAclExpressions(userState));

		final TargetList newTL = getTargetList(privilegeNode, domainObj);
		List<SecurityPostProcessor> processorList = postProcessors.getBeanList();
		for( SecurityPostProcessor securityPostProcessor : processorList )
		{
			securityPostProcessor.postProcess(privilegeNode, target, domainObj, oldTL, newTL);
		}
	}

	@Override
	@Transactional
	public void addAccessEntry(Object domainObj, Node privilegeNode, boolean grant, boolean override, String privilege,
		String expression, Date expiry)
	{
		String target = SecurityConstants.TARGET_EVERYTHING;
		if( domainObj != null )
		{
			target = getLabelForTarget(domainObj);
		}

		addAccessEntry(target, privilegeNode, grant, override, 0, privilege, expression, expiry);
	}

	@Override
	@Transactional
	public AccessExpression retrieveOrCreate(String expression)
	{
		return expressionDao.retrieveOrCreate(expression);
	}

	private void addAccessEntry(String target, Node privilegeNode, boolean grant, boolean override, int order,
		String privilege, String expression, Date expiry)
	{
		int priority = PrivilegeTree.getPriority(privilegeNode, privilege);
		if( !override )
		{
			priority = -priority;
		}

		AccessEntry newEntry = new AccessEntry();
		newEntry.setPrivilege(privilege);
		newEntry.setTargetObject(target);
		newEntry.setAclPriority(priority);
		newEntry.setAclOrder(order);
		newEntry.setInstitution(CurrentInstitution.get());
		newEntry.setGrantRevoke(grant ? GRANT : REVOKE);
		newEntry.setExpression(expressionDao.retrieveOrCreate(expression));
		newEntry.setExpiry(expiry);

		aclDao.save(newEntry);
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_SECURITY_TREE")
	@Transactional
	public void setTargetListAndReindex(String privilegeNodeValue, Object target, TargetList targetList)
	{
		setTargetList(Node.valueOf(privilegeNodeValue), target, targetList);
	}

	@Override
	@Transactional
	public List<ACLEntryMapping> getAllEntriesForObject(Object domainObj, String privilege)
	{
		Set<String> targets = getLabelsForTarget(domainObj);
		return aclDao.getAllEntries(Arrays.asList(privilege), targets);
	}

	@Override
	@Transactional
	public List<ACLEntryMapping> getAllEntriesForObjectOtherThanTheObject(Object domainObj, String privilege)
	{
		String target = getLabelForTarget(domainObj);

		List<ACLEntryMapping> results = getAllEntriesForObject(domainObj, privilege);
		for( Iterator<ACLEntryMapping> iter = results.iterator(); iter.hasNext(); )
		{
			ACLEntryMapping entry = iter.next();
			if( entry.getTarget().equals(target) )
			{
				iter.remove();
			}
		}
		return results;
	}

	@Override
	@Transactional
	public Triple<Collection<Long>, Collection<Long>, Collection<Long>> getAclExpressions(UserState userState)
	{
		return getAclExpressions(userState, true);
	}

	@Override
	@Transactional
	public Triple<Collection<Long>, Collection<Long>, Collection<Long>> getAclExpressions(UserState userState,
		boolean enableIpReferAcl)
	{
		List<String> values = new ArrayList<String>();
		values.add(getRecipient(EVERYONE));
		values.add(getRecipient(OWNER));

		if( !userState.isGuest() )
		{
			values.add(getRecipient(USER, userState.getUserBean().getUniqueID()));
		}

		for( String groupID : userState.getUsersGroups() )
		{
			values.add(getRecipient(GROUP, groupID));
		}

		for( String role : userState.getUsersRoles() )
		{
			values.add(getRecipient(ROLE, role));
		}

		if( userState.getSharePassEmail() != null )
		{
			values.add(getRecipient(SHARE_PASS, userState.getSharePassEmail()));
		}

		if( userState.getTokenSecretId() != null )
		{
			values.add(getRecipient(TOKEN_SECRET_ID, userState.getTokenSecretId()));
		}

		Collection<Long> common = new ArrayList<Long>();
		Collection<Long> owner = new ArrayList<Long>();
		Collection<Long> notOwner = new ArrayList<Long>();

		AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
		for( Triple<Long, String, Boolean> exp : expressionDao.getMatchingExpressions(values) )
		{
			long expressionID = exp.getFirst();
			String expression = exp.getSecond();
			boolean isDynamic = exp.getThird();

			// If it's a dynamic expression...
			if( isDynamic )
			{
				boolean own = evaluator.evaluate(expression, userState, true, enableIpReferAcl);
				boolean nOwn = evaluator.evaluate(expression, userState, false, enableIpReferAcl);

				if( own && nOwn )
				{
					common.add(expressionID);
				}
				else if( own )
				{
					owner.add(expressionID);
				}
				else if( nOwn )
				{
					notOwner.add(expressionID);
				}
			}
			else if( evaluator.evaluate(expression, userState, true) )
			{
				common.add(expressionID);
			}
		}
		return new Triple<Collection<Long>, Collection<Long>, Collection<Long>>(common, owner, notOwner);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAllEntityChildren(Node type, long id)
	{
		// TODO: De-hardcode this. Could either be part of the
		// SecurityTargetHandler interface, or move the specifics to the method
		// callee (eg, don't pass in a Node).
		String prefix = null;
		switch( type )
		{
			case WORKFLOW_TASK:
				prefix = SecurityConstants.TARGET_WORKFLOW_TASK;
				break;
			case ITEM_METADATA:
				prefix = SecurityConstants.TARGET_ITEM_METADATA;
				break;
			case DYNAMIC_ITEM_METADATA:
				prefix = SecurityConstants.TARGET_DYNAMIC_ITEM_METADATA;
				break;
			default:
				throw new IllegalStateException("Node type not handled: " + type);
		}

		String target = prefix + ":" + id + ":";
		aclDao.deleteAll(target, true, Arrays.asList(type.getOverridePriority(), -type.getOverridePriority()));
	}

	@Override
	@Transactional
	public void deleteExpiredAccessEntries()
	{
		Criterion c1 = Restrictions.isNotNull("expiry");
		Criterion c2 = Restrictions.lt("expiry", new Date());
		for( AccessEntry entry : aclDao.findAllByCriteria(c1, c2) )
		{
			aclDao.delete(entry);
		}
	}

	/**
	 * Generates a pair of mappings, the first mapping being domain objects for
	 * which the current user is the owner, and the second for the remaining
	 * domain objects. The mappings define a target name to a set of relevant
	 * domain objects.
	 */
	private <T> Pair<Map<String, Set<T>>, Map<String, Set<T>>> getNamesToObjectsMapping(Collection<T> domainObjs)
	{
		Map<String, Set<T>> owned = new HashMap<String, Set<T>>();
		Map<String, Set<T>> notOwned = new HashMap<String, Set<T>>();

		for( T domainObj : domainObjs )
		{
			Map<String, Set<T>> currentMapping = isOwner(domainObj) ? owned : notOwned;

			Set<String> names = getLabelsForTarget(domainObj);
			for( String name : names )
			{
				Set<T> otherObjs = currentMapping.get(name);
				if( otherObjs == null )
				{
					otherObjs = new HashSet<T>();
					currentMapping.put(name, otherObjs);
				}
				otherObjs.add(domainObj);
			}
		}

		return new Pair<Map<String, Set<T>>, Map<String, Set<T>>>(owned, notOwned);
	}

	/**
	 * Determines a set of target names for a given domain object.
	 */
	private Set<String> getLabelsForTarget(Object target)
	{
		final Set<String> labels = new LinkedHashSet<String>();
		labels.add(TARGET_EVERYTHING);

		if( target == null )
		{
			return labels;
		}

		TargetTransformer tt = new TargetTransformer(target);
		do
		{
			Object t = tt.next();
			SecurityTargetHandler labelHandler = getHandler(labellingHandlers, t);
			if( labelHandler != null )
			{
				labelHandler.gatherAllLabels(labels, t);
			}
		}
		while( tt.hasNext() );

		return labels;
	}

	/**
	 * Determines the target name of a given domain object.
	 */
	private String getLabelForTarget(Object target)
	{
		SecurityTargetHandler handler = getHandler(labellingHandlers, target);
		if( handler != null )
		{
			return handler.getPrimaryLabel(target);
		}

		// TODO: De-hardcode this by implementing a SecurityTargetHandler and
		// deleting this.
		if( target instanceof WorkflowTaskTarget )
		{
			WorkflowTaskTarget workflowTask = (WorkflowTaskTarget) target;
			return workflowTask.getTarget();
		}
		else if( target instanceof WorkflowTaskDynamicTarget )
		{
			WorkflowTaskDynamicTarget workflowTask = (WorkflowTaskDynamicTarget) target;
			return workflowTask.getTarget();
		}

		else
		{
			throw new RuntimeException("No handler for security target: " + target.getClass());
		}
	}

	/**
	 * Checks whether the current user is the owner of the given domain object.
	 */
	private boolean isOwner(Object target)
	{
		TargetTransformer tt = new TargetTransformer(target);
		do
		{
			Object t = tt.next();
			SecurityTargetHandler ownerHandler = getHandler(ownerHandlers, t);
			if( ownerHandler != null )
			{
				return ownerHandler.isOwner(t, CurrentUser.getUserID());
			}
		}
		while( tt.hasNext() );

		return false;
	}

	private SecurityTargetHandler getHandler(PluginTracker<SecurityTargetHandler> tracker, Object target)
	{
		if( target != null )
		{
			Map<String, SecurityTargetHandler> map = tracker.getBeanMap();
			Class<?> klass = target.getClass();
			while( klass != null )
			{
				SecurityTargetHandler handler = map.get(klass.getName());
				if( handler != null )
				{
					return handler;
				}
				klass = klass.getSuperclass();
			}
		}
		return null;
	}

	private class TargetTransformer implements Iterator<Object>
	{
		private final Set<Class<?>> handledClassNames = new LinkedHashSet<Class<?>>();

		private SecurityTargetHandler handler;
		private Object target;

		public TargetTransformer(Object target)
		{
			this.target = target;
		}

		@Override
		public boolean hasNext()
		{
			handler = getHandler(transformHandlers, target);
			return handler != null;
		}

		@Override
		public Object next()
		{
			// handler is not initialised first time through, and we want to
			// return the original target anyway.
			if( handler != null )
			{
				target = handler.transform(target);

				final Class<? extends Object> targetClass = target.getClass();
				if( handledClassNames.contains(targetClass) )
				{
					// Avoid infinite loops
					throw new RuntimeException(
						"Loop in security target transformation detected: " + handledClassNames.toString()
							+ " attempting to then go back to " + targetClass.getName() + " again");
				}
				else
				{
					handledClassNames.add(targetClass);
				}
			}
			return target;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	@Transactional
	public void userIdChanged(String fromUserId, String toUserId)
	{
		Map<Long, Long> remap = expressionDao.userIdChanged(fromUserId, toUserId);
		remapAclsExpressions(remap);
	}

	@Override
	@Transactional
	public void groupIdChanged(String fromGroupId, String toGroupId)
	{
		Map<Long, Long> remap = expressionDao.groupIdChanged(fromGroupId, toGroupId);
		remapAclsExpressions(remap);
	}

	private void remapAclsExpressions(Map<Long, Long> oldToNewIds)
	{
		for( Entry<Long, Long> entry : oldToNewIds.entrySet() )
		{
			aclDao.remapExpressionId(entry.getKey(), entry.getValue());
		}
	}

	@Override
	@Transactional
	public ListMultimap<String, AccessEntry> getExistingEntriesForVirtualNodes(Node... nodes)
	{
		Collection<Integer> priorities = Lists.newArrayList();
		for( Node node : nodes )
		{
			if( !node.isVirtual() )
			{
				throw new IllegalArgumentException("Not a virtual node");
			}
			priorities.add(node.getOverridePriority());
			priorities.add(-node.getOverridePriority());
		}
		List<AccessEntry> entries = aclDao.getVirtualAccessEntries(priorities);
		ArrayListMultimap<String, AccessEntry> map = ArrayListMultimap.create();
		for( AccessEntry accessEntry : entries )
		{
			map.put(accessEntry.getTargetObject() + Integer.toString(Math.abs(accessEntry.getAclPriority())),
				accessEntry);
		}
		return map;
	}

	@Override
	public String getKeyForVirtualNode(Node node, Object domainObject)
	{
		String target = SecurityConstants.TARGET_EVERYTHING;
		if( domainObject != null )
		{
			target = getLabelForTarget(domainObject);
		}
		return target + node.getOverridePriority();
	}

}
