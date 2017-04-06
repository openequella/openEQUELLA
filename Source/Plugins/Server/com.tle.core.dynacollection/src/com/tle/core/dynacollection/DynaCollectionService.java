package com.tle.core.dynacollection;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.dynacollection.RemoteDynaCollectionService;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;

public interface DynaCollectionService
	extends
		AbstractEntityService<EntityEditingBean, DynaCollection>,
		RemoteDynaCollectionService
{
	List<VirtualisableAndValue<DynaCollection>> enumerateExpanded(String usage);

	void assertUsage(DynaCollection dc, String usage);

	String getFreeTextQuery(DynaCollection dc);

	FreeTextBooleanQuery getSearchClause(DynaCollection dc, String virtualisationValue);

	FreeTextBooleanQuery getSearchClausesNoVirtualisation(DynaCollection dc);

	List<BaseEntityLabel> listSearchable();

	VirtualisableAndValue<DynaCollection> getByCompoundId(String compoundId);
}
