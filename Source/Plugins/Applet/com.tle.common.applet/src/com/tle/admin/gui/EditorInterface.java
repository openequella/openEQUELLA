package com.tle.admin.gui;

import java.awt.Component;

import com.dytech.gui.Changeable;
import com.dytech.gui.JStatusBar;

/**
 * @author Nicholas Read
 */
public interface EditorInterface extends Changeable
{
	/**
	 * Save the document.
	 */
	void save() throws EditorException;

	/**
	 * Validate the document.
	 */
	void validation() throws EditorException;

	/**
	 * Close the editor.
	 */
	void close(Integer confirmOption);

	/**
	 * Unlock any resources
	 */
	void unlock();

	/**
	 * Allows for things to be done just before saving.
	 */
	boolean beforeSaving();

	/**
	 * Gets the name of the document type being edited.
	 * 
	 * @return the document name.
	 */
	String getDocumentName();

	/**
	 * @return true if the document is read-only.
	 */
	boolean isReadOnly();

	/**
	 * The parent component
	 */
	Component getParentWindow();

	/**
	 * Gets the status bar of the editor.
	 */
	JStatusBar getStatusBar();
}
