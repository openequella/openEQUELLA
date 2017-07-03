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

package com.tle.core.dynacollection.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.beans.EntityScript;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.dynacollection.SearchSetAdapter;
import com.tle.common.search.searchset.SearchSet;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.collection.event.listener.ItemDefinitionDeletionListener;
import com.tle.core.dynacollection.DynaCollectionDao;
import com.tle.core.dynacollection.DynaCollectionReferencesEvent;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.guice.Bind;
import com.tle.core.schema.event.listener.SchemaDeletionListener;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.search.searchset.SearchSetService;
import com.tle.core.search.searchset.virtualisation.VirtualisationHelper;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind(DynaCollectionService.class)
@Singleton
@SecureEntity("DYNA_COLLECTION")
public class DynaCollectionServiceImpl
	extends
		AbstractEntityServiceImpl<EntityEditingBean, DynaCollection, DynaCollectionService>
	implements
		DynaCollectionService,
		ItemDefinitionDeletionListener,
		SchemaDeletionListener
{
	private final DynaCollectionDao dao;

	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(DynaCollectionServiceImpl.class);
	private static final String ASSERT_USAGE_ERR_MSG = helper.key("dynacollection.assertusage"); //$NON-NLS-1$

	@Inject
	private SearchSetService searchSetService;

	@Inject
	public DynaCollectionServiceImpl(DynaCollectionDao dao)
	{
		super(Node.DYNA_COLLECTION, dao);
		this.dao = dao;
	}

	@Override
	public List<VirtualisableAndValue<DynaCollection>> enumerateExpanded(String usage)
	{
		List<DynaCollection> unexpanded = Check.isEmpty(usage) ? dao.enumerateAll() : dao.enumerateForUsage(usage);
		return searchSetService.expandSearchSets(unexpanded, null, null, virtualisationHelper);
	}

	/**
	 * In the simple case, the identifier is simply the uuid and the result is the same as getByUuid.
	 * Otherwise the expression is a compound of uuid : virtualized value, and so we call on the dao
	 * to retrieve by uuid and then the searchSetService to expand those values and return a match on
	 * the compound value, if such exists at the present time
	 */
	@Override
	public VirtualisableAndValue<DynaCollection> getByCompoundId(String compoundId)
	{
		String[] deconstructedCompound = compoundId.split(":");
		DynaCollection unexpanded = dao.getByUuid(deconstructedCompound[0]);
		VirtualisableAndValue<DynaCollection> retval = null;
		if( deconstructedCompound.length == 1 )
		{
			retval = new VirtualisableAndValue<DynaCollection>(unexpanded);
		}
		else
		{
			List<VirtualisableAndValue<DynaCollection>> expanded = searchSetService
				.expandSearchSets(Collections.singletonList(unexpanded), null, null, virtualisationHelper);
			for( VirtualisableAndValue<DynaCollection> expand : expanded )
			{
				String virtual = expand.getVirtualisedValue();
				if( virtual != null && virtual.equals(deconstructedCompound[1]) )
				{
					retval = expand;
					break;
				}
			}
		}
		return retval;
	}

	@Override
	public void assertUsage(DynaCollection dc, String usage)
	{
		if( dc.getUsageIds() == null || !dc.getUsageIds().contains(usage) )
		{
			throw new RuntimeException(ASSERT_USAGE_ERR_MSG);
		}
	}

	@Override
	public String getFreeTextQuery(DynaCollection dc)
	{
		return searchSetService.getFreetextQuery(new SearchSetAdapter(dc));
	}

	@Override
	public FreeTextBooleanQuery getSearchClause(DynaCollection dc, String vv)
	{
		Map<String, String> vvmap = vv == null ? null : Collections.singletonMap(dc.getUuid(), vv);
		return searchSetService.getSearchClauses(new SearchSetAdapter(dc), vvmap);
	}

	@Override
	public FreeTextBooleanQuery getSearchClausesNoVirtualisation(DynaCollection dc)
	{
		return searchSetService.getSearchClausesNoVirtualisation(new SearchSetAdapter(dc));
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, DynaCollection> session, DynaCollection entity,
		List<ValidationError> errors)
	{
		// Nothing to validate
	}

	private final VirtualisationHelper<DynaCollection> virtualisationHelper = new VirtualisationHelper<DynaCollection>()
	{
		@Override
		public SearchSet getSearchSet(DynaCollection obj)
		{
			return new SearchSetAdapter(obj);
		}

		@Override
		public DynaCollection newFromPrototypeForValue(DynaCollection obj, String value)
		{
			DynaCollection ndc = new DynaCollection();
			ndc.setUuid(obj.getUuid());
			ndc.setName(newLanguageBundleForValue(obj.getName(), value));
			ndc.setDescription(LanguageBundle.clone(obj.getDescription()));
			ndc.setVirtualisationId(obj.getVirtualisationId());
			ndc.setVirtualisationPath(obj.getVirtualisationPath());
			return ndc;
		}

		@Override
		@SuppressWarnings("nls")
		protected String modifyString(String text, String value)
		{
			return text + " (" + value + ")";
		}
	};

	@Override
	@SecureOnReturn(priv = "SEARCH_DYNA_COLLECTION")
	public List<BaseEntityLabel> listSearchable()
	{
		return listAll();
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		final DynaCollectionReferencesEvent event = new DynaCollectionReferencesEvent(get(id));
		publishEvent(event);
		return event.getReferencingClasses();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void removeReferences(ItemDefinition entity)
	{
		for( DynaCollection dc : dao.getDynaCollectionsReferencingItemDefinition(entity) )
		{
			removeEntity(dc.getItemDefs(), entity);
			dao.update(dc);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void removeReferences(Schema schema)
	{
		for( DynaCollection dc : dao.getDynaCollectionsReferencingSchema(schema) )
		{
			removeEntity(dc.getSchemas(), schema);
			dao.update(dc);
		}
	}

	private <T extends BaseEntity, U extends EntityScript<T>> void removeEntity(List<U> queries, T entity)
	{
		for( Iterator<U> iter = queries.iterator(); iter.hasNext(); )
		{
			EntityScript<T> script = iter.next();
			if( Objects.equals(script.getEntity(), entity) )
			{
				iter.remove();
			}
		}
	}

	@Override
	protected void processClone(EntityPack<DynaCollection> pack)
	{
		DynaCollection dc = pack.getEntity();
		dc.setUsageIds(Sets.newHashSet(dc.getUsageIds()));
		dc.setSchemas(Lists.newArrayList(dc.getSchemas()));
		dc.setItemDefs(Lists.newArrayList(dc.getItemDefs()));
	}

	@Override
	protected void preUnlinkForClone(DynaCollection dc)
	{
		Hibernate.initialize(dc.getUsageIds());
		Hibernate.initialize(dc.getSchemas());
		Hibernate.initialize(dc.getItemDefs());
	}
}
