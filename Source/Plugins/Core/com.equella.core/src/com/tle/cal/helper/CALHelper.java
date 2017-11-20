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

package com.tle.cal.helper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.item.Item;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.cal.service.CALService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.AbstractHelper;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CALHelper extends AbstractHelper
{
	@Inject
	private CALService calService;
	@Inject
	private ActivationService activationService;

	@Override
	public void load(PropBagEx itemxml, Item item)
	{
		if( calService.isCopyrightedItem(item) )
		{
			PropBagEx activations = itemxml.aquireSubtree("activations");
			activations.deleteAll(Constants.XML_WILD);
			for( ActivateRequest activation : activationService.getAllRequests(calService.getActivationType(), item) )
			{
				PropBagEx activeXML = activations.newSubtree("activation");
				setNode(activeXML, "@uuid", activation.getUuid());
				setNode(activeXML, "@status",
					CurrentLocale.get(activationService.getStatusKey(activation.getStatus())));

				setNode(activeXML, "attachment", activation.getAttachment());

				CourseInfo course = activation.getCourse();
				setNode(activeXML, "coursename", CurrentLocale.get(course.getName()));
				setNode(activeXML, "coursecode", course.getCode());

				setNode(activeXML, "startdate", activation.getFrom());
				setNode(activeXML, "enddate", activation.getUntil());

				CALHolding holding = calService.getHoldingForItem(item);
				Map<Long, List<CALPortion>> portions = calService.getPortionsForItems(Collections.singletonList(item));
				CALPortion portion = portions.get(item.getId()) == null ? null : portions.get(item.getId()).get(0);
				String citation = calService.citate(holding, portion);
				setNode(activeXML, "citation", citation);
			}
		}
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		// nothing
	}

}
