/*
 * Copyright 2019 Apereo
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

package com.tle.web.appletcommon.gui;

import javax.swing.SwingUtilities;

import com.dytech.gui.workers.AbstractGlassSwingWorker;

/**
 * @author Nicholas Read
 */
public abstract class GlassProgressWorker<RESULT> extends AbstractGlassSwingWorker<RESULT, GlassProgressPane>
{
	private final int maxProgress;
	private final String message;
	private final boolean cancellable;

	public GlassProgressWorker(String message, int maxProgress, boolean cancellable)
	{
		super(GlassProgressPane.class);

		this.message = message;
		this.maxProgress = maxProgress;
		this.cancellable = cancellable;
	}

	@Override
	protected GlassProgressPane constructGlassPane()
	{
		return new GlassProgressPane(message, maxProgress, getComponent(), isDisallowClosing(), this, cancellable);
	}

	@Override
	protected void processExistingGlassPane(GlassProgressPane gp)
	{
		gp.setWorker(this);
		gp.setCancellable(cancellable);
		gp.setMessage(message);
		gp.setTotal(maxProgress);
		gp.resetProgress();
	}

	public void addProgress(final int value)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				getGlassPane().addProgress(value);
			}
		});
	}

	public void setTotal(final int total)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				getGlassPane().setTotal(total);
			}
		});
	}

	public void setMessage(final String messageText)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				getGlassPane().setMessage(messageText);
			}
		});
	}
}