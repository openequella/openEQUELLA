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

package com.tle.core.activation.workflow;

import java.util.Date;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface OperationFactory
{
	RolloverOperation createRollover(long courseId, @Assisted("from") Date from, @Assisted("until") Date until);

	DeactivateOperation createDeactivate(long requestId);

	DeleteActivationOperation createDelete(long requestId);

	DeactivateOperation createDeactivate();

	DeleteActivationOperation createDelete();

	ActivateOperation createActivate(String activationType);

	ReassignActivationOperation reassignActivations(@Assisted("fromUserId") String fromUserId,
		@Assisted("toUserId") String toUserId);
}
