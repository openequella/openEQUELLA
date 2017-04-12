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
