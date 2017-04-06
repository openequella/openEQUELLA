package com.dytech.edge.admin.script;

import java.io.Reader;

import javax.swing.JPanel;
import javax.swing.ListModel;

/**
 * A <code>ScriptModel</code> provides an abstract interface for manipulating
 * script models. A script model is a representation of a type of script. For
 * example, the <code>ifmodel</code> package provides and absract representation
 * of an "if" statement from which other script models can derive and provide
 * extended functionality.
 * 
 * @author Nicholas Read
 */
public interface ScriptModel extends ListModel
{
	/**
	 * Returns the textual representation of the script.
	 * 
	 * @return The script.
	 */
	String toScript();

	/**
	 * Determine whether or not a row is removable.
	 * 
	 * @param row The row being inquired.
	 * @return True if the row can be removed.
	 */
	boolean allowRemoval(Row row);

	/**
	 * Removes a row from the script.
	 * 
	 * @param row The row to be removed.
	 */
	void remove(Row row);

	/**
	 * Clear the script, ie, delete the whole thing.
	 */
	void clearScript();

	/**
	 * This is called whenever a row is selected in the script editor.
	 * 
	 * @param row The row that has been selected.
	 * @param index The index of the selected row.
	 */
	void rowSelected(Row row, int index);

	/**
	 * Provides a graphical editor for creating and changing rows in the script
	 * editor.
	 * 
	 * @return A <code>JPanel</code> inserting in to the script editor which
	 *         provides editing capabilities.
	 */
	JPanel getStatementEditor();

	/**
	 * Import a script in the given string.
	 * 
	 * @param s The script to be imported.
	 * @throws InvalidScriptException This is thrown if the script is invalid.
	 */
	void importScript(Reader in) throws InvalidScriptException;
}
