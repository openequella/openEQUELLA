package com.tle.web.htmleditor;

import java.util.Map;
import java.util.Set;

import com.tle.common.scripting.ScriptContextFactory;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.js.JSDisableable;

/**
 * @author aholland
 */
public interface HtmlEditorControl extends Section, HtmlRenderer, JSDisableable
{
	void setData(SectionInfo info, Map<String, String> properties, boolean restrictedCollections,
		boolean restrictedDynacolls, boolean restrictedSearches, boolean restrictedContributables,
		Map<Class<?>, Set<String>> searchableUuids, Set<String> contributableUuids, String formId,
		ScriptContextFactory scriptContextFactory)
		throws Exception;

	String getHtml(SectionInfo info);

	void setDefaultPropertyName(String defaultPropertyName);
}
