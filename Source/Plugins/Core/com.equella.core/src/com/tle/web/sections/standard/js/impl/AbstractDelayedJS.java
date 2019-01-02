/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.standard.js.impl;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererCallback;
import com.tle.web.sections.standard.js.DelayedRenderer;

@NonNullByDefault
public abstract class AbstractDelayedJS<T> implements RendererCallback, DelayedRenderer<T>
{
	protected ElementId id;

	public AbstractDelayedJS(ElementId id)
	{
		this.id = id;
	}

	@Override
	public final void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		info.setAttribute(this, renderer);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public T getSelectedRenderer(RenderContext info)
	{
		return (T) info.getAttribute(this);
	}
}
