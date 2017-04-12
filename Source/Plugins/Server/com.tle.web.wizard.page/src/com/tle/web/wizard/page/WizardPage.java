package com.tle.web.wizard.page;

import java.util.List;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.queries.FreeTextQuery;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.WizardPageException;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;

public interface WizardPage
{
	List<String> getCriteriaList();

	void setState(WebWizardPageState webWizardPageState);

	void createPage() throws WizardPageException;

	void ensureTreeAdded(SectionInfo info, boolean submitWizard);

	void loadFromDocument(SectionInfo info);

	void saveToDocument(SectionInfo info) throws Exception;

	PropBagEx getDocBag();

	void setWizardPage(DefaultWizardPage wizard);

	void setReloadFunction(JSCallable reloadFunction);

	FreeTextQuery getPowerSearchQuery();

	void setRepository(LERepository repos);

	List<ControlResult> renderPage(RenderContext context);

	Map<String, List<ControlResult>> renderPage(RenderContext context, AjaxUpdateData data, String rootId);

	void setPageNumber(int i);

	void init();

	void ensureTreeAdded(SectionInfo info);

	void setSubmitted(boolean submitted);

	void setShowMandatory(boolean showMandatory);

	boolean isValid();

	void saveDefaults() throws Exception;

	List<? extends SectionId> getRootIds();

}
