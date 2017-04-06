package com.dytech.installer;

class BasicNextCallback implements Callback
{
	@Override
	public void task(Wizard installer)
	{
		int page = installer.getCurrentPageNumber() + 1;
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