/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.standard;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.impl.DelayedJSDisabler;
import com.tle.web.sections.standard.model.HtmlComponentState;

@NonNullByDefault
public abstract class AbstractDisablerComponent<S extends HtmlComponentState> extends AbstractRenderedComponent<S>
	implements
		JSDisableable
{
	private DelayedJSDisabler delayedDisabler;

	public AbstractDisablerComponent(String defaultRenderer)
	{
		super(defaultRenderer);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		delayedDisabler = new DelayedJSDisabler(this);
	}

	@Override
	public void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		delayedDisabler.rendererSelected(info, renderer);
		super.rendererSelected(info, renderer);
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return delayedDisabler.createDisableFunction();
	}
}
