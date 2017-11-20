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

package com.tle.core.cluster.service;

import java.io.Serializable;

public interface ClusterMessagingService
{
	static final int MAX_MSG_SIZE = 5 * 1024 * 1024;
	static final int MAX_QUEUE_SIZE = 20 * 1024 * 1024;

	void postMessage(Serializable msg);

	void postMessage(String toNodeIdOnly, Serializable msg);
}
