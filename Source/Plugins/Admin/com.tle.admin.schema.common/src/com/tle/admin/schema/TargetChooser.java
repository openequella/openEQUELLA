package com.tle.admin.schema;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

/**
 * @author Nicholas Read
 */
public abstract class TargetChooser extends JComponent
{
	private SchemaTree tree;
	private SchemaModel model;
	private String targetBase;
	private EventListenerList listeners;

	protected boolean warnAboutNonFields;
	protected boolean enableNonLeafSelection = false;
	protected boolean attributesAllowed = true;

	public TargetChooser(SchemaModel m, String tb)
	{
		this.model = m;
		this.targetBase = tb;

		if( targetBase == null || targetBase.length() == 0 || targetBase.equals("/") ) //$NON-NLS-1$
		{
			targetBase = null;
		}

		if( targetBase != null )
		{
			SchemaNode newRoot = model.getNode(targetBase);
			model = new SchemaModel(newRoot);
		}

		listeners = new EventListenerList();
	}

	public void setWarnAboutNonFields(boolean warnAboutNonFields)
	{
		this.warnAboutNonFields = warnAboutNonFields;
	}

	public void setNonLeafSelection(boolean b)
	{
		enableNonLeafSelection = b;
	}

	public void setAttributesAllowed(boolean attributesAllowed)
	{
		this.attributesAllowed = attributesAllowed;
	}

	protected synchronized SchemaTree getTree()
	{
		if( tree == null )
		{
			tree = new SchemaTree(model, warnAboutNonFields);
			tree.setEditable(false);
		}
		return tree;
	}

	/**
	 * @return Returns the targetBase.
	 */
	protected String getTargetBase()
	{
		return targetBase;
	}

	/**
	 * @return Returns the model.
	 */
	protected SchemaModel getSchemaModel()
	{
		return model;
	}

	public void addTargetListener(TargetListener l)
	{
		listeners.add(TargetListener.class, l);
	}

	public void removeTargetListener(TargetListener l)
	{
		listeners.remove(TargetListener.class, l);
	}

	protected void fireTargedAdded(String target)
	{
		for( TargetListener listener : listeners.getListeners(TargetListener.class) )
		{
			listener.targetAdded(target);
		}
	}

	protected void fireTargedRemoved(String target)
	{
		for( TargetListener listener : listeners.getListeners(TargetListener.class) )
		{
			listener.targetRemoved(target);
		}
	}
}
