package com.tle.admin.schema;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.tle.admin.helper.GeneralDialog;

public class SchemaDialog extends GeneralDialog implements TreeSelectionListener
{
	private static final int WINDOW_WIDTH = 300;
	private static final int WINDOW_HEIGHT = 450;

	protected SchemaTree tree;
	private boolean nonLeafSelection = false;
	private boolean attributesAllowed = true;

	public SchemaDialog(Component parent, String title, SchemaTree tree)
	{
		super(parent, title);
		setup(tree);
	}

	public void setNonLeafSelection(boolean b)
	{
		nonLeafSelection = b;
	}

	public void setAttributesAllowed(boolean attributesAllowed)
	{
		this.attributesAllowed = attributesAllowed;
	}

	protected void setup(SchemaTree t)
	{
		tree = t;
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if( e.getClickCount() == 2 && !tree.isSelectionEmpty() && okButton.isEnabled() )
				{
					onOk();
				}
			}
		});

		JScrollPane scroller = new JScrollPane(tree);
		setInner(scroller);

		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	@Override
	public void showDialog()
	{
		okButton.setEnabled(false);
		tree.clearSelection();
		tree.addTreeSelectionListener(this);

		super.showDialog();
	}

	@Override
	public void ok()
	{
		tree.removeTreeSelectionListener(this);
		if( tree.isSelectionEmpty() )
		{
			setValue(null);
		}
		else
		{
			setValue(tree.getLastSelectedPathComponent());
		}
	}

	@Override
	public void cancelled()
	{
		tree.removeTreeSelectionListener(this);
		setValue(null);
	}

	public void addItem(Object item)
	{
		// We don't want people to be able to add items
	}

	public void removeItem(Object item)
	{
		// We don't want people to be able to remove items
	}

	public void clearItems()
	{
		// We don't want people to be able to clear items
	}

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		okButton.setEnabled(false);

		if( !tree.isSelectionEmpty() )
		{
			SchemaNode node = (SchemaNode) tree.getLastSelectedPathComponent();

			if( (attributesAllowed || !node.isAttribute()) && (nonLeafSelection || !node.hasNonAttributeChildren()) )
			{
				okButton.setEnabled(true);
			}
		}
	}
}
