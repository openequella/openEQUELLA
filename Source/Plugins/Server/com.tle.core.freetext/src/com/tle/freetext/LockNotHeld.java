/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
