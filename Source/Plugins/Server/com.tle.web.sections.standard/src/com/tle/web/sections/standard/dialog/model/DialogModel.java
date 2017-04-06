/**
 * 
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