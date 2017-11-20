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

package com.dytech.edge.admin.wizard.editor.drm;

import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.gui.TableLayout;
import com.tle.admin.Driver;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.ExpressionBuilderFinder;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class DRMRequireAcceptanceFromTab extends JPanel
{
	private static final long serialVersionUID = 1L;

	private ExpressionBuilderFinder finder;

	public DRMRequireAcceptanceFromTab()
	{
		createGUI();
	}

	private void createGUI()
	{
		JLabel title = new JLabel(
			CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrequireacceptancefrom.title")); //$NON-NLS-1$

		finder = new ExpressionBuilderFinder(Driver.instance().getClientService().getService(RemoteUserService.class));

		final int[] rows = {title.getPreferredSize().height, TableLayout.DOUBLE_FILL, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL,};
		setLayout(new TableLayout(rows, cols));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(title, new Rectangle(0, 0, 1, 1));
		add(finder, new Rectangle(0, 1, 1, 1));
	}

	public void load(DRMPage page)
	{
		if( Check.isEmpty(page.getRequireAcceptanceFrom()) )
		{
			setDefaultFinderExpression();
		}
		else
		{
			finder.setExpression(page.getRequireAcceptanceFrom());
		}
	}

	public void save(DRMPage page)
	{
		if( isFinderExpressionInvalid() )
		{
			setDefaultFinderExpression();
		}
		page.setRequireAcceptanceFrom((String) finder.getSelectedResults().get(0));
	}

	private void setDefaultFinderExpression()
	{
		finder.setExpression(SecurityConstants.getRecipient(Recipient.EVERYONE));
	}

	private boolean isFinderExpressionInvalid()
	{
		try
		{
			finder.getSelectedResults();
		}
		catch( RuntimeApplicationException ex )
		{
			return true;
		}
		return false;
	}
}
