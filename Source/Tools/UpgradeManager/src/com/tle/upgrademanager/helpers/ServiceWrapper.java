package com.tle.upgrademanager.helpers;

import java.io.File;
import java.io.IOException;

import com.dytech.edge.common.Constants;
import com.tle.common.util.EquellaConfig;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;

@SuppressWarnings("nls")
public class ServiceWrapper
{
	private final EquellaConfig config;
	private final String equellaScript;
	private final String managerScript;

	public ServiceWrapper(EquellaConfig config)
	{
		this.config = config;
		equellaScript = ExecUtils.findExe(config.getManagerDir(), "equellaserver").getAbsolutePath();
		managerScript = ExecUtils.findExe(config.getManagerDir(), "manager").getAbsolutePath();
	}

	public void stop()
	{
		ExecResult exec = ExecUtils.exec(new String[]{equellaScript, "stop", "exit"}, null, config.getManagerDir());
		exec.ensureOk();

		// give it a small grace period... it really should be stopped by now
		// anyway
		try
		{
			Thread.sleep(3000);
		}
		catch( InterruptedException i )
		{
			// Nothing
		}
	}

	public void start() throws IOException
	{
		File lock = new File(config.getConfigDir(), Constants.UPGRADE_LOCK);
		if( lock.exists() )
		{
			lock.delete();
		}
		lock.createNewFile();

		ExecResult exec = ExecUtils.exec(new String[]{equellaScript, "start", "exit"}, null, config.getManagerDir());
		exec.ensureOk();
	}

	public boolean status()
	{
		ExecResult exec = ExecUtils.exec(new String[]{equellaScript, "quickstatus"}, null, config.getManagerDir());
		// check http://wrapper.tanukisoftware.org/doc/english/launch-win.html
		// for info on wrapper return values.
		return (exec.getExitStatus() & 2) == 2;
	}

	public void restartmanager()
	{
		ExecUtils.exec(new String[]{managerScript, "restart", "exit"}, null, config.getManagerDir());
		// We should be restarting by now, but let's really make sure :)
		System.exit(0);
	}
}
