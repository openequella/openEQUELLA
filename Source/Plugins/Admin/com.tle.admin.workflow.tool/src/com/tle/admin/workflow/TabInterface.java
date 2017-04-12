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
