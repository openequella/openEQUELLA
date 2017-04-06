/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

public class HtmlEdit extends EditBox
{
	private static final long serialVersionUID = 1;
	public static final String CLASS = "htmledit";

	private boolean showmenu;

	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public boolean isShowMenu()
	{
		return showmenu;
	}

	public void setShowMenu(boolean showmenu)
	{
		this.showmenu = showmenu;
	}
}
