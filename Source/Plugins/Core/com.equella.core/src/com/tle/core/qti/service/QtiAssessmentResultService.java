/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.qti.entity.QtiAssessmentResult;
import com.tle.common.qti.entity.QtiAssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;

@NonNullByDefault
public interface QtiAssessmentResultService {
  @Nullable
  QtiAssessmentResult getAssessmentResult(
      QtiAssessmentTest test,
      String resourceLinkId,
      String userId,
      String toolConsumerInstanceGuid);

  QtiAssessmentResult ensureAssessmentResult(
      QtiAssessmentTest test,
      String resourceLinkId,
      String userId,
      String toolConsumerInstanceGuid);

  AssessmentResult persistTestSessionState(
      QtiAssessmentTest test,
      TestSessionController testSessionController,
      QtiAssessmentResult qtiAssessmentResult);

  TestSessionController loadTestSessionState(
      ResolvedAssessmentTest resolvedAssessmentTest,
      @Nullable QtiAssessmentResult qtiAssessmentResult);

  AssessmentResult computeAssessmentResult(TestSessionController testSessionController);

  int countAttemptsByResourceLink(
      QtiAssessmentTest test,
      String resourceLinkId,
      String userId,
      String toolConsumerInstanceGuid);
}
