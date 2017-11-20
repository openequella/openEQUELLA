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

package com.tle.core.services.item.relation;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.Relation;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.AbstractWorkflowOperation;

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
