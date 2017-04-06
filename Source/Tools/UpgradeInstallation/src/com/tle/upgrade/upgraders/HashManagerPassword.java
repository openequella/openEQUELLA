package com.tle.upgrade.upgraders;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.tle.common.Check;
import com.tle.common.hash.Hash;
import com.tle.upgrade.UpgradeResult;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class HashManagerPassword extends AbstractUpgrader
{
	private static final Logger LOGGER = Logger.getLogger(HashManagerPassword.class);

	@Override
	public String getId()
	{
		return "HashManagerPassword";
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception
	{
		final Properties userPassMap = new Properties();
		final File users = new File(tleInstallDir, "manager/users.properties");

		int count = 0;
		try( InputStream in = new BufferedInputStream(new FileInputStream(users)) )
		{
			userPassMap.load(in);

			for( Entry<Object, Object> entry : userPassMap.entrySet() )
			{
				final String val = (String) entry.getValue();
				if( !Check.isEmpty(val) )
				{
					if( !Hash.isHashed(val) )
					{
						count++;
						entry.setValue(Hash.hashPassword(val));
					}
				}
			}
		}

		try( OutputStream out = new FileOutputStream(users) )
		{
			userPassMap.store(out, null);
		}

		LOGGER.info("Hashed " + count + " manager user passwords");
	}
}
