/*
 * Created on May 10, 2005
 */
package com.tle.admin.schema.manager;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JLabel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.beans.entity.Schema;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class TransformationsTab extends BaseEntityTab<Schema>
{
	private TransformationsPanel ip;
	private TransformationsPanel ep;

	@Override
	public void init(Component parent)
	{
		setupGUI();
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.schema.manager.transformationstab.title"); //$NON-NLS-1$
	}

	private void setupGUI()
	{
		JLabel il = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.transformationstab.importTitle")); //$NON-NLS-1$
		JLabel el = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.transformationstab.exportTitle")); //$NON-NLS-1$

		ip = new TransformationsPanel(adminService, state, true);
		ep = new TransformationsPanel(adminService, state, false);

		final int height1 = il.getPreferredSize().height;

		final int[] rows = {height1, TableLayout.FILL, 10, height1, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));

		add(il, new Rectangle(0, 0, 1, 1));
		add(ip, new Rectangle(0, 1, 1, 1));
		add(el, new Rectangle(0, 3, 1, 1));
		add(ep, new Rectangle(0, 4, 1, 1));
	}

	public void addToChangeListener(ChangeDetector changeDetector)
	{
		ip.addToChangeListener(changeDetector);
		ep.addToChangeListener(changeDetector);
	}

	@Override
	public void load()
	{
		ip.load();
		ep.load();
	}

	@Override
	public void save()
	{
		ip.save();
		ep.save();
	}

	@Override
	public void validation() throws EditorException
	{
		// DO NOTHING
	}
}
