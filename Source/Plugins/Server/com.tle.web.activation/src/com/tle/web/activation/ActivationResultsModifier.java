package com.tle.web.activation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.beans.item.Item;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;

public interface ActivationResultsModifier
{
	public void processSingle(SectionInfo info, ObjectNode link, String prefix, Item item, SelectedResource resource);
}
