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

package com.tle.web.qti.viewer;

import java.util.List;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Value;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Pair;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.SubmitValuesFunction;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface QtiViewerContext
{
	RenderContext getRenderContext();

	TestSessionState getSessionState();

	TestSessionController getTestSessionController();

	ItemSessionController getItemSessionController();

	Bookmark getViewResourceUrl(String url);

	UpdateDomFunction getValueChangedFunction();

	SubmitValuesFunction getEndAttemptFunction();

	@Nullable
	List<String> getValues(Identifier responseIdentifier);

	Value evaluateVariable(@Nullable QtiNode caller, Identifier variableId);

	void addError(String message, Identifier interactionId);

	List<Pair<String, Identifier>> getErrors();
}
