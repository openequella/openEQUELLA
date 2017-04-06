package com.tle.freetext;

// //////////////////////////////////////////
// LockNotHeld.java //
// 2000 Tarak Modi, All rights reserved.//
// //
// //////////////////////////////////////////

// This exception is thrown when a thread that
// does not hold a lock tries to release, upgrade, or downgrade a lock.
public class LockNotHeld extends Exception
{
	private static final long serialVersionUID = 1L;

	public LockNotHeld()
	{
		super();
	}
}
