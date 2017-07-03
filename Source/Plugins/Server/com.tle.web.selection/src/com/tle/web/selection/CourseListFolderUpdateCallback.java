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

package com.tle.web.selection;

import java.util.Map;

import com.tle.common.Check;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.ajax.AbstractDOMResult;
import com.tle.web.sections.ajax.AjaxEffects;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.FullAjaxCaptureResult;
import com.tle.web.sections.ajax.FullDOMResult;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.js.EventGeneratorListener;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;

@SuppressWarnings("nls")
public class CourseListFolderUpdateCallback implements JSONResponseCallback
{
	private static final IncludeFile INCJS = new IncludeFile(
		ResourcesService.getResourceHelper(CourseListFolderUpdateCallback.class).url("scripts/courselist.js"),
		AjaxEffects.EFFECTS_LIB, JQueryUIEffects.HIGHLIGHT, JQueryUIEffects.BLIND);
	private static final JSCallAndReference COURSE_LIST_CLASS = new ExternallyDefinedFunction("CourseList");
	private static final JSCallable UPDATE_TARGET_FOLDER = new ExternallyDefinedFunction(COURSE_LIST_CLASS,
		"updateTargetFolder", 4, INCJS);

	protected AjaxRenderContext context;
	protected CourseListFolderAjaxUpdateData data;

	public CourseListFolderUpdateCallback(AjaxRenderContext context, CourseListFolderAjaxUpdateData reloadData)
	{
		this.context = context;
		this.data = reloadData;
		String[] event = data.getEvent();
		context.addAjaxDivs(data.getAjaxIds());
		if( !Check.isEmpty(event) )
		{
			MutableSectionInfo minfo = context.getAttributeForClass(MutableSectionInfo.class);
			ParametersEvent paramEvent = new ParametersEvent(EventGeneratorListener.convertToParamMap(event), false);
			minfo.addParametersEvent(paramEvent);
			minfo.processEvent(paramEvent);
		}
	}

	// public abstract Map<String, List<ControlResult>> getPageResults();

	public Map<String, String> getHiddenState()
	{
		return null;
	}

	@Override
	public Object getResponseObject(AjaxRenderContext context)
	{
		for( FullAjaxCaptureResult captureResult : context.getAllCaptures().values() )
		{
			Map<String, Object> params = captureResult.getParams();
			addEffectLibrary(context, params, "showEffect");
			addEffectLibrary(context, params, "hideEffect");
		}

		FullDOMResult fullDOMResult = context.getFullDOMResult();
		Map<String, FullAjaxCaptureResult> htmlMap = fullDOMResult.getHtml();

		return new UpdateFoldersObj(fullDOMResult, data.getFolderId(), htmlMap);
	}

	private void addEffectLibrary(AjaxRenderContext context, Map<String, Object> params, String effect)
	{
		Object effectName = params.get(effect);
		if( effectName instanceof String )
		{
			context.captureResources(JQueryUIEffects.getEffectLibrary((String) effectName));
		}
	}

	public static class UpdateFoldersObj extends AbstractDOMResult
	{
		private final String folderId;
		private final Map<String, FullAjaxCaptureResult> updates;

		public UpdateFoldersObj(AbstractDOMResult result, String folderId, Map<String, FullAjaxCaptureResult> updates)
		{
			super(result);
			this.folderId = folderId;
			this.updates = updates;
		}

		public String getFolderId()
		{
			return folderId;
		}

		public Map<String, FullAjaxCaptureResult> getUpdates()
		{
			return updates;
		}
	}

	/**
	 * Wraps the CourseListSection.reloadFolder function in the
	 * updateTargetFolder js function
	 * 
	 * @param drop
	 * @param event
	 * @param ajaxIds
	 * @return
	 */
	public static JSCallable getReloadFunction(JSCallable ajaxFunction)
	{
		return new PrependedParameterFunction(CourseListFolderUpdateCallback.UPDATE_TARGET_FOLDER, ajaxFunction);
	}
}