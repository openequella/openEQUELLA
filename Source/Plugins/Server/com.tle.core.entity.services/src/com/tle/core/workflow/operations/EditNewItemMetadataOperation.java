package com.tle.core.workflow.operations;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.dytech.edge.exceptions.OperationException;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.common.Check;
import com.tle.common.security.SecurityConstants;
import com.tle.core.dao.ItemDao;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.user.CurrentUser;

/**
 * Same as EditMetadataOperation, but different required privs
 * 
 * @author aholland
 */
@Bind
@SecureOnCall(priv = SecurityConstants.CREATE_ITEM)
public class EditNewItemMetadataOperation extends AbstractEditMetadataOperation
{
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private ItemDao itemDao;

	private ItemStatus initialStatus;

	public void setInitialStatus(ItemStatus initialStatus)
	{
		this.initialStatus = initialStatus;
	}

	@Override
	protected void checkExistence()
	{
		if( params.isUpdate() )
		{
			throw new OperationException(
				"com.tle.core.workflow.operations.editmeta.error.itemexists", params.getItemKey()); //$NON-NLS-1$
		}
	}

	@Override
	protected void ensureItemInternal(Item newItem)
	{
		ItemKey itemkey = params.getItemKey();

		params.setItemPack(newPack);

		String uuid = itemkey.getUuid();
		if( uuid.length() == 0 )
		{
			uuid = UUID.randomUUID().toString();
		}
		else
		{
			// Make sure valid uuid
			UUID.fromString(uuid);
		}

		itemkey = new ItemId(uuid, itemkey.getVersion());
		params.setItemKey(itemkey, 0);
		newItem.setId(0);
		newItem.setUuid(uuid);
		newItem.setVersion(itemkey.getVersion());
		newItem.setDateCreated(new Date());

		newItem.setItemDefinition(itemDefinitionService.get(newItem.getItemDefinition().getId()));
		if( itemkey.getVersion() > 1 )
		{
			long collectionId = itemDao.getCollectionIdForUuid(itemkey.getUuid());
			if( collectionId != 0 && newItem.getItemDefinition().getId() != collectionId )
			{
				throw new OperationException("com.tle.core.workflow.operations.editmeta.error.cannotchangecollection"); //$NON-NLS-1$
			}
		}

		boolean noOwner = Check.isEmpty(newItem.getOwner());
		if( noOwner )
		{
			newItem.setOwner(CurrentUser.getUserID());
		}

		newItem.setStatus(null);
		newItem.setStatus(initialStatus);

		relinkAttachments(newItem.getAttachments(), newItem.getTreeNodes());
		createHistory(Type.contributed);
	}

	/**
	 * 1. Ensures any attachments that no longer exist are not referred to in
	 * tree nodes.
	 * 
	 * @param attachments
	 * @param treeNodes
	 */
	private void relinkAttachments(List<Attachment> attachments, List<ItemNavigationNode> treeNodes)
	{
		Map<String, Attachment> attmap = new HashMap<String, Attachment>();
		for( Attachment a : attachments )
		{
			attmap.put(a.getUuid(), a);
		}

		for( ItemNavigationNode node : treeNodes )
		{
			List<ItemNavigationTab> tabs = node.getTabs();
			if( !Check.isEmpty(tabs) )
			{
				for( ItemNavigationTab tab : tabs )
				{
					Attachment attach = tab.getAttachment();
					if( attach != null )
					{
						tab.setAttachment(attmap.get(attach.getUuid()));
					}
				}
			}
		}
	}

}
