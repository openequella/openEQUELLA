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

package com.tle.admin.tools.common;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;

import com.tle.admin.common.gui.actions.ExportAction;
import com.tle.admin.common.gui.actions.ImportAction;
import com.tle.admin.gui.common.actions.ArchiveAction;
import com.tle.admin.gui.common.actions.CloneAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UnarchiveAction;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class AdminToolListClone extends AdminToolList
{
	protected boolean enableClone = true;

	public AdminToolListClone()
	{
		super();
	}

	@Override
	protected void getButtonActions(List<TLEAction> actions)
	{
		super.getButtonActions(actions);

		actions.add(cloneAction);
		actions.add(importAction);
		actions.add(exportAction);
	}

	/**
	 * Called when the clone button is pressed.
	 * 
	 * @param selected The selected Object in the list. If the returned object
	 *            is null then no Object is added.
	 */
	protected abstract void onClone();

	/**
	 * Called when the import button is pressed. If the returned object is null
	 * then no Object is added.
	 */
	protected abstract void onImport();

	/**
	 * Called when the export button is pressed.
	 * 
	 * @param selected The selected Object in the list.
	 */
	protected abstract void onExport();

	protected abstract void onArchive();

	protected abstract void onUnarchive();

	protected TLEAction cloneAction = new CloneAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			List<NameValue> selections = getSelectedObjects();
			final int confirm = JOptionPane.showConfirmDialog(parentFrame,
				CurrentLocale.get("com.dytech.edge.admin.guiadmintoollistclone.confirm", selections.get(0) //$NON-NLS-1$
					.toString()), CurrentLocale.get("com.dytech.edge.admin.guiadmintoollistclone.clone"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if( confirm == JOptionPane.YES_OPTION )
			{
				onClone();
			}
		}

		@Override
		public void update()
		{
			setEnabled(enableClone && getSelectedObjects().size() == 1);
		}
	};

	protected TLEAction importAction = new ImportAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			onImport();
		}
	};

	protected TLEAction exportAction = new ExportAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			onExport();
		}

		@Override
		public void update()
		{
			setEnabled(getSelectedObjects().size() == 1);
		}
	};

	protected TLEAction archiveAction = new ArchiveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			final int result = JOptionPane.showConfirmDialog(parentFrame,
				CurrentLocale.get("com.tle.admin.gui.common.actions.archiveaction.confirmarchive"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.admin.gui.common.actions.archiveaction.name"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if( result == JOptionPane.YES_OPTION )
			{
				onArchive();
			}
		}

		@Override
		public void update()
		{
			setEnabled(getSelectedObjects().size() > 0);
		}
	};

	protected TLEAction unarchiveAction = new UnarchiveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			final int result = JOptionPane.showConfirmDialog(parentFrame,
				CurrentLocale.get("com.tle.admin.gui.common.actions.unarchiveaction.confirmunarchive"), //$NON-NLS-1$
				CurrentLocale.get("com.tle.admin.gui.common.actions.unarchiveaction.name"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if( result == JOptionPane.YES_OPTION )
			{
				onUnarchive();
			}
		}

		@Override
		public void update()
		{
			setEnabled(getSelectedObjects().size() > 0);
		}
	};
}