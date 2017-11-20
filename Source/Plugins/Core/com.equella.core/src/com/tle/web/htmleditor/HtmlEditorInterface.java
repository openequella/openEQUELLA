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

package com.tle.web.htmleditor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tle.common.scripting.ScriptContextFactory;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.render.HtmlRenderer;

// FFS. Comment formating butchered the Javadoc
/**
 * @author aholland
 *         <p>
 *         Usage:
 * 
 *         <pre>
 * {@code 
 * @@TreeLookup 
 * private HtmlEditorInterface htmlEditor; 
 * 
 * public void registered(String id, SectionTree tree) 
 * { 
 * 		super.registered(id, tree); 
 * 		... 
 * 		tree.registerSections(htmlEditorService.getEditor(),
 *             id); 
 * } 
 * 
 * public SectionResult renderHtml() 
 * { 
 * 		...
 * 		htmlEditor.setData(info, properties);
 * 		model.setResults(renderChildren(context, event, new ResultListCollector()).getResultList()); 
 * 		... 
 * }
 * }
 * </pre>
 */
@TreeIndexed
@SuppressWarnings("nls")
public interface HtmlEditorInterface extends Section, HtmlRenderer
{
	String HTML = "html";
	String WIDTH = "width";
	String HEIGHT = "height";
	String EXCLUDED_ADDONS = "EXCLUDED_ADDONS";

	/**
	 * The property name to populate the html is "html". (Use the constant Luke)
	 * <p>
	 * The width and height of the editor are css style values and their
	 * property names are "width" and "height" respectively. These two
	 * properties are mandatory, you can specify a value of "auto" if you are
	 * not fussed about the size of your editor.
	 * <p>
	 * Additional properties are dependent on the HTML editor implementation.
	 * Check implementers of this interface for additional properties.
	 * 
	 * @param info
	 * @param properties
	 * @throws Exception
	 */
	void setData(SectionInfo info, Map<String, String> properties, ScriptContextFactory scriptContextFactory)
		throws Exception;

	String getHtml(SectionInfo info);

	/**
	 * List all buttons whether currently on the toolbar or not
	 * 
	 * @param info
	 * @return
	 */
	LinkedHashMap<String, HtmlEditorButtonDefinition> getAllButtons(SectionInfo info);

	/**
	 * @return Three lists of button IDs
	 */
	List<List<String>> getDefaultButtonConfiguration();
}
