package com.dytech.installer;

class BasicFinishedCallback implements Callback
{
	@Override
	public void task(Wizard installer)
	{
		installer.finished();
	}
}