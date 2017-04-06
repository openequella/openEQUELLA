package com.tle.admin.schema;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JTextField;

import com.dytech.gui.Changeable;
import com.tle.admin.helper.GeneralDialog;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class SingleTargetChooser extends TargetChooser implements ActionListener, Changeable
{
	private static final long serialVersionUID = 1L;

	private SchemaNode node;
	private SchemaNode original;
	private JTextField target;
	private JButton search;

	public SingleTargetChooser(SchemaModel model, String targetBase)
	{
		super(model, targetBase);
		createGUI();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		search.setEnabled(enabled);
		target.setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#clearChanges()
	 */
	@Override
	public void clearChanges()
	{
		original = node;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#hasDetectedChanges()
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		return !Objects.equals(original, node);
	}

	public boolean hasTarget()
	{
		return node != null;
	}

	public String getTarget()
	{
		return target.getText();
	}

	public SchemaNode getSchemaNode()
	{
		return node;
	}

	public void setTarget(String t)
	{
		SchemaNode n = null;
		if( t != null && t.length() > 0 )
		{
			n = getSchemaModel().getNode(t);
		}
		setTarget(n);
	}

	public void setTarget(SchemaNode n)
	{
		setTargetImpl(n);
		clearChanges();
	}

	private void setTargetImpl(SchemaNode n)
	{
		node = n;
		if( n == null )
		{
			target.setText(""); //$NON-NLS-1$
		}
		else
		{
			String xpath = node.getXmlPath();
			if( getTargetBase() != null )
			{
				xpath = xpath.substring(getTargetBase().length());
			}

			if( xpath.length() == 0 )
			{
				xpath = "/"; //$NON-NLS-1$
			}

			target.setText(xpath);
		}
	}

	protected void createGUI()
	{
		search = new JButton(CurrentLocale.get("com.tle.admin.schema.singletargetchooser.browse"));
		search.addActionListener(this);

		target = new JTextField();
		target.setEditable(false);

		setLayout(new BorderLayout(5, 5));

		add(target, BorderLayout.CENTER);
		add(search, BorderLayout.EAST);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		WhereTargetDialog dialog = new WhereTargetDialog(this, getTree(), warnAboutNonFields);
		dialog.setNonLeafSelection(enableNonLeafSelection);
		dialog.setAttributesAllowed(attributesAllowed);
		dialog.showDialog();

		if( dialog.getResponse() == GeneralDialog.OK_RESPONSE )
		{
			SchemaNode n = (SchemaNode) dialog.getValue();
			if( n != null )
			{
				if( node != null )
				{
					fireTargedRemoved(target.getText());
				}

				setTargetImpl(n);
				fireTargedAdded(target.getText());
			}
		}
	}
}
