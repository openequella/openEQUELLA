package com.tle.reporting.oda.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("nls")
public class UserManagementEditorPage extends SimpleEditorPage
{
	private static final String USERS_IN_GROUP_QUERY = "ug:";
	private static final String USER_QUERY = "u:";
	private static final String USER_SEARCH_QUERY = "su:";
	private static final String GROUP_QUERY = "g:";
	private static final String GROUP_SEARCH_QUERY = "sg:";
	private static final String GROUPS_FOR_USER_QUERY = "gfu:";
	private static final String ROLE_QUERY = "r:";

	public UserManagementEditorPage(String arg0)
	{
		super(arg0);
	}

	public UserManagementEditorPage(String arg0, String arg1, ImageDescriptor arg2)
	{
		super(arg0, arg1, arg2);
	}

	@Override
	public void createPageCustomControl(Composite parent)
	{
		setControl(createPageControl(parent, "freetext"));
	}

	@Override
	protected void setTypeCombo(ComboViewer typeCombo)
	{
		typeCombo.add(new Object[] { new PrefixType(USER_QUERY, "User information"),
				new PrefixType(GROUP_QUERY, "Group Information"), new PrefixType(ROLE_QUERY, "Role Information"),
				new PrefixType(USER_SEARCH_QUERY, "Search users"), new PrefixType(GROUP_SEARCH_QUERY, "Search groups"),
				new PrefixType(USERS_IN_GROUP_QUERY, "Get users in group"),
				new PrefixType(GROUPS_FOR_USER_QUERY, "Get groups for user"), });

		typeCombo.setSelection(new StructuredSelection(new PrefixType(getPrefixText(), "")));
	}

	@Override
	protected String getDefaultPrefix()
	{
		return USER_SEARCH_QUERY;
	}
}
