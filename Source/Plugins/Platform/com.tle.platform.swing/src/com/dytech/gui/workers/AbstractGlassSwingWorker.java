package com.dytech.gui.workers;

import java.awt.Component;

/**
 * @author Nicholas Read
 */
public abstract class AbstractGlassSwingWorker<RESULT, GLASSPANE extends Component> extends AdvancedSwingWorker<RESULT>
{
	private final Class<GLASSPANE> glassPaneClass;

	private Component component;
	private GLASSPANE glassPane;
	private boolean disallowClosing;
	private boolean alwaysRecreateGlasspane;

	public AbstractGlassSwingWorker(Class<GLASSPANE> glassPaneClass)
	{
		this.glassPaneClass = glassPaneClass;
		setDisallowClosing(true);
	}

	protected abstract GLASSPANE constructGlassPane();

	protected abstract void processExistingGlassPane(GLASSPANE gp);

	protected GLASSPANE getGlassPane()
	{
		return glassPane;
	}

	private void activateGlassPane()
	{
		// Mount the glasspane on the component window
		glassPane = GlassPaneUtils.mount(getComponent(), isDisallowClosing(), isAlwaysRecreateGlasspane(),
			glassPaneClass, new GlassPaneUtils.GlassPaneCallback<GLASSPANE>()
			{
				@Override
				public GLASSPANE construct()
				{
					return constructGlassPane();
				}

				@Override
				public void processExisting(GLASSPANE gp)
				{
					processExistingGlassPane(gp);
				}
			});

		if( glassPane != null )
		{
			// Start interception UI interactions
			glassPane.setVisible(true);
		}
	}

	@Override
	protected void afterException()
	{
		if( getComponent() != null )
		{
			getComponent().requestFocus();
		}
	}

	@Override
	protected void afterFinished()
	{
		if( getComponent() != null )
		{
			getComponent().requestFocus();
		}
	}

	@Override
	protected void beforeConstruct()
	{
		activateGlassPane();
	}

	@Override
	protected void beforeException()
	{
		deactivateGlassPane();
	}

	@Override
	protected void beforeFinished()
	{
		deactivateGlassPane();
	}

	private void deactivateGlassPane()
	{
		if( glassPane != null )
		{
			// Stop UI interception
			glassPane.setVisible(false);
		}
	}

	/**
	 * @return Returns the component.
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * @param component The component to set.
	 */
	public void setComponent(Component component)
	{
		this.component = component;
	}

	/**
	 * Indicates whether the window will not be allowed to close via the
	 * top-right X.
	 * 
	 * @return Returns the stopClosing.
	 */
	public boolean isDisallowClosing()
	{
		return disallowClosing;
	}

	/**
	 * @param stopClosing The stopClosing to set.
	 */
	public void setDisallowClosing(boolean stopClosing)
	{
		this.disallowClosing = stopClosing;
	}

	public void setAlwaysRecreateGlasspane(boolean alwaysRecreateGlasspane)
	{
		this.alwaysRecreateGlasspane = alwaysRecreateGlasspane;
	}

	public boolean isAlwaysRecreateGlasspane()
	{
		return alwaysRecreateGlasspane;
	}
}