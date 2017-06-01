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

package com.tle.admin.controls;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.PluginDescriptor;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.FixedMetadata;
import com.dytech.edge.wizard.beans.WizardPage;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.google.common.base.Throwables;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.controls.repository.ControlRepository;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginService;

/**
 * @author Nicholas Read
 */
public class ControlRepositoryImpl implements ControlRepository
{
	/**
	 * The default icon for controls that do not define one.
	 */
	private static final String DEFAULT_ICON = "control.gif"; //$NON-NLS-1$

	/**
	 * A collection of ControlDefinition objects indexed by control ID. Holds
	 * all of the possible controls that can be used in wizard models.
	 */
	private final Map<String, ControlDefinition> definitions = new HashMap<String, ControlDefinition>();
	private final Map<String, List<ControlDefinition>> contextMap = new HashMap<String, List<ControlDefinition>>();

	/**
	 * A collection of icons for displaying with controls.
	 */
	private final Map<String, ImageIcon> icons = new HashMap<String, ImageIcon>();

	private final PluginService pluginService;

	/**
	 * Constructs a new ControlRepository.
	 */
	public ControlRepositoryImpl(PluginService pluginService)
	{
		this.pluginService = pluginService;

		for( Extension extension : pluginService.getConnectedExtensions("com.tle.admin.controls", "control") ) //$NON-NLS-1$ //$NON-NLS-2$
		{
			final String id = extension.getParameter("id").valueAsString(); //$NON-NLS-1$

			final ControlDefinition definition = new ControlDefinition();
			definition.setExtension(extension);
			definition.setId(id);
			definition.setName(CurrentLocale.get(extension.getParameter("name").valueAsString())); //$NON-NLS-1$

			final Parameter param = extension.getParameter("factoryClass"); //$NON-NLS-1$
			definition.setEditorFactoryClass(param != null ? param.valueAsString() : StandardEditorFactory.class
				.getName());

			for( Parameter cparam : extension.getParameters("context") ) //$NON-NLS-1$
			{
				String context = cparam.valueAsString();
				definition.getContexts().add(context);

				List<ControlDefinition> defs = contextMap.get(context);
				if( defs == null )
				{
					defs = new ArrayList<ControlDefinition>();
					contextMap.put(context, defs);
				}
				defs.add(definition);
			}

			definitions.put(id, definition);
		}

		for( List<ControlDefinition> defs : contextMap.values() )
		{
			Collections.sort(defs, controlSorter);
		}
	}

	/**
	 * Returns the control definition for the given ID.
	 */
	@Override
	public ControlDefinition getDefinition(String id)
	{
		ControlDefinition definition = definitions.get(id);
		if( definition == null )
		{
			throw new RuntimeException("No definition for:" + id); //$NON-NLS-1$
		}
		return definition;
	}

	/**
	 * Returns the icon for the given ID and whether it watermarked as having a
	 * script.
	 */
	@Override
	public Icon getIcon(String id, boolean scripted)
	{
		String key = id + ":" + scripted; //$NON-NLS-1$

		ImageIcon icon = icons.get(key);
		if( icon == null )
		{
			// Construct the icon
			ControlDefinition definition = getDefinition(id);
			Extension extension = definition.getExtension();
			Parameter param = extension.getParameter("icon"); //$NON-NLS-1$
			String iconPath = param != null ? param.valueAsString() : "/icons/" + DEFAULT_ICON; //$NON-NLS-1$
			icon = new ControlIcon(getClass().getResource(iconPath), scripted);

			// Put it back in to the map
			icons.put(key, icon);
		}
		return icon;
	}

	/**
	 * Provides graphics for an icon with a possible watermark.
	 * 
	 * @author Nicholas Read
	 */
	private static class ControlIcon extends ImageIcon
	{
		private static final long serialVersionUID = 1L;

		/**
		 * The size of the watermark.
		 */
		private static final int WATERMARK_SIZE = 4;

		/**
		 * Indicates whether to draw a watermark or not.
		 */
		private final boolean watermark;

		/**
		 * Creates a new ControlIcon.
		 */
		public ControlIcon(URL location, boolean watermark)
		{
			super(location);
			this.watermark = watermark;
		}

		@Override
		public synchronized void paintIcon(Component c, Graphics g, int x, int y)
		{
			super.paintIcon(c, g, x, y);
			if( watermark )
			{
				int wx = getIconWidth() - WATERMARK_SIZE;
				int wy = getIconHeight() - WATERMARK_SIZE;
				g.setColor(Color.RED);
				g.fillRect(wx, wy, WATERMARK_SIZE, WATERMARK_SIZE);
			}
		}
	}

	@Override
	@SuppressWarnings("nls")
	public Object getModelForControl(String id)
	{
		ControlDefinition definition = getDefinition(id);
		Extension extension = definition.getExtension();
		PluginDescriptor plugin = extension.getDeclaringPluginDescriptor();
		pluginService.ensureActivated(plugin);
		ClassLoader classLoader = pluginService.getClassLoader(plugin);
		try
		{
			Class<?> modelClass = classLoader.loadClass(extension.getParameter("modelClass").valueAsString());
			Control controlModel = (Control) modelClass.getConstructor(ControlDefinition.class).newInstance(definition);
			controlModel.setControlRepository(this);
			return controlModel;
		}
		catch( Exception e ) /*
							 * ClassNotFoundException, InstantiationException,
							 * IllegalAccessException, IllegalArgumentException,
							 * InvocationTargetException
							 */
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String getIdForWizardObject(Object object)
	{
		if( object instanceof WizardControl )
		{
			return ((WizardControl) object).getClassType();
		}
		else if( object instanceof WizardPage )
		{
			return ((WizardPage) object).getType();
		}
		else if( object instanceof FixedMetadata )
		{
			return "metadata"; //$NON-NLS-1$
		}
		else if( object instanceof Wizard )
		{
			return "wizard"; //$NON-NLS-1$
		}
		else
		{
			throw new Error("Unknown wizard Object:" + object.getClass()); //$NON-NLS-1$
		}
	}

	@Override
	public Object getNewWrappedObject(String id)
	{
		ControlDefinition definition = getDefinition(id);
		Extension extension = definition.getExtension();
		Parameter modelParam = extension.getParameter("wrappedClass"); //$NON-NLS-1$
		PluginDescriptor descriptor = extension.getDeclaringPluginDescriptor();
		pluginService.ensureActivated(descriptor);
		ClassLoader classLoader = pluginService.getClassLoader(descriptor);
		try
		{
			Class<?> modelClass = classLoader.loadClass(modelParam != null ? modelParam.valueAsString()
				: CustomControl.class.getName());
			return modelClass.newInstance();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ControlDefinition> getDefinitionsForContext(String context)
	{
		List<ControlDefinition> defs = contextMap.get(context);
		if( defs == null )
		{
			return Collections.emptyList();
		}
		return defs;
	}

	@Override
	public List<ClassLoader> getClassLoaders()
	{
		List<ClassLoader> res = new ArrayList<ClassLoader>();
		for( ControlDefinition def : definitions.values() )
		{
			res.add(pluginService.getClassLoader(def.getExtension().getDeclaringPluginDescriptor()));
		}
		return res;
	}

	@SuppressWarnings("serial")
	private final Comparator<? super ControlDefinition> controlSorter = new NumberStringComparator<ControlDefinition>()
	{
		@Override
		public String convertToString(ControlDefinition def)
		{
			return def.getName();
		}
	};
}
