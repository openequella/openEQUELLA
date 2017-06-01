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

package com.tle.upgrade.upgraders;

import java.io.File;
import java.util.Properties;

import com.dytech.common.io.FileUtils;
import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeResult;

public class DeleteLuceneIndex extends AbstractUpgrader
{

	@Override
	public String getId()
	{
		// Change the 0 to force this to happen again
		return DeleteLuceneIndex.class.getName() + "0";
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		// It is marked as backwards compatible so
		// you dont have to keep creating
		// new classes for this
		return true;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		result.setCanRetry(true);
		final File configFolder = new File(tleInstallDir, CONFIG_FOLDER);
		Properties props = loadProperties(new File(configFolder, PropertyFileModifier.MANDATORY_CONFIG));
		File indexDir = new File((String) props.get("freetext.index.location"));
		if( !indexDir.isAbsolute() )
		{
			result.addLogMessage("Freetext dir is not absolute (" + indexDir + "), skipping");
			return;
		}
		FileUtils.delete(indexDir);
	}
}
