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

package com.tle.web.htmleditor.tinymce;

import java.util.List;
import java.util.Set;

import com.dytech.edge.common.ScriptContext;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.render.PreRenderable;

/**
 * @author aholland
 */
@NonNullByDefault
public interface TinyMceAddOn extends PreRenderable
{
	/**
	 * Return true if your addon can handle the supplied action name. This will
	 * exclude any other addon from handling that action.
	 * 
	 * @param action The name of the action associated with the addon. This will
	 *            come from your editor_plugin_src.js file. Eg: init :
	 *            function(ed, url) { ed.addCommand('mceMyAddon', function() {
	 *            ed.windowManager.open({ file : baseActionUrl +
	 *            'myAddonActionName', ...
	 * @return true if you will handle the action
	 */
	boolean applies(String action);

	/**
	 * @return true if the Add-on is enabled.
	 */
	boolean isEnabled();

	/**
	 * TinyMceActionSection will call this on your addon if you returned true in
	 * your applies method.
	 * 
	 * @param info
	 * @param action
	 * @param sessionId
	 * @param pageId
	 * @param tinyMceBaseUrl
	 * @return A renderable result, or null
	 */
	SectionResult execute(SectionInfo info, String action, String sessionId, String pageId, String tinyMceBaseUrl,
		boolean restrictedCollections, Set<String> collectionUuids, boolean restrictedDynacolls,
		Set<String> dynaCollUuids, boolean restrictedSearches, Set<String> searchUuids,
		boolean restrictedContributables, Set<String> contributableUuids);

	/**
	 * @return A unique identifier for your addon. E.g. example_plugin
	 */
	String getId();

	/**
	 * @return The full url to your editor_plugin_src.js file. Used to include
	 *         in the HTML editor.
	 */
	String getJsUrl();

	/**
	 * @return The URL to the public resources of this plugin. Return null if
	 *         you don't have any.
	 */
	@Nullable
	String getResourcesUrl();

	/**
	 * @param info
	 * @param scriptContext The script context you may wish to use during
	 *            preRender and getInitialisation. You can reliably store it in
	 *            the info if your addOn is a singleton.
	 */
	void setScriptContext(SectionInfo info, ScriptContext scriptContext);

	/**
	 * Usually you would add onload statements here
	 * 
	 * @param context
	 * @return
	 */
	@Override
	void preRender(PreRenderContext context);

	/**
	 * @return The base URL where the editor_plugin_src.js file resides
	 */
	String getBaseUrl();

	/**
	 * @param info
	 * @return A list of button ids.
	 */
	List<HtmlEditorButtonDefinition> getButtons(SectionInfo info);

	/**
	 * Registers your addon into TinyMceActionSection's tree. You do not
	 * necessarily need to do anything in this method.
	 * 
	 * @param tree
	 * @param parentId
	 */
	void register(SectionTree tree, String parentId);

	/**
	 * Properties passed into the tiny mce editor initialisation
	 * 
	 * @param context
	 * @return An object containing the contents of the config.json file. Return
	 *         null if no config needed
	 */
	@Nullable
	ObjectExpression getInitialisation(RenderContext context);
}
