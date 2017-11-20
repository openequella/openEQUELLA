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

package com.tle.web.bulk.workflowtask;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.web.bulk.workflowtask.operations.TaskApproveOperation;
import com.tle.web.bulk.workflowtask.operations.TaskReassignModeratorOperation;
import com.tle.web.bulk.workflowtask.operations.TaskRejectOperation;

@BindFactory
public interface BulkWorkflowTaskOperationFactory
{
	TaskApproveOperation approve(@Assisted("message") String message,
		@Assisted("acceptAllUsers") boolean acceptAllUsers);

	TaskRejectOperation reject(@Assisted("message") String message);

	TaskReassignModeratorOperation changeModeratorAssign(@Assisted("toUser") String toUser);
}
