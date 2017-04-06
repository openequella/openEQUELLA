package com.tle.web.sections.ajax;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.PreRenderable;

public interface AjaxRenderContext extends PreRenderContext
{
	Writer startCapture(Writer out, String divId, Map<String, Object> captureParams, boolean collection);

	void endCapture(String divId);

	FullDOMResult getFullDOMResult();

	void setFormBookmarkEvent(BookmarkEvent event);

	void setJSONResponseCallback(JSONResponseCallback jsonResponseCallback);

	void addAjaxDivs(String... divIds);

	void addAjaxDivs(Collection<String> ajaxIds);

	boolean isCurrentlyCapturing();

	boolean isRenderingAjaxDiv(String divId);

	Map<String, FullAjaxCaptureResult> getAllCaptures();

	void captureResources(PreRenderable... preRenderables);

}
