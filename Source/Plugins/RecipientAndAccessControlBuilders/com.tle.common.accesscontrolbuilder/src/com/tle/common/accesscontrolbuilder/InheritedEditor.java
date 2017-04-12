package com.tle.common.accesscontrolbuilder;

import java.util.List;

import javax.swing.JComponent;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.remoting.RemoteTLEAclManager;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class InheritedEditor implements PrivilegeListEditor
{
	private final RemoteTLEAclManager aclManager;
	private final RemoteUserService userService;

	public InheritedEditor(RemoteTLEAclManager aclManager, RemoteUserService userService)
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
		List<PrivilegeListEntry> entries = list.getEntries();
		return entries == null || entries.isEmpty();
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
		List<PrivilegeListEntry> entries = list.getEntries();
		if( entries != null )
		{
			entries.clear();
		}
		return new InheritedEditorPanel(aclManager, userService, domainObj, list.getPrivilege());
	}
}
