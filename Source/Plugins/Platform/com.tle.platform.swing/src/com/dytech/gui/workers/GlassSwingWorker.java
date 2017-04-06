package com.dytech.gui.workers;

/**
 * @author Nicholas Read
 */
public abstract class GlassSwingWorker<RESULT> extends AbstractGlassSwingWorker<RESULT, BusyGlassPane>
{
	public GlassSwingWorker()
	{
		super(BusyGlassPane.class);
	}

	@Override
	protected BusyGlassPane constructGlassPane()
	{
		return new BusyGlassPane(getComponent(), isDisallowClosing());
	}

	@Override
	protected void processExistingGlassPane(BusyGlassPane gp)
	{
		// Nothing to do here
	}
}