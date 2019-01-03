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

package com.tle.web.sections.equella.dialog;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

/**
 * A dialog containing OK and CANCEL buttons.
 * 
 * @author aholland
 */
@NonNullByDefault
public abstract class AbstractOkayableDialog<S extends DialogModel> extends EquellaDialog<S>
{
	protected static final ButtonType OK_BUTTON_TYPE = ButtonType.SAVE;

	@PlugKey("okayabledialog.ok")
	private static Label OK_LABEL;

	@Component
	private Button ok;

	private Label okLabel;

	private JSHandler okHandler;
	private JSCallable okCallback;
	private JSHandler cancelHandler;
	private JSCallable cancelCallback;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		ok.setLabel(getOkLabel());
		ok.setComponentAttribute(ButtonType.class, OK_BUTTON_TYPE);
	}

	@SuppressWarnings("nls")
	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		if( okHandler == null )
		{
			if( okCallback == null )
			{
				throw new Error("Must set an ok button callback on " + getClass());
			}
			okCallback = addParentCallable(okCallback);
			okHandler = createOkHandler(tree);
		}
		if( okHandler != null )
		{
			ok.setClickHandler(okHandler);
		}

		if( cancelHandler == null )
		{
			if( cancelCallback == null )
			{
				cancelCallback = getCloseFunction();
			}
			else
			{
				cancelCallback = addParentCallable(cancelCallback);
			}
			cancelHandler = createCancelHandler(tree);
		}
		super.treeFinished(id, tree);
	}

	protected JSHandler createOkHandler(SectionTree tree)
	{
		return new OverrideHandler(createOkCallStatement(tree), new FunctionCallStatement(getCloseFunction()));
	}

	protected JSStatements createOkCallStatement(SectionTree tree)
	{
		return new FunctionCallStatement(getOkCallback());
	}

	protected JSHandler createCancelHandler(SectionTree tree)
	{
		final OkayableDialogCancelStatements cancelStatements = createCancelCallStatement(tree);

		if( cancelStatements.isClosesDialog() )
		{
			return new OverrideHandler(cancelStatements.getStatements());
		}

		return new OverrideHandler(cancelStatements.getStatements(), new FunctionCallStatement(getCloseFunction()));
	}

	protected OkayableDialogCancelStatements createCancelCallStatement(SectionTree tree)
	{
		return new OkayableDialogCancelStatements(true, new FunctionCallStatement(cancelCallback));
	}

	@Override
	protected JSHandler getTemplateCloseFunction()
	{
		return cancelHandler;
	}

	protected Label getOkLabel()
	{
		if( okLabel != null )
		{
			return okLabel;
		}
		return OK_LABEL;
	}

	public void setOkCallback(JSCallable okCallback)
	{
		this.okCallback = okCallback;
	}

	protected JSCallable getOkCallback()
	{
		return okCallback;
	}

	public JSCallable getcancelCallback()
	{
		return cancelCallback;
	}

	public void setCancelCallback(JSCallable cancelCallback)
	{
		this.cancelCallback = cancelCallback;
	}

	public void setOkLabel(Label okLabel)
	{
		this.okLabel = okLabel;
	}

	public Button getOk()
	{
		return ok;
	}

	public void setOkHandler(JSHandler okHandler)
	{
		this.okHandler = okHandler;
	}

	public void setCancelHandler(JSHandler cancelHandler)
	{
		this.cancelHandler = cancelHandler;
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		return Lists.newArrayList(ok);
	}

	/**
	 * In at least one specific case, we require a subclass (AddQuotaDialog) to
	 * have a contained section loaded via an ajax div's readyStatements rather
	 * than via a callback. Accordingly that subclass will override this method
	 * and return true.
	 * 
	 * @return false by default
	 */
	public boolean loadViaReadyStatements()
	{
		return false;
	}

	public static class OkayableDialogCancelStatements
	{
		private final boolean closesDialog;
		private final JSStatements statements;

		public OkayableDialogCancelStatements(boolean closesDialog, JSStatements statements)
		{
			this.closesDialog = closesDialog;
			this.statements = statements;
		}

		public boolean isClosesDialog()
		{
			return closesDialog;
		}

		public JSStatements getStatements()
		{
			return statements;
		}
	}
}