package com.tle.web.appletcommon.gui;

import com.tle.web.appletcommon.io.ProgressMonitorCallback;

public class InterruptAwareGlassPaneProgressMonitorCallback implements ProgressMonitorCallback
{
	private final GlassProgressWorker<?> worker;

	public InterruptAwareGlassPaneProgressMonitorCallback(GlassProgressWorker<?> worker)
	{
		this.worker = worker;
	}

	@Override
	public void addToProgress(int value)
	{
		if( Thread.currentThread().isInterrupted() )
		{
			throw new UserCancelledException();
		}
		worker.addProgress(value);
	}
}