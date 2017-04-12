/*
 * Created on Oct 1, 2004
 */
package com.tle.admin.itemdefinition.mapping;

import javax.swing.JComponent;

import com.tle.beans.entity.itemdef.MetadataMapping;

/**
 * @author Charles O'Farrell
 */
public interface Mapping
{
	/**
	 * Gets the viewable component for this mapping.
	 */
	JComponent getComponent();

	/**
	 * Retrieve the XML for the current mapping.
	 */
	void save(MetadataMapping mapping);

	/**
	 * Load the mapping editor from the given XML.
	 */
	void loadItem(MetadataMapping mapping);
}
