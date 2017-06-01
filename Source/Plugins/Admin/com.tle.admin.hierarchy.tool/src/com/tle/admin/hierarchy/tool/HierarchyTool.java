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

package com.tle.admin.hierarchy.tool;

import java.util.Set;

import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.AdminTool;
import com.tle.admin.hierarchy.HierarchyDialog;

/**
 * @author Nicholas Read
 */
public class HierarchyTool extends AdminTool
{
	public HierarchyTool()
	{
		// There is nothing for us to do here.
	}

	@Override
	public void toolSelected()
	{
		final GlassSwingWorker<HierarchyDialog> worker = new GlassSwingWorker<HierarchyDialog>()
		{
			@Override
			public HierarchyDialog construct()
			{
				// Make sure that the management panel has
				// enough time to change the look and feel
				// before we go creating things.
				try
				{
					Thread.sleep(500);
				}
				catch( InterruptedException ex )
				{
					// We don't care
				}
				return new HierarchyDialog(parentFrame, driver.getClientService());
			}

			@Override
			public void finished()
			{
				HierarchyDialog dialog = get();
				dialog.setModal(true);
				dialog.setVisible(true);
			}

			@Override
			public void exception()
			{
				getException().printStackTrace();
			}
		};

		worker.setComponent(parentFrame);
		worker.start();
	}

	@Override
	public void setup(Set<String> grantedPrivilges, String name)
	{
		// nothing to do
	}
}
