/*
 * Created on Nov 9, 2004
 */
package com.dytech.installer.foreign;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;

/**
 * @author Nicholas Read
 */
public class SaveResults extends ForeignCommand
{
	private File file;

	public SaveResults(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException
	{
		super(commandBag, resultBag);

		file = new File(getForeignValue("file"));
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Command#execute()
	 */
	@Override
	public void execute() throws InstallerException
	{
		file.getParentFile().mkdirs();
		if( !file.getParentFile().exists() )
		{
			throw new InstallerException("Could not create directory");
		}

		try( BufferedWriter out = new BufferedWriter(new FileWriter(file)) )
		{
			out.write(resultBag.toString());
		}
		catch( IOException ex )
		{
			throw new InstallerException("Could not write file " + file.getAbsolutePath(), ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Command#toString()
	 */
	@Override
	public String toString()
	{
		return "Saving Installer Log";
	}
}
