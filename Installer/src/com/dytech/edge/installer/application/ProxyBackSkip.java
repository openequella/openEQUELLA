package com.dytech.edge.installer.application;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;

/**
 * @author Nicholas Read
 */
public class ProxyBackSkip implements Callback
{
	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.Callback#task(com.dytech.installer.Wizard)
	 */
	@Override
	public void task(Wizard installer)
	{
		PropBagEx output = installer.getOutputNow();
		if( output.isNodeTrue("proxy/connection") )
		{
			installer.gotoRelativePage(-1);
		}
		else
		{
			installer.gotoRelativePage(-2);
		}
	}
}
