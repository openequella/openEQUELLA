package com.tle.web.integration;

import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;

@NonNullByDefault
public interface IntegrationSessionExtension
{
	void setupSession(SectionInfo info, SelectionSession session, SingleSignonForm form);

	void processResultForSingle(SectionInfo info, SelectionSession session, Map<String, String> params, String prefix,
		IItem<?> item, SelectedResource resource);

	void processResultForMultiple(SectionInfo info, SelectionSession session, ObjectNode link, IItem<?> item,
		SelectedResource resource);
}
