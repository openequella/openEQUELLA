/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
