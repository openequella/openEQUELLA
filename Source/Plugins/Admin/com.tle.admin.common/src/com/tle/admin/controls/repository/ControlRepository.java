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