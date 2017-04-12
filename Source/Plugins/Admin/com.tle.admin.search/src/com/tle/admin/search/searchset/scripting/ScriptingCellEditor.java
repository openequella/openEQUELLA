package com.tle.admin.search.searchset.scripting;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.dytech.edge.admin.script.ScriptEditor;
import com.dytech.edge.admin.script.ScriptModel;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;

/**
 * @author Nicholas Read
 */
public class ScriptingCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{
	private static final long serialVersionUID = 1L;
	private final Component parent;
	private final ScriptingTableModelInterface<? extends BaseEntity> model;
	private final ClientService clientService;

	private String currentScript;
	private BaseEntity currentEntity;
	private JButton button;

	public ScriptingCellEditor(Component parent, ClientService clientService,
		ScriptingTableModelInterface<? extends BaseEntity> model)
	{
		this.parent = parent;
		this.clientService = clientService;
		this.model = model;

		button = new JButton();
		button.addActionListener(this);
		button.setBorderPainted(false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == button )
		{
			ScriptModel scriptModel = null;
			if( currentEntity instanceof ItemDefinition )
			{
				ItemDefinition itemDef = (ItemDefinition) currentEntity;
				scriptModel = new WhereModel(clientService, parent, itemDef);
			}
			else if( currentEntity instanceof Schema )
			{
				Schema schema = (Schema) currentEntity;
				scriptModel = new WhereModel(clientService, parent, schema);
			}
			else
			{
				throw new IllegalStateException();
			}

			ScriptEditor editor = new ScriptEditor(scriptModel);
			if( !Check.isEmpty(currentScript) )
			{
				editor.importScript(currentScript);
			}
			editor.showEditor(parent);

			if( editor.scriptWasSaved() )
			{
				currentScript = editor.getScript();
			}

			fireEditingStopped();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue()
	{
		return currentScript;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing
	 * .JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		currentScript = (String) value;
		currentEntity = model.getEntity(row);
		return button;
	}
}