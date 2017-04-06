package com.tle.core.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tle.beans.entity.itemdef.DynamicMetadataRule;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.security.WorkflowTaskDynamicTarget;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.scripting.WorkflowScriptConstants;
import com.tle.core.scripting.WorkflowScriptContextParams;
import com.tle.core.scripting.WorkflowScriptObjectContributor;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.ItemDynamicMetadataTarget;
import com.tle.core.services.WorkflowOperationService;
import com.tle.core.services.user.UserService;

/**
 * @author aholland
 */
@Bind(WorkflowOperationService.class)
@Singleton
@SuppressWarnings("nls")
public class WorkflowOperationServiceImpl implements WorkflowOperationService
{
	private static final Logger LOGGER = Logger.getLogger(WorkflowOperationService.class);
	private static final String ITEM_METADATA_SECURITY_RULE_SCRIPT_NAME = "saveSecurityMetdataRule";

	@Inject
	private ScriptingService scriptingService;
	@Inject
	private UserService userService;
	@Inject
	private TLEAclManager aclManager;

	private PluginTracker<WorkflowScriptObjectContributor> scriptObjectTracker;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		scriptObjectTracker = new PluginTracker<WorkflowScriptObjectContributor>(pluginService,
			WorkflowOperationService.class, "scriptObjects", "id");
		scriptObjectTracker.setBeanKey("class");
	}

	@Override
	public ScriptContext createScriptContext(ItemPack itemPack, FileHandle fileHandle,
		Map<String, Object> attributes, Map<String, Object> extraObjects)
	{
		WorkflowScriptContextParams params = new WorkflowScriptContextParams(this, itemPack, fileHandle,
			attributes);

		ScriptContext scriptContext = scriptingService.createScriptContext(params);

		// now add our own bits...
		Map<String, Object> objects = Maps.newHashMap();
		if( extraObjects != null )
		{
			objects.putAll(extraObjects);
		}
		for( WorkflowScriptObjectContributor contrib : scriptObjectTracker.getBeanList() )
		{
			contrib.addWorkflowScriptObjects(objects, params);
		}
		for( Map.Entry<String, Object> entry : objects.entrySet() )
		{
			scriptContext.addScriptObject(entry.getKey(), entry.getValue());
		}

		return scriptContext;
	}

	@Override
	public void addWorkflowScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params)
	{
		Map<String, Object> attributes = params.getAttributes();

		PropBagEx newXml = (PropBagEx) attributes.get(WorkflowScriptConstants.NEW_XML);
		if( newXml != null )
		{
			objects.put(WorkflowScriptConstants.NEW_XML, new PropBagWrapper(newXml));
		}
	}

	@Override
	public boolean evaluateScript(String script, String scriptName, ScriptContext context)
	{
		return scriptingService.evaluateScript(script, scriptName, context);
	}

	@Override
	public Object executeScript(String script, String scriptName, ScriptContext context, boolean function)
	{
		return scriptingService.executeScript(script, scriptName, context, function);
	}

	@Override
	public Object executeScript(String script, String scriptName, ScriptContext context, boolean function,
		Class<?> expecedReturnType)
	{
		return scriptingService.executeScript(script, scriptName, context, function, expecedReturnType);
	}

	@Override
	public boolean isAnOwner(Item item, String userUuid)
	{
		if( item.getOwner().equals(userUuid) )
		{
			return true;
		}

		return item.getCollaborators().contains(userUuid);
	}

	@Override
	public UserBean getOwner(Item item)
	{
		UserBean owner = null;
		String ownerID = item.getOwner();
		try
		{
			owner = userService.getInformationForUser(ownerID);
		}
		catch( Exception e )
		{
			LOGGER.error("Error getting user", e);
		}

		if( owner == null )
		{
			owner = new DefaultUserBean(ownerID, ownerID, ownerID, ownerID, ownerID);
		}

		return owner;
	}

	@Override
	public void updateMetadataBasedSecurity(PropBagEx itemxml, Item item)
	{
		Workflow workflow = item.getItemDefinition().getWorkflow();
		WorkflowTaskDynamicTarget itemTask = new WorkflowTaskDynamicTarget(item.getId());

		List<TargetListEntry> entries = acquireTargetListEntries(workflow, itemxml, item);

		TargetList targetList = new TargetList();
		targetList.setPartial(true);
		targetList.setEntries(entries);
		aclManager.setTargetList(Node.WORKFLOW_TASK, itemTask, targetList);

		setItemMetadataSecurityTargets(itemxml, item);

		// Dynamic ACLs
		List<DynamicMetadataRule> dynamicMetadataRules = item.getItemDefinition().getDynamicMetadataRules();

		if( !Check.isEmpty(dynamicMetadataRules) )
		{
			// Map of Lists<Privilege, List<Pair<Grant/Revoke, UserID>>>
			Multimap<String, Pair<Boolean, String>> privMultimap = ArrayListMultimap.create();

			for( DynamicMetadataRule rule : dynamicMetadataRules )
			{
				List<String> ids = Lists.newArrayList(itemxml.getNodeList(rule.getPath()));

				for( String id : ids )
				{
					for( TargetListEntry tle : rule.getTargetList().getEntries() )
					{
						privMultimap.put(tle.getPrivilege(), new Pair<Boolean, String>(tle.isGranted(),
							SecurityConstants.getRecipient(SecurityConstants.getRecipientType(rule.getType()), id)));
					}
				}
			}

			List<TargetListEntry> tlEntries = Lists.newArrayList();

			// Create target list
			for( String privilege : privMultimap.keySet() )
			{
				Collection<Pair<Boolean, String>> listOfGrants = privMultimap.get(privilege);

				if( !Check.isEmpty(listOfGrants) )
				{
					getTargetListEntriesFromGrants(tlEntries, listOfGrants, privilege);
				}
			}
			TargetList dynaMetaTargetList = new TargetList(tlEntries);

			aclManager.setTargetList(Node.DYNAMIC_ITEM_METADATA, new ItemDynamicMetadataTarget(item),
				dynaMetaTargetList);
		}
		else
		{
			aclManager.setTargetList(Node.DYNAMIC_ITEM_METADATA, new ItemDynamicMetadataTarget(item), null);
		}
	}

	// Sonar - cyclomatic complexity reduction ...
	private List<TargetListEntry> acquireTargetListEntries(Workflow workflow, PropBagEx itemxml, Item item)
	{
		List<TargetListEntry> entries = new ArrayList<TargetListEntry>();
		if( workflow != null && item.isModerating() )
		{
			Map<String, WorkflowItem> items = workflow.getAllWorkflowItems();
			for( WorkflowItem task : items.values() )
			{
				String taskId = task.getUuid();
				String path = task.getUserPath();
				if( !Check.isEmpty(path) )
				{
					for( String userId : itemxml.getNodeList(path) )
					{
						String recipient = SecurityConstants.getRecipient(Recipient.USER, userId);
						entries.add(new TargetListEntry(true, false, "MODERATE_ITEM", recipient, taskId));
						entries.add(new TargetListEntry(true, false, "VIEW_ITEM", recipient, taskId));
						if( task.isAllowEditing() )
						{
							entries.add(new TargetListEntry(true, false, "EDIT_ITEM", recipient, taskId));
						}
					}
				}
			}
		}
		return entries;
	}

	// Sonar - cyclomatic complexity reduction ...
	private void setItemMetadataSecurityTargets(PropBagEx itemxml, Item item)
	{
		List<String> results = Lists.newArrayList();
		List<ItemMetadataRule> itemMetadataRules = item.getItemDefinition().getItemMetadataRules();

		if( itemMetadataRules != null )
		{
			for( ItemMetadataRule rule : itemMetadataRules )
			{
				ItemPack itemPack = new ItemPack();
				itemPack.setItem(item);
				itemPack.setXml(itemxml);
				ScriptContext context = createScriptContext(itemPack, null, null, null);
				if( evaluateScript(rule.getScript(), ITEM_METADATA_SECURITY_RULE_SCRIPT_NAME + rule.getName(), context) )
				{
					results.add(rule.getId());
				}
			}
		}
		item.setMetadataSecurityTargets(results);
	}

	// Sonar - cyclomatic complexity reduction ...
	private void getTargetListEntriesFromGrants(List<TargetListEntry> tlEntries,
		Collection<Pair<Boolean, String>> listOfGrants, String privilege)
	{
		TargetListEntry tle = null;
		Boolean last = null;
		int count = 0;

		for( Pair<Boolean, String> privEntry : listOfGrants )
		{
			Boolean grant = privEntry.getFirst();
			String who = privEntry.getSecond();

			if( last == null )
			{
				last = grant;
				tle = new TargetListEntry(grant.booleanValue(), false, privilege, who);
				count++;
			}
			else if( last.equals(grant) && tle != null )
			{
				String existingWho = tle.getWho();
				tle.setWho(existingWho + (count > 1 ? " OR " + who : " " + who));
				count++;
			}
			else
			{
				tlEntries.add(tle);
				last = grant;
				tle = new TargetListEntry(grant.booleanValue(), false, privilege, who);
				count = 0;
			}
		}

		if( tle != null && count > 1 )
		{
			tle.setWho(tle.getWho() + " OR");
		}

		tlEntries.add(tle);
	}
}
