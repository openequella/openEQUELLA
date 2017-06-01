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

package com.tle.web.htmleditor.tinymce.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.HtmlEditorFactoryInterface;
import com.tle.web.htmleditor.tinymce.TinyMceAddOn;
import com.tle.web.htmleditor.tinymce.TinyMceModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.AbstractRenderedComponent;

/**
 * @author aholland
 */
public interface TinyMceService extends HtmlEditorFactoryInterface
{
	/**
	 * E.g. textAreaComponent.addReadyStatements(context,
	 * tinyMceService.setupEditorStatements(textAreaComponent, model));
	 * 
	 * @param textAreaComponent
	 * @param model
	 */
	void preRender(PreRenderContext context, AbstractRenderedComponent<?> textAreaComponent, TinyMceModel model);

	/**
	 * Bind this to the JSHandler.EVENT_PRESUBMIT so that AJAX posts will read
	 * the up-to-date html E.g.
	 * textAreaComponent.setEventHandler(JSHandler.EVENT_PRESUBMIT,
	 * tinyMceService.getPreSubmitHandler(textAreaComponent));
	 * 
	 * @param textAreaComponent
	 * @return
	 */
	JSHandler getPreSubmitHandler(ElementId textAreaComponent);

	/**
	 * @param textAreaComponent
	 * @return
	 */
	JSHandler getToggleFullscreeenHandler(ElementId textAreaComponent, ElementId link);

	List<TinyMceAddOn> getAddOns();

	/**
	 * Used internally
	 * 
	 * @param info
	 * @param model
	 * @param properties
	 * @param formId Control form ID, or null
	 */
	void populateModel(SectionInfo info, TinyMceModel model, Map<String, String> properties,
		boolean restrictedCollections, boolean restrictedDynacolls, boolean restrictedSearches,
		boolean restrictedContributables, Map<Class<?>, Set<String>> searchableUuids, Set<String> contributableUuids);

	JSCallable getDisableFunction(ElementId element, ElementId fullScreenLinkElement);

	LinkedHashMap<String, HtmlEditorButtonDefinition> getButtons(SectionInfo info);

	List<List<String>> getDefaultButtonConfiguration();
}
