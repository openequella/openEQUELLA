package com.dytech.installer;

import java.util.Iterator;

import com.dytech.common.text.ResolverException;
import com.dytech.common.text.Substitution;
import com.dytech.devlib.PropBagEx;
import com.dytech.installer.commands.Command;

public abstract class ForeignCommand extends Command
{
	protected static Substitution resolver = null;

	protected PropBagEx commandBag;
	protected PropBagEx resultBag;

	public ForeignCommand(PropBagEx commandBag, PropBagEx resultBag)
	{
		this.commandBag = commandBag;
		this.resultBag = resultBag;

		if( resolver == null )
		{
			resolver = new Substitution(new XpathResolver(resultBag), "${ }");
		}
	}

	protected String getForeignValue(String key) throws InstallerException
	{
		Iterator iter = commandBag.iterator("foreign");
		while( iter.hasNext() )
		{
			PropBagEx foreign = (PropBagEx) iter.next();
			if( foreign.getNode("@key").equals(key) )
			{
				String value = foreign.getNode("@value");
				try
				{
					return resolver.resolve(value);
				}
				catch( ResolverException e )
				{
					throw new InstallerException(e);
				}
			}
		}
		return null;
	}
}