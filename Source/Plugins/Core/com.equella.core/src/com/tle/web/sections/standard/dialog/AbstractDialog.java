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

package com.tle.web.sections.standard.dialog;

import java.util.ArrayList;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.InnerBodyEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.ReadyToRespondListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.generic.DummyRenderContext;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.MutableHeaderHelper;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.PageUniqueId;
import com.tle.web.sections.js.generic.expression.CurrentForm;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.ParentFrameFunction;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.statement.ExecuteReady;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.AbstractRenderedComponent;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.dialog.renderer.DialogRenderer;
import com.tle.web.sections.standard.js.impl.DelayedFunction;
import com.tle.web.sections.standard.renderers.fancybox.FancyBoxDialogRenderer;

/**
 * A component superclass for modal dialogs.
 * <p>
 * A dialog has the following properties:
 * <ul>
 * <li>Contents</li>
 * <li>An open and close javascript function</li>
 * <li>An optional "opener" link/button</li>
 * </ul>
 * It can be rendered as an inline DIV, ajax loaded inline DIV and IFrame
 * <p>
 * Details such as width and height of the dialog is stored here, but the
 * renderer can override those sizes.
 * 
 * @see DialogRenderer
 * @see FancyBoxDialogRenderer
 * @author jmaginnis
 * @param <S> The Model type
 */
@NonNullByDefault
public abstract class AbstractDialog<S extends DialogModel> extends AbstractRenderedComponent<S>
	implements
		Dialog,
		ReadyToRespondListener
{
	private final List<JSCallAndReference> parentCalls = new ArrayList<JSCallAndReference>();

	@Nullable
	private JSCallable openFunction;
	@Nullable
	private JSCallable closeFunction;
	@Nullable
	private DialogRenderer dialogRenderer;
	private SectionId displayDialog = this;
	private boolean dynamicRenderer;
	private boolean inline;
	private boolean ajax;
	private boolean modal = true;
	private boolean canAddParentCallbacks;

	/**
	 * Function called when dialog is fully opened
	 */
	private JSFunction dialogOpenedCallback;

	/**
	 * Function called when dialog is fully closed
	 */
	private JSFunction dialogClosedCallback;

	@AjaxFactory
	protected AjaxGenerator ajaxEvents;
	@EventFactory
	protected EventGenerator events;

	@Component
	private DialogLink opener;
	private JSBookmarkModifier showContents;

	protected AbstractDialog()
	{
		super(RendererConstants.DIALOG);
	}

	@Override
	public boolean isTreeIndexed()
	{
		return true;
	}

	public Link getOpener()
	{
		return opener;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		S model = instantiateDialogModel(info);
		if( info.isReal() )
		{
			setupState(info, model);
		}
		return model;
	}

	@Override
	protected S setupState(SectionInfo info, S state)
	{
		super.setupState(info, state);
		state.setHeight(getHeight());
		state.setWidth(getWidth());
		state.setModal(modal);
		state.setInline(inline);
		state.setAjax(ajax);
		state.setDialogOpenedCallback(dialogOpenedCallback);
		state.setDialogClosedCallback(dialogClosedCallback);
		if( !ajax )
		{
			state.setContentsUrl(new BookmarkAndModify(info, showContents));
		}
		else
		{
			state.setOpenModifier(showContents);
		}
		return state;
	}

	public abstract S instantiateDialogModel(SectionInfo info);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		canAddParentCallbacks = true;
		opener.setDialog(this);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		if( ajax )
		{
			InnerBodyEvent.ensureRegistered(tree);
			showContents = ajaxEvents.getUpdateDomModifier(tree, this, AjaxGenerator.AJAXID_BODY, getAjaxShowEvent());
		}
		else
		{
			showContents = events.getNamedModifier("show"); //$NON-NLS-1$
		}

		super.treeFinished(id, tree);

		canAddParentCallbacks = false;
	}

	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("show"); //$NON-NLS-1$
	}

	@EventHandlerMethod(name = "show")
	public void showDialog(SectionInfo context)
	{
		getState(context).setShowing(true);
	}

	public void closeDialog(SectionInfo info, JSCallable callable, Object... args)
	{
		closeDialog(info, new FunctionCallStatement(callable, args));
	}

	public void closeDialog(SectionInfo info, JSStatements... statements)
	{
		getModel(info).setAfterCloseStatements(
			StatementBlock.get(new FunctionCallStatement(getCloseFunction()), StatementBlock.get(statements)));
	}

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		S model = getModel(context);
		if( model.isModalViewing() )
		{
			model.setModalViewing(false);
			setupModal(context);
			return getSelectedRenderer(context);
		}
		return super.renderHtml(context);
	}

	protected void setupModal(RenderEventContext context)
	{
		// nothing
	}

	@Override
	protected void prepareModel(RenderContext info)
	{
		super.prepareModel(info);
		S state = getState(info);
		boolean showing = state.isShowing();
		if( !inline && !showing )
		{
			for( JSCallAndReference parentFuncs : parentCalls )
			{
				info.getPreRenderContext().preRender(parentFuncs);
			}
		}
		if( showing || inline )
		{
			JSStatements afterClose = state.getAfterCloseStatements();
			if( afterClose != null )
			{
				state.setContents(new ExecuteReady(afterClose));
			}
			else
			{
				state.setContents(getDialogContents(info));
			}
		}
	}

	@Nullable
	protected SectionRenderable getDialogContents(RenderContext context)
	{
		return getRenderableContents(context);
	}

	@Nullable
	protected abstract SectionRenderable getRenderableContents(RenderContext context);

	public String getWidth()
	{
		return "auto"; //$NON-NLS-1$
	}

	public String getHeight()
	{
		return "auto"; //$NON-NLS-1$
	}

	@Override
	public void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		DialogRenderer diagRend = (DialogRenderer) renderer;
		if( !dynamicRenderer && dialogRenderer == null )
		{
			dialogRenderer = diagRend;
		}
		getModel(info).setDialogRenderer(diagRend);
	}

	protected JSCallable addParentCallable(JSCallable callable)
	{
		if( !canAddParentCallbacks )
		{
			throw new SectionsRuntimeException("Should add callbacks in treeFinished()"); //$NON-NLS-1$
		}

		if( inline )
		{
			return callable;
		}

		JSCallAndReference parentCall = CallAndReferenceFunction.get(callable, this);
		parentCalls.add(parentCall);
		if( ajax )
		{
			return new ExternallyDefinedFunction(parentCall.getExpression(new DummyRenderContext()));
		}
		return new ParentFrameFunction(parentCall);
	}

	@Override
	public JSCallable getOpenFunction()
	{
		if( openFunction == null )
		{
			openFunction = new DelayedFunction<DialogRenderer>(this, "open", this, getNumberOfOpenParameters()) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, DialogRenderer renderer)
				{
					return renderer.createOpenFunction();
				}
			};
		}
		return openFunction;
	}

	protected int getNumberOfOpenParameters()
	{
		return 0;
	}

	@Override
	public JSCallable getCloseFunction()
	{
		if( closeFunction == null )
		{
			closeFunction = new DelayedFunction<DialogRenderer>(this, "close", this, 1) //$NON-NLS-1$
			{
				@Override
				protected JSCallable createRealFunction(RenderContext info, DialogRenderer renderer)
				{
					return renderer.createCloseFunction();
				}
			};
		}
		return closeFunction;
	}

	@Override
	protected SectionRenderable chooseRenderer(RenderContext info, S state)
	{
		DialogRenderer renderer;
		S model = getModel(info);
		if( dialogRenderer != null )
		{
			renderer = dialogRenderer.createNewRenderer(state);
			state.fireRendererCallback(info, renderer);
		}
		else
		{
			renderer = (DialogRenderer) super.chooseRenderer(info, state);
			if( !dynamicRenderer )
			{
				dialogRenderer = renderer;
			}
		}
		model.setDialogRenderer(renderer);
		return renderer;
	}

	@Override
	@SuppressWarnings("nls")
	public DialogRenderer getSelectedRenderer(RenderContext info)
	{
		S model = getModel(info);
		DialogRenderer diagRenderer = model.getDialogRenderer();
		if( diagRenderer != null )
		{
			return diagRenderer;
		}
		SectionResult result = SectionUtils.renderSectionResult(info, displayDialog);
		if( displayDialog != this )
		{
			SectionUtils.renderToString(info, (SectionRenderable) result);
		}
		diagRenderer = model.getDialogRenderer();
		if( diagRenderer == null )
		{
			throw new SectionsRuntimeException("Section ID:" + displayDialog.getSectionId()
				+ " didn't render the dialog");
		}
		return diagRenderer;
	}

	@Override
	public void readyToRespond(SectionInfo info, boolean redirect)
	{
		if( !redirect )
		{
			S model = getModel(info);
			if( model.isShowing() )
			{
				info.getRootRenderContext().setModalId(getSectionId());
				model.setModalViewing(true);
				if( ajax )
				{
					RenderContext context = info.getRootRenderContext();
					MutableHeaderHelper helper = (MutableHeaderHelper) context.getHelper();
					FormTag form = context.getForm();
					form.setElementId(new AppendedElementId(this, "_f")); //$NON-NLS-1$
					helper.setFormExpression(new ElementByIdExpression(form));
					info.setAttribute(PageUniqueId.class, getElementId(info));
					PrependedParameterFunction submit = new PrependedParameterFunction(AjaxGenerator.SUBMIT_BODY,
						CurrentForm.EXPR, true, true);
					PrependedParameterFunction submitNoBlock = new PrependedParameterFunction(
						AjaxGenerator.SUBMIT_BODY, CurrentForm.EXPR, false, false);
					PrependedParameterFunction submitEvent = new PrependedParameterFunction(AjaxGenerator.SUBMIT_BODY,
						CurrentForm.EXPR, true, true);
					PrependedParameterFunction submitNoValidation = new PrependedParameterFunction(
						AjaxGenerator.SUBMIT_BODY, CurrentForm.EXPR, false, true);
					PrependedParameterFunction submitEventNoValidation = new PrependedParameterFunction(
						AjaxGenerator.SUBMIT_BODY, CurrentForm.EXPR, false, true);
					PrependedParameterFunction submitEventNoBlock = new PrependedParameterFunction(
						AjaxGenerator.SUBMIT_BODY, CurrentForm.EXPR, false, false);

					helper.setSubmitFunctions(submit, submitNoValidation, submitNoBlock, submitEvent,
						submitEventNoValidation, submitEventNoBlock);
				}
			}
		}
	}

	public void setModal(boolean modal)
	{
		this.modal = modal;
	}

	public static class DialogLink extends Link
	{
		private Dialog dialog;

		@Override
		protected void prepareModel(RenderContext info)
		{
			super.prepareModel(info);
			dialog.getSelectedRenderer(info).setupOpener(getState(info));
		}

		public void setDialog(Dialog dialog)
		{
			this.dialog = dialog;
		}
	}

	public void setDisplayDialog(SectionId displayDialog)
	{
		this.displayDialog = displayDialog;
		dynamicRenderer = true;
	}

	public boolean isInline()
	{
		return inline;
	}

	public void setInline(boolean inline)
	{
		this.inline = inline;
	}

	public boolean isAjax()
	{
		return ajax;
	}

	public void setAjax(boolean ajax)
	{
		this.ajax = ajax;
	}

	public boolean isModal()
	{
		return modal;
	}

	public void setDialogOpenedCallback(JSFunction dialogOpenedCallback)
	{
		this.dialogOpenedCallback = dialogOpenedCallback;
	}

	public void setDialogClosedCallback(JSFunction dialogClosedCallback)
	{
		this.dialogClosedCallback = dialogClosedCallback;
	}
}
