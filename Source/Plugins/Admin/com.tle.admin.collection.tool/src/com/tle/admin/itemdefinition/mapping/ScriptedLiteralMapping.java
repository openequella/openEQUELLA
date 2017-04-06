/*
 * Created on Oct 1, 2004
 */
package com.tle.admin.itemdefinition.mapping;

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.MetadataMapping;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ScriptedLiteralMapping extends JPanel implements Mapping, ListSelectionListener
{
	private static final long serialVersionUID = 1L;
	private SchemaList targets;
	private ScriptedLiterals literals;

	public ScriptedLiteralMapping(SchemaModel schema)
	{
		createGUI(schema);
	}

	private void createGUI(SchemaModel schema)
	{
		targets = new SchemaList(schema);
		literals = new ScriptedLiterals(schema);

		targets.addListSelectionListener(this);

		final int width1 = targets.getPreferredSize().width;
		final int[] rows = {TableLayout.FILL,};
		final int[] cols = {width1, 10, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		add(targets, new Rectangle(0, 0, 1, 1));
		add(literals, new Rectangle(2, 0, 1, 1));
	}

	public void setChangeDetector(ChangeDetector detector)
	{
		targets.setChangeDetector(detector);
	}

	@Override
	public JComponent getComponent()
	{
		return this;
	}

	@Override
	public void save(MetadataMapping mapping)
	{
		targets.save(mapping);
	}

	@Override
	public void loadItem(MetadataMapping mapping)
	{
		targets.load(mapping);
	}

	@Override
	public String toString()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.mapping.scriptedliteralmapping.title");
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		literals.loadTarget(targets.getSelection());
	}
}
