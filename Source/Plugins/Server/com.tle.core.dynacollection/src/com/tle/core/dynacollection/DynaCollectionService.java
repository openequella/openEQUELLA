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

package com.tle.core.dynacollection;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.dynacollection.RemoteDynaCollectionService;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.search.VirtualisableAndValue;

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
