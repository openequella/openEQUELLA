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

package com.tle.web.wizard.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tle.common.Check;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.ajax.AbstractDOMResult;
import com.tle.web.sections.ajax.AjaxCaptureResult;
import com.tle.web.sections.ajax.AjaxEffects;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.FullAjaxCaptureResult;
import com.tle.web.sections.ajax.FullDOMResult;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.js.EventGeneratorListener;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;

@SuppressWarnings("nls")
public abstract class PageUpdateCallback implements JSONResponseCallback
{
	private static final IncludeFile INCJS = new IncludeFile(
		ResourcesService.getResourceHelper(PageUpdateCallback.class).url("scripts/updatecontrols.js"),
		AjaxEffects.EFFECTS_LIB, JQueryUIEffects.HIGHLIGHT, JQueryUIEffects.BLIND);
	private static final JSCallable UPDATE_CONTROLS = new ExternallyDefinedFunction("updateControls", 4, INCJS);

	protected AjaxRenderContext context;
	protected AjaxUpdateData data;

	public PageUpdateCallback(AjaxRenderContext context, AjaxUpdateData reloadData)
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

	public abstract Map<String, List<ControlResult>> getPageResults();

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
		Map<String, AjaxCaptureResult> html = new HashMap<String, AjaxCaptureResult>();
		Map<String, FullAjaxCaptureResult> htmlMap = fullDOMResult.getHtml();
		Map<String, List<ControlResult>> controlLists = getPageResults();
		Map<String, List<String>> visibleControls = new HashMap<String, List<String>>();
		for( Map.Entry<String, List<ControlResult>> entry : controlLists.entrySet() )
		{
			List<String> ids = new ArrayList<String>();
			for( ControlResult control : entry.getValue() )
			{
				String sectionId = control.getSectionId();
				ids.add(sectionId);
				FullAjaxCaptureResult captured = htmlMap.remove(sectionId);
				if( captured != null )
				{
					html.put(sectionId, new AjaxCaptureResult(captured));
				}
			}
			visibleControls.put(entry.getKey(), ids);
		}
		return new UpdateControlsObj(fullDOMResult, html, htmlMap, visibleControls, fullDOMResult.getLists(),
			getHiddenState());
	}

	private void addEffectLibrary(AjaxRenderContext context, Map<String, Object> params, String effect)
	{
		Object effectName = params.get(effect);
		if( effectName instanceof String )
		{
			context.captureResources(JQueryUIEffects.getEffectLibrary((String) effectName));
		}
	}

	public static class UpdateControlsObj extends AbstractDOMResult
	{
		private final Map<String, AjaxCaptureResult> contents;
		private final Map<String, FullAjaxCaptureResult> updates;
		private final Map<String, List<FullAjaxCaptureResult>> listUpdates;
		private final Map<String, List<String>> controlIds;
		private final Map<String, String> hiddenState;

		public UpdateControlsObj(AbstractDOMResult result, Map<String, AjaxCaptureResult> contents,
			Map<String, FullAjaxCaptureResult> updates, Map<String, List<String>> controlIds,
			Map<String, List<FullAjaxCaptureResult>> listUpdates, Map<String, String> hiddenState)
		{
			super(result);
			this.updates = updates;
			this.contents = contents;
			this.controlIds = controlIds;
			this.listUpdates = listUpdates;
			this.hiddenState = hiddenState;
		}

		public Map<String, AjaxCaptureResult> getContents()
		{
			return contents;
		}

		public Map<String, List<String>> getControlIds()
		{
			return controlIds;
		}

		public Map<String, FullAjaxCaptureResult> getUpdates()
		{
			return updates;
		}

		public Map<String, List<FullAjaxCaptureResult>> getListUpdates()
		{
			return listUpdates;
		}

		public Map<String, String> getHiddenState()
		{
			return hiddenState;
		}
	}

	public static JSCallable getReloadFunction(JSCallable ajaxFunction)
	{
		return new PrependedParameterFunction(PageUpdateCallback.UPDATE_CONTROLS, ajaxFunction);
	}
}