/*
 * Created on Apr 22, 2005
 */
package com.tle.admin.itemdefinition;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.admin.wizard.model.BasicAbstractControl;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.FixedMetadata;
import com.dytech.edge.wizard.beans.WizardPage;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.beans.entity.itemdef.Wizard;

/**
 * @author Nicholas Read
 */
public class PagedWizardModel extends BasicAbstractControl
{
	private Wizard wizard;

	/**
	 * Constructs a new PagedWizardModel.
	 */
	public PagedWizardModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		this.wizard = (Wizard) wrappedObject;
	}

	@Override
	public boolean allowsChildren()
	{
		return true;
	}

	@Override
	public List<?> getChildObjects()
	{
		List<Object> children = new ArrayList<Object>();
		List<WizardPage> pages = wizard.getPages();
		if( pages != null )
		{
			children.addAll(pages);
		}
		if( wizard.getMetadata() != null )
		{
			children.add(wizard.getMetadata());
		}
		return children;
	}

	@Override
	public String getControlClass()
	{
		return "wizard"; //$NON-NLS-1$
	}

	@Override
	public Object save()
	{
		List<WizardPage> pages = new ArrayList<WizardPage>();
		List<Control> children = getChildren();
		wizard.setMetadata(null);
		for( Control control : children )
		{
			Object childSaved = control.save();
			if( childSaved instanceof WizardPage )
			{
				pages.add((WizardPage) childSaved);
			}
			else
			{
				wizard.setMetadata((FixedMetadata) childSaved);
			}
		}
		wizard.setPages(pages);
		return wizard;
	}

	@Override
	public boolean isRemoveable()
	{
		return false;
	}

	@Override
	public boolean isScriptable()
	{
		return false;
	}

	public Wizard getWizard()
	{
		return wizard;
	}
}
