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

package com.tle.web.htmleditor.tinymce.addon.tle.selection.callback;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.web.htmleditor.tinymce.addon.tle.AbstractSelectionAddon;
import com.tle.web.htmleditor.tinymce.addon.tle.service.MimeTemplateService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.ModalSession;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.result.util.CloseWindowResult;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.SelectionsMadeCallback;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractSelectionsMadeCallback implements SelectionsMadeCallback
{
	private static final long serialVersionUID = 1L;

	private static final PluginResourceHelper urlHelper = ResourcesService
		.getResourceHelper(AbstractSelectionsMadeCallback.class);
	private static final ExternallyDefinedFunction DO_SELECTION_FUNCTION = new ExternallyDefinedFunction(
		"DoSelection.selectionsMade",
		new IncludeFile(urlHelper.plugUrl("com.tle.web.htmleditor.tinymce", "scripts/tinymce/tiny_mce_popup.js")),
		new IncludeFile(urlHelper.url("scripts/doselection.js")));

	@Inject
	private SelectionService selectionService;
	@Inject
	private MimeTemplateService mimeTemplateService;

	@Override
	public boolean executeSelectionsMade(SectionInfo info, SelectionSession session)
	{
		final String sessionId = session.getAttribute(AbstractSelectionAddon.SESSION_ID);
		final String pageId = session.getAttribute(AbstractSelectionAddon.PAGE_ID);

		final FunctionCallStatement selectionsMadeCall = Js.call_s(DO_SELECTION_FUNCTION, sessionId,
			massageSelectedResources(sessionId, pageId, session, info));

		final FunctionCallStatement selectionsMadeSetupCall = Js.call_s(new ExternallyDefinedFunction("DoSelection.sm"),
			Js.function(selectionsMadeCall));

		info.getRootRenderContext().setRenderedBody(new CloseWindowResult(selectionsMadeSetupCall));
		return false;
	}

	@Override
	public void executeModalFinished(SectionInfo info, ModalSession session)
	{
		String sessionId = session.getAttribute(AbstractSelectionAddon.SESSION_ID);
		CloseWindowResult closeMe = new CloseWindowResult(new FunctionCallStatement(DO_SELECTION_FUNCTION, sessionId));
		info.getRootRenderContext().setRenderedBody(closeMe);
	}

	protected ObjectExpression[] massageSelectedResources(String sessionId, String pageId, SelectionSession session,
		SectionInfo info)
	{
		final List<ObjectExpression> attachmentUrls = new ArrayList<ObjectExpression>();

		for( SelectedResource res : session.getSelectedResources() )
		{
			final ObjectExpression jsobj = new ObjectExpression();
			final ViewableResource vres = createViewableResourceFromSelection(info, res, sessionId, pageId);
			populateSelection(info, jsobj, vres, res.getTitle());
			attachmentUrls.add(jsobj);
		}
		return attachmentUrls.toArray(new ObjectExpression[attachmentUrls.size()]);
	}

	protected ViewableResource createViewableResourceFromSelection(SectionInfo info, SelectedResource res,
		String sessionId, String pageId)
	{
		return selectionService.createViewableResource(info, res);
	}

	protected void populateSelection(SectionInfo info, ObjectExpression jsobj, ViewableResource vres, String title)
	{
		final String vhref = vres.createDefaultViewerUrl()
			.addFlag(ViewItemUrl.FLAG_FULL_URL | ViewItemUrl.FLAG_IGNORE_TRANSIENT).getHref();
		jsobj.put("href", vhref);
		jsobj.put("title", title);
		jsobj.put("template", mimeTemplateService.getPopulatedTemplate(info, vres, title));
	}
}
