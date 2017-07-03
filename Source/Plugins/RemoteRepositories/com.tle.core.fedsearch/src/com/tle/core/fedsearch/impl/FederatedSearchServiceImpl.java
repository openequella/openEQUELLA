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

package com.tle.core.fedsearch.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.tle.core.services.ValidationHelper;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.EntityPack;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.fedsearch.FedSearchPrivileges;
import com.tle.core.fedsearch.FederatedSearchDao;
import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.core.guice.Bind;
import com.tle.core.remoting.RemoteFederatedSearchService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;

@SuppressWarnings("nls")
@Bind(FederatedSearchService.class)
@Singleton
@SecureEntity(RemoteFederatedSearchService.ENTITY_TYPE)
public class FederatedSearchServiceImpl
	extends
		AbstractEntityServiceImpl<EntityEditingBean, FederatedSearch, FederatedSearchService>
	implements
		FederatedSearchService
{
	private static final String[] BLANKS = {"name", "type"};

	private final FederatedSearchDao federatedSearchDao;

	@Inject
	public FederatedSearchServiceImpl(FederatedSearchDao federatedSearchDao)
	{
		super(Node.FEDERATED_SEARCH, federatedSearchDao);
		this.federatedSearchDao = federatedSearchDao;
	}

	@Override
	@SecureOnReturn(priv = FedSearchPrivileges.SEARCH_FEDERATED_SEARCH)
	public List<BaseEntityLabel> listSearchable()
	{
		return listAll();
	}

	@Override
	@SecureOnReturn(priv = FedSearchPrivileges.SEARCH_FEDERATED_SEARCH)
	public List<BaseEntityLabel> listEnabledSearchable()
	{
		return federatedSearchDao.listEnabled();
	}

	@Override
	public List<Long> findEngineNamesForType(String type)
	{
		return federatedSearchDao.findEngineNamesByType(type);
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, FederatedSearch> session,
		FederatedSearch entity, List<ValidationError> errors)
	{
		ValidationHelper.checkBlankFields(entity, BLANKS, errors);
	}

	@Override
	protected void processClone(EntityPack<FederatedSearch> pack)
	{
		FederatedSearch searchGateway = pack.getEntity();
		searchGateway.setAttributes(searchGateway.getAttributes());
	}

	@Override
	@SecureOnReturn(priv = FedSearchPrivileges.SEARCH_FEDERATED_SEARCH)
	public FederatedSearch getForSearching(String uuid)
	{
		return getByUuid(uuid);
	}

	@Override
	@Transactional
	@SecureOnReturn(priv = FedSearchPrivileges.SEARCH_FEDERATED_SEARCH)
	public List<FederatedSearch> getForCollectionUuid(String uuid)
	{
		return federatedSearchDao.findAllByCriteria(Restrictions.eq("collectionUuid", uuid), getInstitutionCriterion());
	}

	@Override
	@SecureOnReturn(priv = FedSearchPrivileges.SEARCH_FEDERATED_SEARCH)
	public List<FederatedSearch> enumerateSearchable()
	{
		return enumerate();
	}

	@Override
	@SecureOnReturn(priv = FedSearchPrivileges.SEARCH_FEDERATED_SEARCH)
	public List<FederatedSearch> enumerateEnabledSearchable()
	{
		return findAllWithCriterion(Restrictions.eq("disabled", false));
	}
}
