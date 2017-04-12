package com.tle.freetext;

// //////////////////////////////////////////
// UpgradeNotAllowed.java //
// 2000 Tarak Modi, All rights reserved.//
// //
// //////////////////////////////////////////

// This exception is thrown when an upgrade method is
// called on a lock that has this capability turned off
public class UpgradeNotAllowed extends Exception
{
	private static final long serialVersionUID = 1L;

	public UpgradeNotAllowed()
	{
		super();
	}
}
