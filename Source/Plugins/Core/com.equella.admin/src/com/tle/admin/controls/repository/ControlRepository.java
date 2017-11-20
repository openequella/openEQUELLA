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

package com.tle.admin.controls.repository;

import java.util.List;

import javax.swing.Icon;

public interface ControlRepository
{
	String getIdForWizardObject(Object object);

	Object getNewWrappedObject(String id);

	Object getModelForControl(String id);

	/**
	 * Returns the control definition for the given ID.
	 */
	ControlDefinition getDefinition(String id);

	/**
	 * Returns the icon for the given ID and whether it watermarked as having
	 * script.
	 */
	Icon getIcon(String id, boolean scripted);

	List<ControlDefinition> getDefinitionsForContext(String category);

	List<ClassLoader> getClassLoaders();
}