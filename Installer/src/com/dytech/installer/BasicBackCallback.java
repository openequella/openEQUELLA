package com.dytech.installer;

class BasicBackCallback implements Callback
{
	@Override
	public void task(Wizard installer)
	{
		int page = installer.getCurrentPageNumber() - 1;
		try
		{
			installer.gotoPage(page);
		}
		catch( InstallerException e )
		{
			System.out.println(e.toString());
		}
	}
}