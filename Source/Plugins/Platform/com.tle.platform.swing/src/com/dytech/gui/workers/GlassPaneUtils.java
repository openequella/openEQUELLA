package com.dytech.gui.workers;

import java.awt.Component;

import javax.swing.RootPaneContainer;

public final class GlassPaneUtils
{
	private GlassPaneUtils()
	{
		throw new Error();
	}

	public synchronized static <T extends Component> T mount(Component startComponent, boolean stopClosing,
		boolean alwaysRecreateGlasspane, Class<T> paneClass, GlassPaneCallback<T> callback)
	{
		RootPaneContainer rootpane = getRootContainer(startComponent);
		if( rootpane != null )
		{
			Component gp = rootpane.getGlassPane();
			if( !alwaysRecreateGlasspane )
			{
				if( paneClass.isAssignableFrom(gp.getClass()) )
				{
					if( !gp.isVisible() )
					{
						T ogp = paneClass.cast(gp);
						callback.processExisting(ogp);
						return ogp;
					}
					else
					{
						return null;
					}
				}
			}

			T ngp = callback.construct();
			rootpane.setGlassPane(ngp);
			return ngp;
		}
		return null;
	}

	private static RootPaneContainer getRootContainer(Component startComponent)
	{
		Component aComponent = startComponent;

		// Climb the component hierarchy until a RootPaneContainer is found or
		// until the very top
		while( (aComponent.getParent() != null) && !(aComponent instanceof RootPaneContainer) )
		{
			aComponent = aComponent.getParent();
		}

		// Guard against error conditions if climb search wasn't successful
		if( aComponent instanceof RootPaneContainer )
		{
			return (RootPaneContainer) aComponent;
		}

		return null;
	}

	public interface GlassPaneCallback<T>
	{
		T construct();

		void processExisting(T gp);
	}
}
