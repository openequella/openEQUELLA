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

package com.tle.core.powersearch.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.EntityPack;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.collection.event.listener.ItemDefinitionDeletionListener;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.guice.Bind;
import com.tle.core.powersearch.PowerSearchDao;
import com.tle.core.powersearch.PowerSearchService;
import com.tle.core.powersearch.event.PowerSearchDeletionEvent;
import com.tle.core.remoting.RemotePowerSearchService;
import com.tle.core.schema.SchemaReferences;
import com.tle.core.schema.event.listener.SchemaReferencesListener;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.ValidationHelper;

/**
 * @author Nicholas Read
 */
@Bind(PowerSearchService.class)
@Singleton
@SecureEntity(RemotePowerSearchService.ENTITY_TYPE)
public class PowerSearchServiceImpl
	extends
		AbstractEntityServiceImpl<EntityEditingBean, PowerSearch, PowerSearchService>
	implements
		PowerSearchService,
		SchemaReferences,
		SchemaReferencesListener,
		ItemDefinitionDeletionListener
{
	private static final String[] BLANKS = {"name"}; //$NON-NLS-1$

	private final PowerSearchDao powerSearchDao;

	@Inject
	public PowerSearchServiceImpl(PowerSearchDao powerSearchDao)
	{
		super(Node.POWER_SEARCH, powerSearchDao);
		this.powerSearchDao = powerSearchDao;
	}

	@Override
	protected void deleteReferences(PowerSearch entity)
	{
		publishEvent(new PowerSearchDeletionEvent(entity));
	}

	@Override
	@SecureOnReturn(priv = "SEARCH_POWER_SEARCH")
	public List<BaseEntityLabel> listSearchable()
	{
		return listAll();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.entity.PowerSearchService#listAllForSchema(long)
	 */
	@Override
	public List<BaseEntityLabel> listAllForSchema(long schemaId)
	{
		return powerSearchDao.listAllForSchema(schemaId);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.PowerSearchService#enumerateForSchema(com.dytech
	 * .edge.user.UserState, long)
	 */
	@Override
	public List<BaseEntityLabel> listAllForSchema(Schema schema)
	{
		return listAllForSchema(schema.getId());
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void removeReferences(ItemDefinition itemDefinition)
	{
		for( PowerSearch psearch : powerSearchDao.getPowerSearchesReferencingItemDefinition(itemDefinition) )
		{
			psearch.getItemdefs().remove(itemDefinition);
			powerSearchDao.saveOrUpdate(psearch);
		}
	}

	@Override
	protected void preUnlinkForClone(PowerSearch powerSearch)
	{
		Hibernate.initialize(powerSearch.getItemdefs());
	}

	@Override
	protected void processClone(EntityPack<PowerSearch> pack)
	{
		PowerSearch psearch = pack.getEntity();
		psearch.setItemdefs(new ArrayList<ItemDefinition>(psearch.getItemdefs()));
	}

	@Override
	public List<Long> enumerateItemdefIds(long powerSearchId)
	{
		return powerSearchDao.enumerateItemdefIds(powerSearchId);
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, PowerSearch> session, PowerSearch entity,
		List<ValidationError> errors)
	{
		ValidationHelper.checkBlankFields(entity, BLANKS, errors);
	}

	@Override
	public List<BaseEntityLabel> getSchemaUses(long id)
	{
		return listAllForSchema(id);
	}

	@Override
	public void addSchemaReferencingClasses(Schema schema, List<Class<?>> referencingClasses)
	{
		if( powerSearchDao.listAllForSchema(schema.getId()).size() > 0 )
		{
			referencingClasses.add(PowerSearch.class);
		}
	}
}
