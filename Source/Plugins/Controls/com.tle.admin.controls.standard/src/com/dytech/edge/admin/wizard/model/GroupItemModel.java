/*
 * Created on Apr 22, 2005
 */
package com.dytech.edge.admin.wizard.model;

import java.util.Arrays;
import java.util.List;

import com.dytech.edge.admin.wizard.Contexts;
import com.dytech.edge.wizard.beans.control.GroupItem;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class GroupItemModel extends AbstractControlModel<GroupItem>
{
	private GroupItem groupItem;

	/**
	 * Constructs a new GroupItemModel.
	 */
	public GroupItemModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		groupItem = (GroupItem) wrappedObject;
	}

	@Override
	public List<?> getChildObjects()
	{
		return groupItem.getControls();
	}

	@Override
	public List<String> getContexts()
	{
		return Arrays.asList(Contexts.CONTEXT_PAGE);
	}

	@Override
	public boolean allowsChildren()
	{
		return true;
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		GroupItem gcontrol = getControl();
		if( LangUtils.isEmpty(gcontrol.getName()) || Check.isEmpty(gcontrol.getValue()) )
		{
			return CurrentLocale.get("groupitem.validation.empty");
		}
		return null;
	}
}
