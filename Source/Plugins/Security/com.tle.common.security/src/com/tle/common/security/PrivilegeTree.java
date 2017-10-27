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

package com.tle.common.security;

import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_COLLECTIONS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_CONNECTORS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_COURSE_INFO;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_CUSTOM_LINKS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_DYNA_COLLECTIONS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_ECHOS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_EXTERNAL_TOOLS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_FEDERATED_SEARCHES;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_FILTER_GROUPS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_HARVESTER_PROFILES;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_HTMLEDITOR_PLUGINS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_KALTURAS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_LTI_CONSUMERS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_MANAGING;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_OAUTH_CLIENTS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_PORTLETS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_POWER_SEARCHES;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_REPORTS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_SCHEMAS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_SYSTEM_SETTINGS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_TAXONOMIES;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_USER_SCRIPTS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_WORKFLOWS;
import static com.tle.common.security.SecurityConstants.PRIORITY_COLLECTION;
import static com.tle.common.security.SecurityConstants.PRIORITY_CONNECTOR;
import static com.tle.common.security.SecurityConstants.PRIORITY_COURSE_INFO;
import static com.tle.common.security.SecurityConstants.PRIORITY_CUSTOM_LINK;
import static com.tle.common.security.SecurityConstants.PRIORITY_DYNAMIC_METADATA;
import static com.tle.common.security.SecurityConstants.PRIORITY_DYNA_COLLECTION;
import static com.tle.common.security.SecurityConstants.PRIORITY_ECHO;
import static com.tle.common.security.SecurityConstants.PRIORITY_EXTERNAL_TOOL;
import static com.tle.common.security.SecurityConstants.PRIORITY_FEDERATED_SEARCH;
import static com.tle.common.security.SecurityConstants.PRIORITY_FILTER_GROUP;
import static com.tle.common.security.SecurityConstants.PRIORITY_GLOBAL_ITEM_STATUS;
import static com.tle.common.security.SecurityConstants.PRIORITY_HARVESTER_PROFILE;
import static com.tle.common.security.SecurityConstants.PRIORITY_HIERARCHY_TOPIC;
import static com.tle.common.security.SecurityConstants.PRIORITY_HTMLEDITOR_PLUGIN;
import static com.tle.common.security.SecurityConstants.PRIORITY_INSTITUTION;
import static com.tle.common.security.SecurityConstants.PRIORITY_ITEM;
import static com.tle.common.security.SecurityConstants.PRIORITY_ITEM_METADATA;
import static com.tle.common.security.SecurityConstants.PRIORITY_ITEM_STATUS;
import static com.tle.common.security.SecurityConstants.PRIORITY_KALTURA;
import static com.tle.common.security.SecurityConstants.PRIORITY_LTI_CONSUMER;
import static com.tle.common.security.SecurityConstants.PRIORITY_MANAGING;
import static com.tle.common.security.SecurityConstants.PRIORITY_OAUTH_CLIENT;
import static com.tle.common.security.SecurityConstants.PRIORITY_OBJECT_INSTANCE;
import static com.tle.common.security.SecurityConstants.PRIORITY_PORTLET;
import static com.tle.common.security.SecurityConstants.PRIORITY_POWER_SEARCH;
import static com.tle.common.security.SecurityConstants.PRIORITY_REPORT;
import static com.tle.common.security.SecurityConstants.PRIORITY_SCHEMA;
import static com.tle.common.security.SecurityConstants.PRIORITY_SYSTEM_SETTING;
import static com.tle.common.security.SecurityConstants.PRIORITY_TAXONOMY;
import static com.tle.common.security.SecurityConstants.PRIORITY_USER_SCRIPTS;
import static com.tle.common.security.SecurityConstants.PRIORITY_WORKFLOW;
import static com.tle.common.security.SecurityConstants.PRIORITY_WORKFLOW_TASK;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public final class PrivilegeTree
{
	// FIXME: at some stage someone is going to have to do something about
	// this...
	public enum Node
	{
		INSTITUTION(true, PRIORITY_INSTITUTION), ALL_SCHEMAS(true, PRIORITY_ALL_SCHEMAS),
		SCHEMA(false, PRIORITY_SCHEMA), ALL_COLLECTIONS(true, PRIORITY_ALL_COLLECTIONS),
		COLLECTION(false, PRIORITY_COLLECTION), GLOBAL_ITEM_STATUS(true, PRIORITY_GLOBAL_ITEM_STATUS),
		ITEM_STATUS(true, PRIORITY_ITEM_STATUS), ITEM_METADATA(true, PRIORITY_ITEM_METADATA),
		DYNAMIC_ITEM_METADATA(false, PRIORITY_DYNAMIC_METADATA), ALL_WORKFLOWS(true, PRIORITY_ALL_WORKFLOWS),
		WORKFLOW(false, PRIORITY_WORKFLOW), WORKFLOW_TASK(false, PRIORITY_WORKFLOW_TASK),
		ALL_POWER_SEARCHES(true, PRIORITY_ALL_POWER_SEARCHES), POWER_SEARCH(false, PRIORITY_POWER_SEARCH),
		ALL_FEDERATED_SEARCHES(true, PRIORITY_ALL_FEDERATED_SEARCHES),
		FEDERATED_SEARCH(false, PRIORITY_FEDERATED_SEARCH), ALL_FILTER_GROUPS(true, PRIORITY_ALL_FILTER_GROUPS),
		FILTER_GROUP(false, PRIORITY_FILTER_GROUP), ITEM(false, PRIORITY_ITEM), ALL_REPORTS(true, PRIORITY_ALL_REPORTS),
		REPORT(false, PRIORITY_REPORT), ALL_SYSTEM_SETTINGS(true, PRIORITY_ALL_SYSTEM_SETTINGS),
		SYSTEM_SETTING(false, PRIORITY_SYSTEM_SETTING), HIERARCHY_TOPIC(false, PRIORITY_HIERARCHY_TOPIC),
		ALL_COURSE_INFO(true, PRIORITY_ALL_COURSE_INFO), COURSE_INFO(false, PRIORITY_COURSE_INFO),
		ALL_DYNA_COLLECTIONS(true, PRIORITY_ALL_DYNA_COLLECTIONS), DYNA_COLLECTION(false, PRIORITY_DYNA_COLLECTION),
		ALL_TAXONOMIES(true, PRIORITY_ALL_TAXONOMIES), TAXONOMY(false, PRIORITY_TAXONOMY),

		// Portlet HAX
		ALL_PORTLETS(true, PRIORITY_ALL_PORTLETS), PORTLET(false, PRIORITY_PORTLET),
		PORTLET_HTML(false, PRIORITY_PORTLET), PORTLET_RSS(false, PRIORITY_PORTLET),
		PORTLET_IFRAME(false, PRIORITY_PORTLET), PORTLET_FREEMARKER(false, PRIORITY_PORTLET),
		PORTLET_TASKS(false, PRIORITY_PORTLET), PORTLET_SEARCH(false, PRIORITY_PORTLET),
		PORTLET_MYRESOURCES(false, PRIORITY_PORTLET), PORTLET_FAVOURITES(false, PRIORITY_PORTLET),
		PORTLET_RECENT(false, PRIORITY_PORTLET), PORTLET_BROWSE(false, PRIORITY_PORTLET),
		PORTLET_TASKSTATISTICS(false, PRIORITY_PORTLET),
		// PORTLET_CONTRIB(false, PRIORITY_PORTLET) ??

		// Connectors HAX
		ALL_CONNECTORS(true, PRIORITY_ALL_CONNECTORS), CONNECTOR(false, PRIORITY_CONNECTOR),

		// Kaltura HAX
		ALL_KALTURAS(true, PRIORITY_ALL_KALTURAS), KALTURA(false, PRIORITY_KALTURA),

		// Echo 360 HAX
		ALL_ECHOS(true, PRIORITY_ALL_ECHOS), ECHO(false, PRIORITY_ECHO),

		// OAuth HAX
		ALL_OAUTH_CLIENTS(true, PRIORITY_ALL_OAUTH_CLIENTS), OAUTH_CLIENT(false, PRIORITY_OAUTH_CLIENT),

		// User scripts HAX
		ALL_USER_SCRIPTS(true, PRIORITY_ALL_USER_SCRIPTS), USER_SCRIPTS(false, PRIORITY_USER_SCRIPTS),

		// External Tools HAX
		ALL_EXTERNAL_TOOLS(true, PRIORITY_ALL_EXTERNAL_TOOLS), EXTERNAL_TOOL(false, PRIORITY_EXTERNAL_TOOL),

		// LTI Consumers HAX ON HAX ON HAX
		ALL_LTI_CONSUMERS(true, PRIORITY_ALL_LTI_CONSUMERS), LTI_CONSUMER(false, PRIORITY_LTI_CONSUMER),

		// Sigh. Tiny MCE Plugins HAX
		ALL_HTMLEDITOR_PLUGINS(true, PRIORITY_ALL_HTMLEDITOR_PLUGINS),
		HTMLEDITOR_PLUGIN(false, PRIORITY_HTMLEDITOR_PLUGIN),

		ALL_HARVESTER_PROFILES(true, PRIORITY_ALL_HARVESTER_PROFILES),
		HARVESTER_PROFILE(false, PRIORITY_HARVESTER_PROFILE), ALL_CUSTOM_LINKS(true, PRIORITY_ALL_CUSTOM_LINKS),
		CUSTOM_LINK(false, PRIORITY_CUSTOM_LINK), ALL_MANAGING(true, PRIORITY_ALL_MANAGING),
		MANAGING(false, PRIORITY_MANAGING);

		private final boolean virtual;
		private final int overridePriority;

		private Node(boolean virtual, int overridePriority)
		{
			this.virtual = virtual;
			this.overridePriority = overridePriority;
		}

		public boolean isVirtual()
		{
			return virtual;
		}

		public int getOverridePriority()
		{
			return overridePriority;
		}
	}

	private PrivilegeTree()
	{
		throw new Error();
	}

	/**
	 * Keeps a mapping of node types to privilege nodes.
	 */
	private static final Map<Node, PrivilegeNode> mapping = new HashMap<Node, PrivilegeNode>();

	/**
	 * Determines whether a privilege for the given node is an override/default.
	 */
	public static boolean isOverrideDefault(Node node, String privilege)
	{
		return getPriority(node, privilege) != 0;
	}

	/**
	 * Finds the priority for a given privilege on a node.
	 */
	public static int getPriority(Node node, String privilege)
	{
		ensureMapping();

		PrivilegeNode privNode = mapping.get(node);
		if( privNode.getPrivileges().contains(privilege) )
		{
			return node.isVirtual() ? node.getOverridePriority() : PRIORITY_OBJECT_INSTANCE;
		}
		else if( doesNodeContainPrivilege(privNode, privilege) )
		{
			return node.getOverridePriority();
		}
		else
		{
			throw new RuntimeException("Privilege is not defined for this target: " + privilege + " - " + node);
		}
	}

	private static boolean doesNodeContainPrivilege(PrivilegeNode privNode, String privilege)
	{
		if( privNode.getPrivileges().contains(privilege) )
		{
			return true;
		}

		for( PrivilegeNode childNode : privNode.getChildren() )
		{
			if( doesNodeContainPrivilege(childNode, privilege) )
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the privileges for the current node.
	 */
	public static Set<String> getPrivilegesForNode(Node node)
	{
		ensureMapping();
		PrivilegeNode privNode = mapping.get(node);
		return Collections.unmodifiableSet(privNode.getPrivileges());
	}

	/**
	 * Returns all privileges for the current node and all it's children.
	 */
	public static Map<String, Integer> getAllPrivilegesForNode(Node node)
	{
		ensureMapping();

		Map<String, Integer> results = new TreeMap<String, Integer>();
		getAllPrivilegesForNode(results, mapping.get(node), !node.isVirtual(), node.getOverridePriority());
		return results;
	}

	private static void getAllPrivilegesForNode(Map<String, Integer> results, PrivilegeNode node,
		boolean useObjectInstancePriority, int defaultPriority)
	{
		final int priority = useObjectInstancePriority ? PRIORITY_OBJECT_INSTANCE : defaultPriority;

		// For each defined privilege
		for( String privilege : node.getPrivileges() )
		{
			Integer oldPriority = results.put(privilege, priority);
			if( oldPriority != null && oldPriority.intValue() != priority )
			{
				throw new RuntimeException("Privilege already added with different priority: " + privilege);
			}
		}

		// Recurse on children
		for( PrivilegeNode child : node.getChildren() )
		{
			getAllPrivilegesForNode(results, child, false, defaultPriority);
		}
	}

	/**
	 * This method should be invoked at the start of all methods.
	 */
	private static synchronized void ensureMapping()
	{
		if( !mapping.isEmpty() )
		{
			return;
		}

		PrivilegeNode item = new PrivilegeNode(Node.ITEM);
		item.registerPrivilege("REDRAFT_ITEM");
		item.registerPrivilege("REVIEW_ITEM");
		item.registerPrivilege("NEWVERSION_ITEM");
		item.registerPrivilege("ARCHIVE_ITEM");
		item.registerPrivilege("DISCOVER_ITEM");
		item.registerPrivilege("VIEW_ITEM");
		item.registerPrivilege("EDIT_ITEM");
		item.registerPrivilege("DELETE_ITEM");
		item.registerPrivilege("PURGE_ITEM");
		item.registerPrivilege("COMMENT_CREATE_ITEM");
		item.registerPrivilege("COMMENT_VIEW_ITEM");
		item.registerPrivilege("COMMENT_DELETE_ITEM");
		item.registerPrivilege("REASSIGN_OWNERSHIP_ITEM");
		item.registerPrivilege("DIGITAL_RIGHTS_ITEM");
		item.registerPrivilege("VIEW_HISTORY_ITEM");
		item.registerPrivilege("EXPORT_ITEM");
		item.registerPrivilege("SHARE_ITEM");
		item.registerPrivilege("SUSPEND_ITEM");
		item.registerPrivilege("CLONE_ITEM");
		item.registerPrivilege("MOVE_ITEM");
		item.registerPrivilege("RAW_VIEW_ITEM");
		item.registerPrivilege("VIEW_RESTRICTED_ATTACHMENTS");
		item.registerPrivilege("RESTRICT_ATTACHMENTS");
		item.registerPrivilege("VIEW_ATTACHMENTS");

		// CAL TODO: plugin
		item.registerPrivilege("COPYRIGHT_ITEM");
		item.registerPrivilege("DELETE_ACTIVATION_ITEM");
		item.registerPrivilege("VIEW_ACTIVATION_ITEM");
		item.registerPrivilege("DEACTIVATE_ACTIVATION_ITEM");
		item.registerPrivilege("VIEW_INACTIVE_PORTIONS");
		item.registerPrivilege("VIEW_LINKED_PORTIONS");
		item.registerPrivilege("COPYRIGHT_OVERRIDE");
		item.registerPrivilege("EDIT_ACTIVATION_ITEM");
		item.registerPrivilege("AUTO_CREATE_COURSE");

		// HARVESTER TODO: plugin
		item.registerPrivilege("DOWNLOAD_ITEM");

		// Push to LMS TODO: plugin
		item.registerPrivilege("FIND_USES_ITEM");
		item.registerPrivilege("EXPORT_TO_LMS_ITEM");

		mapping.put(Node.ITEM, item);

		// Schemas
		PrivilegeNode schemas = buildEntity(Node.SCHEMA, Node.ALL_SCHEMAS, "SCHEMA");

		// Advanced Searches
		PrivilegeNode powerSearches = buildBasic(Node.POWER_SEARCH, Node.ALL_POWER_SEARCHES, "POWER_SEARCH");
		powerSearches.getChildren().get(0).registerPrivilege("SEARCH_POWER_SEARCH");

		// Dynamic Collections
		PrivilegeNode dynaCollections = buildEntity(Node.DYNA_COLLECTION, Node.ALL_DYNA_COLLECTIONS, "DYNA_COLLECTION");
		dynaCollections.getChildren().get(0).registerPrivilege("SEARCH_DYNA_COLLECTION");

		// Workflows
		PrivilegeNode workflowTask = new PrivilegeNode(Node.WORKFLOW_TASK);
		workflowTask.registerPrivilege("MODERATE_ITEM");
		workflowTask.getChildren().add(item);
		mapping.put(Node.WORKFLOW_TASK, workflowTask);

		PrivilegeNode workflows = buildEntity(Node.WORKFLOW, Node.ALL_WORKFLOWS, "WORKFLOW");
		workflows.getChildren().get(0).registerPrivilege("MANAGE_WORKFLOW");
		workflows.registerPrivilege("MANAGE_WORKFLOW");
		workflows.registerPrivilege("APPROVE_BULK_TASKS");
		workflows.registerPrivilege("REJECT_BULK_TASKS");

		// Federated Searches
		PrivilegeNode federatedSearches = buildBasic(Node.FEDERATED_SEARCH, Node.ALL_FEDERATED_SEARCHES,
			"FEDERATED_SEARCH");
		federatedSearches.getChildren().get(0).registerPrivilege("SEARCH_FEDERATED_SEARCH");

		// Taxonomies
		PrivilegeNode taxonomies = buildEntity(Node.TAXONOMY, Node.ALL_TAXONOMIES, "TAXONOMY");

		// Harvester Profiles
		PrivilegeNode harvesterProfiles = buildBasic(Node.HARVESTER_PROFILE, Node.ALL_HARVESTER_PROFILES,
			"HARVESTER_PROFILE");

		// Custom Link TODO Plugin and web based

		PrivilegeNode customLinks = new PrivilegeNode(Node.ALL_CUSTOM_LINKS);
		customLinks.registerPrivilege("CREATE_" + Node.CUSTOM_LINK);
		customLinks.registerPrivilege("EDIT_" + Node.CUSTOM_LINK);
		customLinks.registerPrivilege("DELETE_" + Node.CUSTOM_LINK);
		mapping.put(Node.ALL_CUSTOM_LINKS, customLinks);

		PrivilegeNode customLinksChild = new PrivilegeNode(Node.CUSTOM_LINK);
		customLinksChild.registerPrivilege("VIEW_" + Node.CUSTOM_LINK);
		customLinks.getChildren().add(customLinksChild);
		mapping.put(Node.CUSTOM_LINK, customLinksChild);

		// Course Infos
		PrivilegeNode courseInfo = buildEntity(Node.COURSE_INFO, Node.ALL_COURSE_INFO, "COURSE_INFO");

		// Reports
		PrivilegeNode reports = buildBasic(Node.REPORT, Node.ALL_REPORTS, "REPORT");
		reports.registerPrivilege("DESIGN_REPORT");
		PrivilegeNode report = reports.getChildren().get(0);
		report.registerPrivilege("EXECUTE_REPORT");

		// Dynamic Item Metadata
		PrivilegeNode dynamicItemMetadata = new PrivilegeNode(Node.DYNAMIC_ITEM_METADATA);
		dynamicItemMetadata.getChildren().add(item);
		mapping.put(Node.DYNAMIC_ITEM_METADATA, dynamicItemMetadata);

		// Item Metadata
		PrivilegeNode itemMetadata = new PrivilegeNode(Node.ITEM_METADATA);
		itemMetadata.getChildren().add(item);
		mapping.put(Node.ITEM_METADATA, itemMetadata);

		// Item Statuses
		PrivilegeNode itemStatus = new PrivilegeNode(Node.ITEM_STATUS);
		itemStatus.getChildren().add(item);
		mapping.put(Node.ITEM_STATUS, itemStatus);

		PrivilegeNode globalItemStatus = new PrivilegeNode(Node.ITEM_STATUS);
		globalItemStatus.getChildren().add(item);
		mapping.put(Node.GLOBAL_ITEM_STATUS, globalItemStatus);

		// Item Definitions
		PrivilegeNode itemDefinitions = buildEntity(Node.COLLECTION, Node.ALL_COLLECTIONS, "COLLECTION");
		PrivilegeNode itemDefinition = itemDefinitions.getChildren().get(0);
		itemDefinition.registerPrivilege("SEARCH_COLLECTION");
		itemDefinition.registerPrivilege("CREATE_ITEM");
		itemDefinition.getChildren().add(itemStatus);
		itemDefinition.getChildren().add(itemMetadata);

		// TODO: pluginerise
		// Portlets
		PrivilegeNode portlets = new PrivilegeNode(Node.ALL_PORTLETS);
		portlets.registerPrivilege("CREATE_PORTLET");
		portlets.registerPrivilege("EDIT_PORTLET");
		portlets.registerPrivilege("ADMINISTER_PORTLETS");
		mapping.put(Node.ALL_PORTLETS, portlets);

		PrivilegeNode child = new PrivilegeNode(Node.PORTLET);
		child.registerPrivilege("EDIT_PORTLET");
		child.registerPrivilege("DELETE_PORTLET");
		child.registerPrivilege("VIEW_PORTLET");
		portlets.getChildren().add(child);
		mapping.put(Node.PORTLET, child);

		addPortletType(portlets, Node.PORTLET_SEARCH);
		addPortletType(portlets, Node.PORTLET_HTML);
		addPortletType(portlets, Node.PORTLET_IFRAME);
		addPortletType(portlets, Node.PORTLET_FREEMARKER);
		addPortletType(portlets, Node.PORTLET_RECENT);
		addPortletType(portlets, Node.PORTLET_RSS);
		addPortletType(portlets, Node.PORTLET_TASKS);
		addPortletType(portlets, Node.PORTLET_FAVOURITES);
		addPortletType(portlets, Node.PORTLET_MYRESOURCES);
		addPortletType(portlets, Node.PORTLET_BROWSE);
		addPortletType(portlets, Node.PORTLET_TASKSTATISTICS);

		// Connectors
		PrivilegeNode connectors = buildEntity(Node.CONNECTOR, Node.ALL_CONNECTORS, "CONNECTOR");
		connectors.registerPrivilege("VIEWCONTENT_VIA_CONNECTOR");
		connectors.registerPrivilege("EXPORT_VIA_CONNECTOR");
		PrivilegeNode connector = connectors.getChildren().get(0);
		connector.registerPrivilege("VIEWCONTENT_VIA_CONNECTOR");
		connector.registerPrivilege("EXPORT_VIA_CONNECTOR");

		// Kalturas
		PrivilegeNode kalturas = buildBasic(Node.KALTURA, Node.ALL_KALTURAS, "KALTURA");

		// Echos
		PrivilegeNode echos = buildBasic(Node.ECHO, Node.ALL_ECHOS, "ECHO");

		// OAuth
		PrivilegeNode oauthClients = buildEntity(Node.OAUTH_CLIENT, Node.ALL_OAUTH_CLIENTS, "OAUTH_CLIENT");

		// Tiny MCE Plugins
		PrivilegeNode htmlEditorPluginss = buildBasic(Node.HTMLEDITOR_PLUGIN, Node.ALL_HTMLEDITOR_PLUGINS,
			"HTMLEDITOR_PLUGIN");

		// User scripts
		PrivilegeNode userScripts = buildBasic(Node.USER_SCRIPTS, Node.ALL_USER_SCRIPTS, "USER_SCRIPTS");

		// External Tools
		PrivilegeNode externalTools = buildBasic(Node.EXTERNAL_TOOL, Node.ALL_EXTERNAL_TOOLS, "EXTERNAL_TOOL");

		// LTI Consumers
		PrivilegeNode ltiConsumers = buildEntity(Node.LTI_CONSUMER, Node.ALL_LTI_CONSUMERS, "LTI_CONSUMER");

		// Admin Console
		PrivilegeNode systemSetting = new PrivilegeNode(Node.SYSTEM_SETTING);
		mapping.put(Node.SYSTEM_SETTING, systemSetting);
		systemSetting.registerPrivilege("EDIT_SYSTEM_SETTINGS");

		PrivilegeNode allSystemSettings = new PrivilegeNode(Node.ALL_SYSTEM_SETTINGS);
		mapping.put(Node.ALL_SYSTEM_SETTINGS, allSystemSettings);
		allSystemSettings.registerPrivilege("EDIT_SYSTEM_SETTINGS");
		allSystemSettings.getChildren().add(systemSetting);

		// Managing activations/items/etc
		PrivilegeNode manage = new PrivilegeNode(Node.MANAGING);
		mapping.put(Node.MANAGING, manage);
		manage.registerPrivilege("VIEW_MANAGEMENT_PAGE");

		PrivilegeNode allManage = new PrivilegeNode(Node.ALL_MANAGING);
		mapping.put(Node.ALL_MANAGING, allManage);
		allManage.registerPrivilege("VIEW_MANAGEMENT_PAGE");
		allManage.getChildren().add(manage);

		// Hierarchy
		PrivilegeNode hierarchyTopic = new PrivilegeNode(Node.HIERARCHY_TOPIC);
		hierarchyTopic.registerPrivilege("VIEW_HIERARCHY_TOPIC");
		hierarchyTopic.registerPrivilege("EDIT_HIERARCHY_TOPIC");
		hierarchyTopic.registerPrivilege("MODIFY_KEY_RESOURCE");
		mapping.put(Node.HIERARCHY_TOPIC, hierarchyTopic);

		// Institution
		PrivilegeNode institution = new PrivilegeNode(Node.INSTITUTION);
		List<PrivilegeNode> instChildren = institution.getChildren();
		instChildren.add(schemas);
		instChildren.add(itemDefinitions);
		instChildren.add(powerSearches);
		instChildren.add(dynaCollections);
		instChildren.add(workflows);
		instChildren.add(federatedSearches);
		instChildren.add(reports);
		instChildren.add(globalItemStatus);
		instChildren.add(allSystemSettings);
		instChildren.add(allManage);
		instChildren.add(hierarchyTopic);
		instChildren.add(courseInfo);
		instChildren.add(taxonomies);
		instChildren.add(harvesterProfiles);
		instChildren.add(portlets);
		instChildren.add(customLinks);
		instChildren.add(connectors);
		instChildren.add(oauthClients);
		instChildren.add(htmlEditorPluginss);
		instChildren.add(kalturas);
		instChildren.add(userScripts);
		instChildren.add(echos);
		instChildren.add(externalTools);
		instChildren.add(ltiConsumers);

		institution.registerPrivilege("EDIT_USER_MANAGEMENT");
		institution.registerPrivilege("VIEW_SECURITY_TREE");
		institution.registerPrivilege("EDIT_SECURITY_TREE");
		institution.registerPrivilege("SEARCH_PAGE");
		institution.registerPrivilege("DASHBOARD_PAGE");
		institution.registerPrivilege("HIERARCHY_PAGE");
		institution.registerPrivilege("INTEGRATION_SELECTION_SESSION");

		// OAuth hax
		institution.registerPrivilege("ADMINISTER_OAUTH_TOKENS");

		// If a workflow step appoints moderators ...?
		// Not sure we need this...  doesn't seem right
		//institution.registerPrivilege("MODERATE_ITEM");

		mapping.put(Node.INSTITUTION, institution);
	}

	private static void addPortletType(PrivilegeNode portlets, Node node)
	{
		PrivilegeNode n = new PrivilegeNode(node);
		n.registerPrivilege("CREATE_PORTLET");
		n.registerPrivilege("EDIT_PORTLET");
		n.registerPrivilege("DELETE_PORTLET");
		n.registerPrivilege("VIEW_PORTLET");

		portlets.getChildren().add(n);
		mapping.put(node, n);
	}

	private static PrivilegeNode buildEntity(Node childNode, Node parentNode, String type)
	{
		PrivilegeNode basic = buildBasic(childNode, parentNode, type);
		basic.registerPrivilege("LIST_" + type);
		basic.registerPrivilege("VIEW_" + type);
		return basic;
	}

	private static PrivilegeNode buildBasic(Node childNode, Node parentNode, String type)
	{
		PrivilegeNode child = new PrivilegeNode(childNode);
		child.registerPrivilege("EDIT_" + type);
		child.registerPrivilege("DELETE_" + type);

		PrivilegeNode parent = new PrivilegeNode(parentNode);
		parent.registerPrivilege("CREATE_" + type);
		parent.getChildren().add(child);

		mapping.put(childNode, child);
		mapping.put(parentNode, parent);

		return parent;
	}
}
