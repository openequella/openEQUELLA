package com.dytech.installer;

class BasicBackTwoCallback implements Callback
{
	@Override
	public void task(Wizard installer)
	{
		try
		{
			installer.gotoRelativePage(-2);
		}
		catch( InstallerException e )
		{
			System.out.println(e.toString());
		}
	}
}