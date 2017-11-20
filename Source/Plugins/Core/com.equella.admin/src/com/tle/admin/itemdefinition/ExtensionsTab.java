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

package com.tle.admin.itemdefinition;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.dytech.gui.VerticalFlowLayout;
import com.tle.admin.PluginServiceImpl;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.i18n.CurrentLocale;

public class ExtensionsTab extends AbstractItemdefTab
{
	private static class ExtensionHandle
	{
		private final String name;
		private final String enabledAttribute;
		private final Class<? extends AbstractExtensionConfigPanel> configPanelClass;
		private final JCheckBox enabledCheckbox;

		@SuppressWarnings("unchecked")
		public ExtensionHandle(PluginServiceImpl service, Extension ext) throws ClassNotFoundException
		{
			this.name = CurrentLocale.get(ext.getParameter("name").valueAsString()); //$NON-NLS-1$
			this.enabledAttribute = ext.getParameter("enabledAttribute").valueAsString(); //$NON-NLS-1$

			Parameter param = ext.getParameter("configPanel"); //$NON-NLS-1$

			this.configPanelClass = param == null ? null : (Class<? extends AbstractExtensionConfigPanel>) service
				.getBeanClass(ext.getDeclaringPluginDescriptor(), param.valueAsString());
			this.enabledCheckbox = new JCheckBox(name);
		}

		public String getName()
		{
			return name;
		}

		public String getEnabledAttribute()
		{
			return enabledAttribute;
		}

		public Class<? extends AbstractExtensionConfigPanel> getConfigPanelClass()
		{
			return configPanelClass;
		}

		public JCheckBox getEnabledCheckbox()
		{
			return enabledCheckbox;
		}
	}

	private final List<ExtensionHandle> extensions = new ArrayList<ExtensionHandle>();
	private String stagingId;

	public ExtensionsTab()
	{
		super();
	}

	@Override
	public void init(final Component parent)
	{
		for( Extension ext : pluginService.getConnectedExtensions("com.tle.admin.collection.tool", //$NON-NLS-1$
			"extra") ) //$NON-NLS-1$
		{
			try
			{
				extensions.add(new ExtensionHandle(pluginService, ext));
			}
			catch( Exception ex )
			{
				LOGGER.error("Error processing extension " + ext.getId(), ex);
			}
		}

		setLayout(new VerticalFlowLayout());

		if( extensions.isEmpty() )
		{
			add(new JLabel("No extensions available"));
		}
		else
		{
			for( final ExtensionHandle extension : extensions )
			{
				final JCheckBox checkbox = extension.getEnabledCheckbox();
				checkbox.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ItemDefinition entity = state.getEntity();
						if( checkbox.isSelected() )
						{
							entity.setAttribute(extension.getEnabledAttribute(), Boolean.TRUE.toString());
						}
						else
						{
							entity.removeAttribute(extension.getEnabledAttribute());
						}
					}
				});

				if( extension.getConfigPanelClass() == null )
				{
					add(checkbox);
				}
				else
				{
					final JButton button = new JButton(
						CurrentLocale.get("com.tle.admin.itemdefinition.extensionstab.config")); //$NON-NLS-1$
					button.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							final AbstractExtensionConfigPanel configPanel;
							try
							{
								configPanel = extension.getConfigPanelClass().newInstance();
								configPanel.setClientService(clientService);
								configPanel.load(stagingId, state.getEntity());
							}
							catch( Exception ex )
							{
								throw new RuntimeException("Could not create config panel instance", ex);
							}

							JButton save = new JButton("OK");
							JButton cancel = new JButton("Cancel");

							final Dimension size = cancel.getPreferredSize();
							final int[] rows = {TableLayout.FILL, size.height,};
							final int[] cols = {TableLayout.FILL, size.width, size.width,};

							JPanel panel = new JPanel(new TableLayout(rows, cols, 5, 5));
							panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
							panel.add(configPanel, new Rectangle(0, 0, 3, 1));
							panel.add(save, new Rectangle(1, 1, 1, 1));
							panel.add(cancel, new Rectangle(2, 1, 1, 1));

							final JDialog dialog = ComponentHelper.createJDialog(button);
							dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
							dialog.setTitle(extension.getName());
							dialog.getContentPane().add(panel);
							dialog.setModal(true);
							dialog.setSize(600, 500);
							ComponentHelper.centreOnScreen(dialog);

							save.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent e)
								{
									configPanel.save(state.getEntity());
									dialog.dispose();

									// Dirty rotten hacks
									((JChangeDetectorPanel) ExtensionsTab.this.getComponent()).forceChange();
								}
							});

							cancel.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent e)
								{
									dialog.dispose();
								}
							});

							dialog.setVisible(true);
						}
					});

					JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
					panel.add(checkbox);
					panel.add(button);

					add(panel);
				}
			}
		}
	}

	@Override
	public void validation()
	{
		// Nothing to validate here.
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.extensionstab.title"); //$NON-NLS-1$
	}

	@Override
	public void load()
	{
		stagingId = state.getEntityPack().getStagingID();

		ItemDefinition itemdef = state.getEntity();
		for( ExtensionHandle extension : extensions )
		{
			boolean enabled = itemdef.getAttribute(extension.getEnabledAttribute(), false);
			extension.getEnabledCheckbox().setSelected(enabled);
		}
	}

	@Override
	public void save()
	{
		// Nothing to do here
	}
}
