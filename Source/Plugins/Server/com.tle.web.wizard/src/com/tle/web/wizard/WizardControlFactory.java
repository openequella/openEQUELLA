package com.tle.web.wizard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.wizard.controls.WebControl;

@Bind
@Singleton
public class WizardControlFactory
{
	@Inject
	private PluginService pluginService;
	private Map<String, Extension> webControlExtensions;

	public synchronized Map<String, Extension> getWebControlExtensions()
	{
		if( webControlExtensions == null )
		{
			webControlExtensions = new HashMap<String, Extension>();
			Collection<Extension> extensions = pluginService.getConnectedExtensions("com.tle.web.wizard", "webControl"); //$NON-NLS-1$ //$NON-NLS-2$
			for( Extension extension : extensions )
			{
				webControlExtensions.put(extension.getParameter("type").valueAsString(), extension); //$NON-NLS-1$
			}
		}
		return webControlExtensions;
	}

	public WebControl createWebControl(HTMLControl control)
	{
		Map<String, Extension> extensions = getWebControlExtensions();
		String classType = control.getControlBean().getClassType();
		Extension extension = extensions.get(classType);
		if( extension != null )
		{
			PluginDescriptor plugin = extension.getDeclaringPluginDescriptor();
			WebControl webControl = (WebControl) pluginService.getBean(plugin, extension
				.getParameter("class").valueAsString()); //$NON-NLS-1$
			webControl.setWrappedControl(control);
			return webControl;
		}
		return new BrokenWebControl(control);
	}

}
