package com.tle.common.accesscontrolbuilder;

import javax.swing.JComponent;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class AdvancedEditor implements PrivilegeListEditor
{
	private final RemoteTLEAclManager aclManager;
	private final RemoteUserService userService;

	public AdvancedEditor(RemoteTLEAclManager aclManager, RemoteUserService userService)
	{
		this.aclManager = aclManager;
		this.userService = userService;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.editors.PrivilegeListEditor#canHandle(com.tle.
	 * common.security.PrivilegeTree.Node,
	 * com.tle.admin.security.editors.PrivilegeList)
	 */
	@Override
	public boolean canHandle(Node privNode, PrivilegeList list)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.admin.security.editors.PrivilegeListEditor#createView(java.lang
	 * .Object, com.tle.common.security.PrivilegeTree.Node,
	 * com.tle.admin.security.editors.PrivilegeList)
	 */
	@Override
	public JComponent createView(Object domainObj, Node privNode, PrivilegeList list)
	{
		return new AdvancedEditorPanel(aclManager, userService, privNode, list, domainObj);
	}
}
