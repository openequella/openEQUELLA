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
