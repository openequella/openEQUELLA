/**
 * 
 */
package com.dytech.installer.controls;

import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;
import com.dytech.installer.Wizard;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class OracleIdSelector extends GRadioButtonGroup
{
	Wizard grandParent;
	PropBagEx defaults;

	public OracleIdSelector(PropBagEx controlBag, Wizard grandParent) throws InstallerException
	{
		super(controlBag);
		this.grandParent = grandParent;
		this.defaults = grandParent != null ? grandParent.getDefaults() : null;
		if( items.size() != 2 )
		{
			throw new InstallerException("Expected 2 item definitions for Oracle id selector radio buttons");
		}

		update();
	}

	@Override
	public JComponent generateControl()
	{
		JComponent generated = super.generateControl();
		update();
		return generated;
	}

	private void update()
	{
		PropBagEx stateOfThings = grandParent.getOutputNow();
		String dbtype = stateOfThings.getNode("datasource/dbtype");
		boolean relevance = ("oracle".equals(dbtype));
		if( relevance )
		{
			loadControl(defaults);
		}
		else
		{
			// If not relevant, hide the description strings, invisiblize the
			// buttons
			this.title = "";
			this.description = "";
			for( Iterator<?> iter = this.items.iterator(); iter.hasNext(); )
			{
				Item item = (Item) iter.next();
				if( item != null && item.getButton() != null )
				{
					item.getButton().setVisible(false);
				}
			}
		}
	}

	@Override
	public AbstractButton generateButton(String name, ButtonGroup group)
	{
		return super.generateButton(name, group);
	}

}
