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

package com.tle.admin.workflow;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.Changeable;
import com.tle.admin.gui.EditorException;

/**
 * @author Nicholas Read
 */
public interface TabInterface extends Changeable
{
	/**
	 * @return the title that should appear on the tab.
	 */
	String getTabTitle();

	/**
	 * The tab must validate it's data.
	 * 
	 * @throws EditorException if something is invalid.
	 */
	void validation() throws EditorException;

	/**
	 * Loads the workflow from the given XML document.
	 */
	void loadWorkflow(PropBagEx xml);

	/**
	 * Save the workflow to the given XML document.
	 */
	void saveWorkflow(PropBagEx xml);
}
