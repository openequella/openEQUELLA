package com.dytech.edge.admin.wizard;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.walkers.FindOtherPagesWithTarget;
import com.tle.admin.schema.TargetListener;
import com.tle.common.i18n.CurrentLocale;

public class CheckDuplicateTargetsHandler implements TargetListener
{
	private final Component parent;
	private final Control control;

	public CheckDuplicateTargetsHandler(Component parent, Control control)
	{
		this.parent = parent;
		this.control = control;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.schema.TargetListener#targetAdded(java.lang.String)
	 */
	@Override
	public void targetAdded(String target)
	{
		Control page = WizardHelper.getPage(control);
		if( page != null )
		{
			Control root = WizardHelper.getRoot(page);
			if( root != null )
			{
				FindOtherPagesWithTarget walker = new FindOtherPagesWithTarget(target, page);
				walker.execute(root);

				if( walker.targetFoundOnOtherPage() )
				{
					JOptionPane.showMessageDialog(parent, CurrentLocale.get("wizard.prompt.targetonmanypages"));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.schema.TargetListener#targetRemoved(java.lang.String
	 * )
	 */
	@Override
	public void targetRemoved(String target)
	{
		// We don't care about this event.
	}
}
