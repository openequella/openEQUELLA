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

import java.awt.Component;

import javax.swing.JOptionPane;

import com.tle.common.i18n.CurrentLocale;

public class WhereTargetDialog extends SchemaDialog
{
	private final boolean warnAboutNonFields;

	public WhereTargetDialog(Component parent, SchemaTree tree, boolean warnAboutNonFields)
	{
		super(parent, getTitle(), tree);
		this.warnAboutNonFields = warnAboutNonFields;
	}

	@Override
	protected void onOk()
	{
		if( tree.isSelectionEmpty() )
		{
			setValue(null);
		}
		else
		{
			SchemaNode node = (SchemaNode) tree.getLastSelectedPathComponent();
			if( warnAboutNonFields && !node.isField() )
			{
				int i = JOptionPane.showConfirmDialog(dialog,
					CurrentLocale.get("wizard.prompt.targetnotfield"), "Warning", JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				if( i == JOptionPane.NO_OPTION )
				{
					return;
				}
			}
			setValue(node);
		}
		super.onOk();
	}

	private static String getTitle()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.wheremodel.wheretargetdialog.title"); //$NON-NLS-1$
	}
}
