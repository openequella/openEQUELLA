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

package com.tle.web.sections.standard.dialog.model;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.standard.dialog.renderer.DialogRenderer;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class DialogModel extends DialogState
{
	@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
	private boolean showing;
	private boolean modalViewing;
	private DialogRenderer dialogRenderer;
	private JSStatements afterCloseStatements;

	public boolean isShowing()
	{
		return showing;
	}

	public void setShowing(boolean showing)
	{
		this.showing = showing;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends HtmlComponentState> Class<T> getClassForRendering()
	{
		return (Class<T>) DialogState.class;
	}

	public DialogRenderer getDialogRenderer()
	{
		return dialogRenderer;
	}

	public void setDialogRenderer(DialogRenderer dialogRenderer)
	{
		this.dialogRenderer = dialogRenderer;
	}

	public boolean isModalViewing()
	{
		return modalViewing;
	}

	public void setModalViewing(boolean modalViewing)
	{
		this.modalViewing = modalViewing;
	}

	public JSStatements getAfterCloseStatements()
	{
		return afterCloseStatements;
	}

	public void setAfterCloseStatements(JSStatements afterCloseStatements)
	{
		this.afterCloseStatements = afterCloseStatements;
	}
}