package com.tle.upgrade.upgraders;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.tle.common.util.EquellaConfig;
import com.tle.common.util.ExecUtils;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.UpgradeResult;

@SuppressWarnings("nls")
public class RemoveJavaMaxPermSize extends AbstractUpgrader
{
	@Override
	public String getId()
	{
		return "RemoveJavaMaxPermSize";
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		String platform = ExecUtils.determinePlatform();
		EquellaConfig config = new EquellaConfig(tleInstallDir);
		File managerDir = config.getManagerDir();
		String name = "equellaserver";
		if( platform.startsWith(ExecUtils.PLATFORM_WIN) )
		{
			new LineFileModifier(new File(managerDir, name + "-config.bat"), result)
			{
				@Override
				protected String processLine(String line)
				{
					if( line.startsWith("set JAVA_ARGS=") )
					{
						return updateJavaOpts(line, "set JAVA_ARGS=", ";");
					}
					return line;
				}

			}.update();
		}
		else
		{
			new LineFileModifier(new File(managerDir, name + "-config.sh"), result)
			{
				@Override
				protected String processLine(String line)
				{
					if( line.startsWith("export JAVA_OPTS=") )
					{
						return updateJavaOpts(line, "export JAVA_OPTS=\"", " ");
					}
					return line;
				}

			}.update();
		}

	}

	private String updateJavaOpts(String line, String prefix, String delim)
	{
		List<String> opts = Lists.newArrayList(Splitter.on(delim).split(line.substring(prefix.length())));
		// fix this shizzle up
		for( Iterator<String> iterator = opts.iterator(); iterator.hasNext(); )
		{
			if( iterator.next().startsWith("-XX:MaxPermSize") )
			{
				iterator.remove();
			}
		}
		return prefix + Joiner.on(delim).join(opts);
	}
}
