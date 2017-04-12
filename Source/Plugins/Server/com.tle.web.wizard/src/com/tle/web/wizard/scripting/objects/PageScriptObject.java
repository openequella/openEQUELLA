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
