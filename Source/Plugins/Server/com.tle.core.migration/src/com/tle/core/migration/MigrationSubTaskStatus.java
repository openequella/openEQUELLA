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

package com.tle.core.migration;

import java.io.Serializable;

public class MigrationSubTaskStatus implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String statusKey;
	private int currentDone;
	private int currentMax;
	private int done;
	private int max;

	public MigrationSubTaskStatus(String statusKey, int max)
	{
		this.max = max;
		this.currentMax = max;
		this.statusKey = statusKey;
	}

	public String getStatusKey()
	{
		return statusKey;
	}

	public void setStatusKey(String statusKey)
	{
		this.statusKey = statusKey;
	}

	public int getDone()
	{
		return done;
	}

	public void setDone(int done)
	{
		this.done = done;
	}

	public int getMax()
	{
		return max;
	}

	public void setMax(int max)
	{
		this.max = max;
	}

	public void increment()
	{
		this.done++;
		currentDone++;
	}

	public int getCurrentMax()
	{
		return currentMax;
	}

	public void setCurrentMax(int currentMax)
	{
		this.currentMax = currentMax;
	}

	public int getCurrentDone()
	{
		return currentDone;
	}

	public void setCurrentDone(int currentDone)
	{
		this.currentDone = currentDone;
	}
}
