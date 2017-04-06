package com.tle.core.services.item.relation;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.Relation;
import com.tle.core.guice.BindFactory;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class RelationOperation extends AbstractWorkflowOperation // NOSONAR
{
	@Inject
	private RelationService relationService;

	private final RelationOperationState state;

	@AssistedInject
	private RelationOperation(@Assisted RelationOperationState state)
	{
		this.state = state;
	}

	@Override
	public boolean execute()
	{
		for( Relation relation : state.getAdds() )
		{
			relation.setId(0);
			relation.setFirstItem(getItem());
			Item secondItem = itemService.get(relation.getSecondItem().getItemId());
			relation.setSecondItem(secondItem);
			relationService.saveRelation(relation);
		}
		for( Relation relation : state.getModifies() )
		{
			relationService.updateRelation(relation);
		}
		for( long relationId : state.getDeletes() )
		{
			relationService.delete(relationService.getById(relationId));
		}
		return false;
	}

	@BindFactory
	public interface RelationOperationFactory
	{
		RelationOperation create(RelationOperationState state);
	}

}
