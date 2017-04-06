package com.tle.core.services.item.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.Relation;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.guice.Bind;
import com.tle.core.qti.QtiConstants;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.operations.DuringSaveOperation;
import com.tle.core.workflow.operations.DuringSaveOperationGenerator;

@Bind
public class CloneRelationOperation extends AbstractWorkflowOperation implements DuringSaveOperationGenerator
{
	@Inject
	private RelationService relationService;

	private RelationOperationState state = new RelationOperationState();

	@Override
	public boolean execute()
	{
		ItemPack<Item> pack = getItemPack();
		Item origItem = pack.getOriginalItem();

		List<Attachment> attachments = pack.getItem().getAttachments();
		for( Attachment attachment : attachments )
		{
			if( attachment.getData(QtiConstants.KEY_TEST_UUID) != null )
			{
				attachment.setData(QtiConstants.KEY_TEST_UUID, UUID.randomUUID().toString());
			}
		}

		if( origItem != null )
		{
			Collection<Relation> relations = relationService.getAllByFromItem(origItem);
			for( Relation relation : relations )
			{
				state.add(relation.getSecondItem().getItemId(), relation.getRelationType(),
					relation.getSecondResource());
			}
		}

		return false;
	}

	@Override
	public Collection<DuringSaveOperation> getDuringSaveOperation()
	{
		DuringSaveOperation relMod = new RelationModify(state);
		List<DuringSaveOperation> ops = new ArrayList<DuringSaveOperation>();
		ops.add(relMod);
		return ops;
	}
}
