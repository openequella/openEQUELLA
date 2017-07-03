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

package com.tle.core.qti.service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.qti.beans.QtiTestDetails;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface QtiService
{
	ResolvedAssessmentTest loadV2Test(FileHandle handle, String basePath, String relativeFilePath);

	QtiTestDetails getTestDetails(ResolvedAssessmentTest test);

	boolean isResponded(AssessmentItem assessmentItem, ItemSessionState itemState);

	TestSessionController getNewTestSessionController(ResolvedAssessmentTest test);

	TestSessionController getTestSessionController(ResolvedAssessmentTest test, TestSessionState testSessionState);
}
