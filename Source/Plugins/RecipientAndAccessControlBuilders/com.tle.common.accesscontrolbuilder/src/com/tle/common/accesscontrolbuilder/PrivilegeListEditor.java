package com.tle.common.accesscontrolbuilder;

import javax.swing.JComponent;

import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public interface PrivilegeListEditor
{
	/**
	 * Indicates whether the editor is able to handle the list of privileges.
	 */
	boolean canHandle(Node privNode, PrivilegeList list);

	/**
	 * Creates the GUI view for the editor.
	 */
	JComponent createView(Object domainObj, Node privNode, PrivilegeList list);
}
