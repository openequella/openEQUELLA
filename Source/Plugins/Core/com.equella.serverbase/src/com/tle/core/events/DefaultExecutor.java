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

package com.tle.core.events;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.tle.common.NamedThreadFactory;

public final class DefaultExecutor
{
	private static final int MAX_THREADS = 150;

	public static final ExecutorService executor;

	private DefaultExecutor()
	{
		throw new Error();
	}

	static
	{
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 60L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("DefaultExecutor.executor"));
		tpe.allowCoreThreadTimeOut(true);
		executor = tpe;
	}
}
