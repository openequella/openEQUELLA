package com.tle.core.fedsearch;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.FederatedSearch;
import com.tle.core.remoting.RemoteFederatedSearchService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;

public interface FederatedSearchService
	extends
		AbstractEntityService<EntityEditingBean, FederatedSearch>,
		RemoteFederatedSearchService
{
	List<BaseEntityLabel> listSearchable();

	List<BaseEntityLabel> listEnabledSearchable();

	List<Long> findEngineNamesForType(String type);

	FederatedSearch getForSearching(String uuid);

	List<FederatedSearch> getForCollectionUuid(String uuid);

	List<FederatedSearch> enumerateSearchable();

	List<FederatedSearch> enumerateEnabledSearchable();
}
