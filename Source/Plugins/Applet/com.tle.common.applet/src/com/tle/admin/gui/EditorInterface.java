/*
 * Copyright 2019 Apereo
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
