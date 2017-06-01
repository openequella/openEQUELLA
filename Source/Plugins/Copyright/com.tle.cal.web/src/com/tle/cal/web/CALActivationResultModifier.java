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

package com.tle.cal.web;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.item.Item;
import com.tle.cal.CALConstants;
import com.tle.cal.service.CALService;
import com.tle.core.guice.Bind;
import com.tle.web.activation.ActivationResultsModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;

@Bind
public class CALActivationResultModifier implements ActivationResultsModifier
{
	@Inject
	private CALService calService;

	@Override
	public void processSingle(SectionInfo info, ObjectNode link, String prefix, Item item, SelectedResource resource)
	{
		Map<String, String> attributes = item.getItemDefinition().getAttributes();
		if( Boolean.valueOf(attributes.get(CALConstants.KEY_USE_CITATION_AS_NAME)) )
		{
			CALHolding holding = calService.getHoldingForItem(item);
			Map<Long, List<CALPortion>> portions = calService.getPortionsForItems(Collections.singletonList(item));
			CALPortion calPortion = portions.get(item.getId()) == null ? null : portions.get(item.getId()).get(0);
			String citation = calService.citate(holding, calPortion);
			link.put(prefix + "name", Jsoup.parse(citation).text());
		}
	}

}
