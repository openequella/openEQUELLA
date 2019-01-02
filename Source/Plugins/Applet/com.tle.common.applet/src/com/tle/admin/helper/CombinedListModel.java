/*
 * Copyright 2019 Apereo
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

package com.tle.admin.helper;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * ListModel used to display the contents of multiple attachment lists as one.
 * 
 * @author cofarrell
 */
public class CombinedListModel extends AbstractListModel implements ListDataListener
{
	private static final long serialVersionUID = 1L;
	private List<ListModel> models = new ArrayList<ListModel>();

	public CombinedListModel()
	{
		super();
	}

	/**
	 * Adds a <code>ListModel</code> to this multiple model.
	 */
	public void addListModel(ListModel model)
	{
		models.add(model);
		model.addListDataListener(this);
	}

	/**
	 * Removes a <code>ListModel</code> from this multiple model.
	 */
	public void removeListModel(ListModel model)
	{
		models.remove(model);
		model.removeListDataListener(this);
	}

	/**
	 * Converts a full index of a list item to the relevant model number. This
	 * is required for <code>AttachmentListCellRenderer</code> to display the
	 * correct icon.
	 * 
	 * @see AttachmentListCellRenderer
	 * @param index of list item
	 * @return index of model used for index parameter
	 */
	public int getModelIndex(int index)
	{
		for( int i = 0; i < models.size(); i++ )
		{
			ListModel model = models.get(i);

			if( model.getSize() <= index )
			{
				index -= model.getSize();
			}
			else
			{
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index)
	{
		for( int i = 0; i < models.size(); i++ )
		{
			ListModel model = models.get(i);
			if( model.getSize() <= index )
			{
				index -= model.getSize();
			}
			else
			{
				return model.getElementAt(index);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize()
	{
		int size = 0;
		for( int i = 0; i < models.size(); i++ )
		{
			ListModel model = models.get(i);
			size += model.getSize();
		}
		return size;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.
	 * ListDataEvent)
	 */
	@Override
	public void contentsChanged(ListDataEvent e)
	{
		this.fireContentsChanged(e.getSource(), e.getIndex0(), e.getIndex1());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.
	 * ListDataEvent)
	 */
	@Override
	public void intervalAdded(ListDataEvent e)
	{
		this.fireIntervalAdded(e.getSource(), e.getIndex0(), e.getIndex1());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.
	 * ListDataEvent)
	 */
	@Override
	public void intervalRemoved(ListDataEvent e)
	{
		this.fireIntervalRemoved(e.getSource(), e.getIndex0(), e.getIndex1());
	}
}
