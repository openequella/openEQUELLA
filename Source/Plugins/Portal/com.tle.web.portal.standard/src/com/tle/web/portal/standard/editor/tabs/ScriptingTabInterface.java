package com.tle.web.portal.standard.editor.tabs;

import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.standard.model.TabSection;

@NonNullByDefault
public interface ScriptingTabInterface extends TabSection
{
	void customLoad(SectionInfo info, PortletEditingBean portlet);

	void customSave(SectionInfo info, PortletEditingBean portlet);

	void customClear(SectionInfo info);

	void customValidate(SectionInfo info, Map<String, Object> errors);

	JSStatements getTabShowStatements();

}
