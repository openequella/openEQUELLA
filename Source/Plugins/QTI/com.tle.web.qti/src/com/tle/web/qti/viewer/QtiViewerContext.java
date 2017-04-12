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
