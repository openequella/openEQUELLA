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

package com.tle.admin.baseentity;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import com.tle.core.plugins.AbstractPluginService;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.dytech.gui.Changeable;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JStatusBar;
import com.tle.admin.Driver;
import com.tle.admin.common.gui.EditorHelper;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.EditorInterface;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.gui.common.actions.CloseAction;
import com.tle.admin.gui.common.actions.SaveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.client.gui.StatusBarContainer;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.NameValue;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class BaseEntityEditor<T extends BaseEntity>
	implements
		EditorInterface,
		DynamicTabService,
		StatusBarContainer
{
	protected static final Log LOGGER = LogFactory.getLog(BaseEntityEditor.class);

	protected final EditorState<T> state;

	protected JDialog dialog;
	private JPanel content;
	protected Changeable changeDetector;

	protected List<? extends BaseEntityTab<T>> btabs;
	private List<? extends BaseEntityTab<T>> tabLoadSaveOrder;
	protected JTabbedPane tabs;

	private final BaseEntityTool<T> tool;
	protected final ClientService clientService;

	protected JStatusBar statusbar;
	@Deprecated
	protected Driver driver;
	protected AbstractDetailsTab<T> detailsTab;
	private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

	protected String getString(String key)
	{
		return CurrentLocale.get(KEY_PFX+key);
	}

	public BaseEntityEditor(BaseEntityTool<T> tool, boolean readonly)
	{
		this.tool = tool;
		this.driver = tool.getDriver();
		this.clientService = tool.getClientService();

		state = new EditorState<T>();
		state.setReadonly(readonly);
	}

	private void setupGUI()
	{
		tabs = new JTabbedPane();
		tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

		statusbar = new JStatusBar(EditorHelper.getStatusBarSpinner());

		JButton saveButton = new JButton(saveAction);
		JButton closeButton = new JButton(closeAction);

		saveAction.setEnabled(!state.isReadonly());

		setup();

		detailsTab = constructDetailsTab();

		addTabs();

		detailsTab.addNameListener(new NameChangeListener());

		JChangeDetectorPanel content1 = new JChangeDetectorPanel();
		changeDetector = content1;
		content1.setLayout(new MigLayout("wrap", "[][]"));
		content1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JComponent main;
		if( btabs.size() == 1 )
		{
			main = btabs.get(0).getComponent();
		}
		else
		{
			main = tabs;
		}
		content1.add(main, "grow, pushy, span 2");
		content1.add(saveButton, "pushx, align right");
		content1.add(closeButton);

		this.content = statusbar.attachToPanel(content1);
	}

	protected void addTabs()
	{
		btabs = getTabs();
		tabLoadSaveOrder = getTabLoadOrder(btabs);

		for( BaseEntityTab<T> tab : btabs )
		{
			addTab(tab);
		}
	}

	protected List<? extends BaseEntityTab<T>> getTabLoadOrder(List<? extends BaseEntityTab<T>> btabs2)
	{
		return btabs;
	}

	public void addTab(BaseEntityTab<T> tab)
	{
		tab.setState(state);
		tab.setDriver(driver);
		tab.setStatusBar(statusbar);
		tab.setDynamicTabService(this);
		tab.init(dialog);

		addTab(tab.getComponent(), tab.getTitle(), -1);
	}

	@Override
	public void addTab(JComponent comp, String tabTitle, int index)
	{
		comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		if( index >= 0 )
		{
			tabs.insertTab(tabTitle, null, comp, null, index);
		}
		else
		{
			tabs.addTab(tabTitle, comp);
		}
	}

	@Override
	public void removeTab(JComponent comp)
	{
		tabs.remove(comp);
	}

	private String getCurrentName()
	{
		T ent = getEntity();
		return CurrentLocale.get(ent.getName(), null);
	}

	protected void setup()
	{
		// TO BE OVERRIDEN
	}

	protected abstract AbstractDetailsTab<T> constructDetailsTab();

	protected abstract String getEntityName();

	protected abstract String getWindowTitle();

	protected abstract List<? extends BaseEntityTab<T>> getTabs();

	/**
	 * Shows the schema manager window.
	 * 
	 * @return true if the schema was saved/added.
	 */
	public boolean showEditor(Component parent)
	{
		dialog = ComponentHelper.createJDialog(parent);

		for( BaseEntityTab<T> tab : btabs )
		{
			tab.setParent(dialog);
		}

		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setContentPane(content);
		dialog.setModal(true);

		refreshWindowTitle(null);
		ComponentHelper.percentageOfScreen(dialog, 0.9f, 0.9f);
		ComponentHelper.centreOnScreen(dialog);
		EditorHelper.listenForClosing(dialog, this);

		dialog.setVisible(true);
		dialog = null;

		return state.isSaved();
	}

	/**
	 * Loads the given entity in the editor.
	 */
	public void load(EntityPack<T> entityPack, boolean isLoaded)
	{
		state.setEntityPack(entityPack);
		state.setLoaded(isLoaded);

		setupGUI();

		for( BaseEntityTab<T> tab : tabLoadSaveOrder )
		{
			tab.load();
		}

		clearChanges();
	}

	public void refreshWindowTitle(String newTitle)
	{
		if( dialog != null )
		{
			if( newTitle == null )
			{
				newTitle = getCurrentName();
			}

			final String key = "com.tle.admin.baseentity.baseentityeditor.title-for-"
				+ (Check.isEmpty(newTitle) ? "unnamed" : "named") + "-entity"; //$NON-NLS-2$ //$NON-NLS-3$
			dialog.setTitle(CurrentLocale.get(key, getWindowTitle(), driver.getInstitutionName(), newTitle));
		}
	}

	public NameValue getEntityDetails()
	{
		return new NameValue(getCurrentName(), Long.toString(getEntity().getId()));
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#getParentWindow()
	 */
	@Override
	public Component getParentWindow()
	{
		return dialog;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#close()
	 */
	@Override
	public void close(Integer confirmOption)
	{
		dialog.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#getStatusBar()
	 */
	@Override
	public final JStatusBar getStatusBar()
	{
		return statusbar;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.admin.gui.EditorInterface#isReadOnly()
	 */
	@Override
	public final boolean isReadOnly()
	{
		return state.isReadonly();
	}

	@Override
	public final void unlock()
	{
		if( state.isLoaded() )
		{
			try
			{
				tool.cancelEdit(state.getEntity().getId());
			}
			catch( Exception ex )
			{
				LOGGER.warn("Problem unlocking schema", ex);
			}
		}
	}

	@Override
	public final void save() throws EditorException
	{
		for( BaseEntityTab<T> tab : tabLoadSaveOrder )
		{
			tab.save();
		}

		try
		{
			if( state.isLoaded() )
			{
				state.setEntity(tool.stopEdit(state.getEntityPack(), false));
			}
			else
			{
				T entity = state.getEntity();
				if( Check.isEmpty(entity.getOwner()) )
				{
					entity.setOwner(driver.getLoggedInUserUUID());
				}

				BaseEntityLabel label = tool.add(state.getEntityPack(), true);
				entity.setId(label.getId());
				state.setLoaded(true);
			}

			state.setSaved(true);
			clearChanges();

			for( BaseEntityTab<T> tab : tabLoadSaveOrder )
			{
				tab.afterSave();
			}
		}
		catch( InvalidDataException ex )
		{
			for( ValidationError error : ex.getErrors() )
			{
				LOGGER.error("Validation error in " + error.getField() + ": " + error.getMessage()); //$NON-NLS-2$
			}
			ValidationError error = ex.getErrors().get(0);
			throw new EditorException("Validation error in " + error.getField() + ": " + error.getMessage());
		}
		catch( Exception ex )
		{
			throw new EditorException(tool.getSavingErrorMessage(), ex);
		}
	}

	@Override
	public final void validation() throws EditorException
	{
		int current = 0;

		try
		{
			for( BaseEntityTab<T> tab : btabs )
			{
				tab.validation();
				current++;
			}

			// If we get to here, all the tabs are valid.
		}
		catch( EditorException ex )
		{
			// If we get to here, one of the tabs is invalid.
			tabs.setSelectedIndex(current);
			throw ex;
		}
	}

	@Override
	public boolean beforeSaving()
	{
		return true;
	}

	private final TLEAction saveAction = new SaveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			EditorHelper.onSave(BaseEntityEditor.this);
		}
	};

	private final TLEAction closeAction = new CloseAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			EditorHelper.onClose(BaseEntityEditor.this, null);
		}
	};

	protected class NameChangeListener extends KeyAdapter
	{
		@Override
		public void keyTyped(final KeyEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					String t = ((JTextComponent) e.getComponent()).getText();
					refreshWindowTitle(t);
				}
			});
		}
	}

	public interface AbstractDetailsTab<T>
	{
		void addNameListener(KeyListener listener);
	}

	protected T getEntity()
	{
		return state.getEntity();
	}

	public JDialog getDialog()
	{
		return dialog;
	}

	public JTabbedPane getTabbedPane()
	{
		return tabs;
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}
}
