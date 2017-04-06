package com.dytech.installer;

class BasicQuitCallback implements Callback
{
	@Override
	public void task(Wizard installer)
	{
		installer.quit();
	}
}