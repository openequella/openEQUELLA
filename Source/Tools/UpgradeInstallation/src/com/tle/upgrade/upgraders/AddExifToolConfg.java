package com.tle.upgrade.upgraders;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.tle.common.util.EquellaConfig;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.UpgradeResult;

@SuppressWarnings("nls")
public class AddExifToolConfg extends AbstractUpgrader
{

	@Override
	public String getId()
	{
		return "AddExifToolConfig";
	}

	@Override
	public List<UpgradeDepends> getDepends()
	{
		UpgradeDepends dep1 = new UpgradeDepends(UpgradeToEmbeddedTomcat.ID);
		return Lists.newArrayList(dep1);
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		EquellaConfig config = new EquellaConfig(tleInstallDir);

		result.addLogMessage("Updating optional-config properties");
		updateOptionalProperties(result, config.getConfigDir());
	}

	private void updateOptionalProperties(final UpgradeResult result, File configDir)
	{
		try
		{
			LineFileModifier lineMod = new LineFileModifier(new File(configDir, PropertyFileModifier.OPTIONAL_CONFIG),
				result)
			{
				@Override
				protected String processLine(String line)
				{
					// Do nothing
					return line;
				}

				@Override
				protected List<String> addLines()
				{
					String exifToolComment = "# ExifTool path";
					String exifToolProp = "#exiftool.path = /path/to/exiftool";
					return Lists.newArrayList(Constants.BLANK, exifToolComment, exifToolProp);
				}
			};

			lineMod.update();
		}
		catch( IOException e )
		{
			throw new RuntimeException("Failed to update config file", e);
		}
	}
}
