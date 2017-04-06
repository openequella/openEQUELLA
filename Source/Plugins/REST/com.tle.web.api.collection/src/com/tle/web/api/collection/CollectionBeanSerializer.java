package com.tle.web.api.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.DynamicMetadataRule;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.beans.item.ItemStatus;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.ItemStatusTarget;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.api.collection.impl.CollectionEditorImpl.CollectionEditorFactory;
import com.tle.web.api.collection.interfaces.beans.CollectionBean;
import com.tle.web.api.collection.interfaces.beans.CollectionSecurityBean;
import com.tle.web.api.collection.interfaces.beans.ItemMetadataSecurityBean;
import com.tle.web.api.interfaces.beans.security.DynamicRuleBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class CollectionBeanSerializer
	extends
		AbstractEquellaBaseEntitySerializer<ItemDefinition, CollectionBean, CollectionEditor>
{
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private CollectionEditorFactory editorFactory;

	@Override
	protected CollectionBean createBean()
	{
		return new CollectionBean();
	}

	@Override
	protected ItemDefinition createEntity()
	{
		return new ItemDefinition();
	}

	@Override
	protected CollectionEditor createNewEditor(ItemDefinition entity, @Nullable String stagingUuid, boolean importing)
	{
		return editorFactory.createNewEditor(entity, stagingUuid, importing);
	}

	@Override
	protected CollectionEditor createExistingEditor(ItemDefinition entity, @Nullable String stagingUuid,
		@Nullable String lockId, boolean importing)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	private List<DynamicRuleBean> getDynamicRules(ItemDefinition entity)
	{
		List<DynamicRuleBean> dynamicRules = new ArrayList<DynamicRuleBean>();
		List<DynamicMetadataRule> dynamicMetadataRules = entity.getDynamicMetadataRules();
		if( dynamicMetadataRules != null )
		{
			for( DynamicMetadataRule rule : dynamicMetadataRules )
			{
				DynamicRuleBean dynamicRule = new DynamicRuleBean();
				dynamicRule.setName(rule.getName());
				dynamicRule.setPath(rule.getPath());
				dynamicRule.setType(rule.getType());
				List<TargetListEntryBean> targetListEntryBean = convertToTargetListEntryBean(rule.getTargetList());
				dynamicRule.setTargetList(targetListEntryBean);
				dynamicRules.add(dynamicRule);
			}
		}

		return dynamicRules;
	}

	private Map<String, ItemMetadataSecurityBean> getItemMetadataRules(ItemDefinition entity)
	{
		Map<String, ItemMetadataSecurityBean> rules = new HashMap<String, ItemMetadataSecurityBean>();
		List<ItemMetadataRule> itemMetadataRules = entity.getItemMetadataRules();
		if( itemMetadataRules != null )
		{
			for( ItemMetadataRule rule : itemMetadataRules )
			{
				ItemMetadataSecurityBean securityBean = new ItemMetadataSecurityBean();
				securityBean.setName(rule.getName());
				securityBean.setScript(rule.getScript());

				TargetList targetList = aclManager.getTargetList(Node.ITEM_METADATA,
					new ItemMetadataTarget(rule.getId(), entity));
				List<TargetListEntryBean> entries = convertToTargetListEntryBean(targetList);
				securityBean.setEntries(entries);
				rules.put(rule.getId(), securityBean);
			}
		}

		return rules;
	}

	private Map<String, List<TargetListEntryBean>> getItemStatusesRules(ItemDefinition entity)
	{
		Map<String, List<TargetListEntryBean>> rules = new HashMap<String, List<TargetListEntryBean>>();
		for( ItemStatus status : ItemStatus.values() )
		{
			TargetList targetList = aclManager.getTargetList(Node.ITEM_STATUS, new ItemStatusTarget(status, entity));
			if( !targetList.getEntries().isEmpty() )
			{
				List<TargetListEntryBean> entries = convertToTargetListEntryBean(targetList);
				rules.put(status.toString(), entries);
			}
		}
		return rules;
	}

	@Override
	protected void copyCustomFields(ItemDefinition entity, CollectionBean bean, Object data)
	{
		CollectionSecurityBean security = new CollectionSecurityBean();

		security.setStatuses(getItemStatusesRules(entity));
		security.setMetadata(getItemMetadataRules(entity));
		security.setDynamicRules(getDynamicRules(entity));

		List<TargetListEntryBean> entityACLs = getEntityACLs(entity);
		if( entityACLs != null )
		{
			security.setRules(entityACLs);
		}

		bean.setSecurity(security);

		bean.setSchema(new BaseEntityReference(entity.getSchema().getUuid()));
	}

	@Override
	protected AbstractEntityService<?, ItemDefinition> getEntityService()
	{
		return collectionService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.COLLECTION;
	}
}
