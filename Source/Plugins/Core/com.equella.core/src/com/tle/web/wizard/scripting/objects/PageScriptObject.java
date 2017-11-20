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

package com.tle.web.wizard.scripting.objects;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'page' variable in script
 * 
 * @author aholland
 */
public interface PageScriptObject extends ScriptObject
{
	/**
	 * @return Controls on the page are editable
	 */
	boolean isEnabled();

	/**
	 * @param enabled Controls on the page are editable
	 */
	void setEnabled(boolean enabled);

	/**
	 * @return The title of the page
	 */
	String getPageTitle();

	/**
	 * @param title The new title of the page
	 */
	void setPageTitle(String title);

	/**
	 * @return All mandatory controls are filled in
	 */
	boolean isValid();

	/**
	 * @param valid You can cause the page to become invalid. This will prevent
	 *            the item from being submitted.
	 */
	void setValid(boolean valid);

	/**
	 * @return The number of this page, starting at zero
	 */
	int getPageNumber();

	/**
	 * @return The number of controls on this page.
	 */
	int getControlCount();

	/**
	 * @param index A zero-based control index
	 * @return Will return null if the index is out of range, otherwise a
	 *         ControlScriptObject instance will be returned.
	 */
	ControlScriptObject getControlByIndex(int index);
}
