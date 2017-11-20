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

package com.tle.core.hibernate;

public abstract class CurrentDataSource
{
	private static ThreadLocal<DataSourceHolder> threadLocal = new ThreadLocal<DataSourceHolder>();

	public static DataSourceHolder get()
	{
		return threadLocal.get();
	}

	public static void set(DataSourceHolder dataSource)
	{
		threadLocal.set(dataSource);
	}

	public static void remove()
	{
		threadLocal.remove();
	}
}
