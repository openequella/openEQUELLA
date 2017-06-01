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

package com.tle.web.wizard.standard.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.DeclarationStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MutableList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.modules.SelectModule;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.StringListModel;
import com.tle.web.template.DialogTemplate;
import com.tle.web.wizard.WebWizardPage;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CMultiCtrl;
import com.tle.web.wizard.controls.SimpleValueControl;
import com.tle.web.wizard.controls.WebControl;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.render.WizardFreemarkerFactory;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class MultiWebControl extends AbstractWebControl<MultiWebControl.MultiWebControlModel> implements JSDisableable
{
	static
	{
		PluginResourceHandler.init(MultiWebControl.class);
	}

	@PlugURL("js/multi.js")
	private static String multi_url;

	@PlugURL("js/dialog/controls.js")
	private static String controls_url;

	private static Label LABEL_TITLE = new KeyLabel("wizard.controls.multi.controldialog.title");

	private static JSCallable SHUFFLEGROUP_FUNC = new ExternallyDefinedFunction("shufflegroup", new IncludeFile(
		multi_url), new IncludeFile("scripts/utf8.js"), JQueryCore.PRERENDER, SelectModule.INCLUDE);

	@PlugKey("shuffle.mandatory")
	private static Label MANDATORY_ALERT;

	@ViewFactory
	private WizardFreemarkerFactory viewFactory;
	@Inject
	private DialogTemplate template;

	@Component
	private Div div;
	@PlugKey("shuffle.add")
	@Component
	private Link addLink;
	@Component(stateful = false)
	private MutableList<String> list;
	@Component
	private final ControlDialog controlDialog = new ControlDialog();

	protected List<WebControl> webControls;
	protected CMultiCtrl multi;
	@Nullable
	private JSCallable disableFunc;

	private final JSCallable EDIT_FUNC = new ExternallyDefinedFunction("setValues", new IncludeFile(controls_url));

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		list.setParameterId(getFormName());
		list.setListModel(new ListModel());
		list.setStyle("display: none;");

		ObjectExpression multiObj = new ObjectExpression();
		multiObj.put("alertmsg", CurrentLocale.get("wizard.controls.multi.please"));
		multiObj.put("list", new ElementByIdExpression(list));
		multiObj.put("edit", EDIT_FUNC);
		multiObj.put("opendialog", controlDialog.getOpenFunction());
		multiObj.put("controls", getMultiObj());
		multiObj.put("editlabel", CurrentLocale.get("wizard.controls.multi.edit"));
		multiObj.put("dellabel", CurrentLocale.get("wizard.controls.multi.delete"));

		addLink.setClickHandler(new OverrideHandler(controlDialog.getOpenFunction()));
		addLink.setDisablable(true);

		div.setStyleClass("shuffle");
		div.addReadyStatements(new ScriptStatement(PropertyExpression.create(new JQuerySelector(div),
			new FunctionCallExpression(SHUFFLEGROUP_FUNC, multiObj))));

		ScriptVariable params = new ScriptVariable("p");
		JSStatements callTrigger = Js.statement(Jq.methodCall(div, Js.function("trigger"), "addMulti", params));
		controlDialog.setOkCallback(CallAndReferenceFunction.get(Js.function(callTrigger, params), this));
		controlDialog.setInline(true);
		controlDialog.setTemplate(template);
		controlDialog.setStyleClass("controldialog");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		addDisablers(context, this, addLink);
		return viewFactory.createResult("multi.ftl", context);
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		multi = (CMultiCtrl) control;
	}

	@Override
	public void setWebWizardPage(WebWizardPage webWizardPage)
	{
		super.setWebWizardPage(webWizardPage);
		webControls = webWizardPage.wrapControls(((CMultiCtrl) control).getControls());

		for( final WebControl webControl : webControls )
		{
			webControl.setNested(false);
		}
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		Collection<String> vals = list.getValues(info);
		setValues(vals.toArray(new String[vals.size()]));
	}

	public Link getAddLink()
	{
		return addLink;
	}

	public MutableList<String> getList()
	{
		return list;
	}

	public class ListModel extends StringListModel
	{
		@Override
		public List<Option<String>> getOptions(SectionInfo info)
		{
			List<Option<String>> opts = new ArrayList<Option<String>>();
			List<NameValue> nvs = multi.getNamesValues();
			for( NameValue nameValue : nvs )
			{
				opts.add(new NameValueOption<String>(nameValue, nameValue.getValue()));
			}
			return opts;
		}
	}

	public Div getDiv()
	{
		return div;
	}

	@Override
	public final Class<MultiWebControlModel> getModelClass()
	{
		return MultiWebControlModel.class;
	}

	public static class MultiWebControlModel extends WebControlModel
	{
		protected List<SectionResult> renderedControls = new ArrayList<SectionResult>();

		public List<SectionResult> getRenderedControls()
		{
			return renderedControls;
		}

		public void addRenderedControl(SectionResult control)
		{
			this.renderedControls.add(control);
		}
	}

	public ControlDialog getControlDialog()
	{
		return controlDialog;
	}

	private ObjectExpression getMultiObj()
	{
		final ObjectExpression multiObj = new ObjectExpression();
		multiObj.put("separator", multi.getSeparator());
		final List<ObjectExpression> exprs = new ArrayList<ObjectExpression>();
		for( final WebControl webControl : webControls )
		{
			final ObjectExpression contDef = new ObjectExpression();
			final SimpleValueControl svControl = (SimpleValueControl) webControl;
			contDef.put("edit", svControl.createEditFunction());
			contDef.put("text", svControl.createTextFunction());
			contDef.put("value", svControl.createValueFunction());
			contDef.put("reset", svControl.createResetFunction());
			contDef.put("mandatory", svControl.isMandatory());
			exprs.add(contDef);
		}
		multiObj.put("controls", new ArrayExpression(exprs));
		return multiObj;
	}

	public class ControlDialog extends AbstractOkayableDialog<DialogModel>
	{
		private final JSCallable TEXTS_FUNC = new ExternallyDefinedFunction("getTexts", new IncludeFile(controls_url));
		private final JSCallable VALUES_FUNC = new ExternallyDefinedFunction("getValues", new IncludeFile(controls_url));
		private final JSCallable RESET_FUNC = new ExternallyDefinedFunction("resetControls", new IncludeFile(
			controls_url));
		private final JSCallable VALIDATE_FUNC = new ExternallyDefinedFunction("validateControls", new IncludeFile(
			controls_url));

		@Override
		protected Label getTitleLabel(RenderContext context)
		{
			return LABEL_TITLE;
		}

		@Override
		protected SectionRenderable getRenderableContents(RenderContext context)
		{
			MultiWebControlModel model = MultiWebControl.this.getModel(context);
			List<SectionResult> renderedControls = model.getRenderedControls();
			for( WebControl control : webControls )
			{
				if( control.isViewable() )
				{
					renderedControls.add(SectionUtils.renderSection(context, control));
				}
			}

			return viewFactory.createNormalResult("controldialog.ftl", MultiWebControl.this);
		}

		@Override
		protected int getNumberOfOpenParameters()
		{
			return 1;
		}

		@Override
		protected JSHandler createOkHandler(SectionTree tree)
		{
			JSStatements alert = Js.alert_s(MANDATORY_ALERT);

			return new OverrideHandler(Js.call_s(getCloseFunction()), createOkCallStatement(tree), Js.call_s(
				RESET_FUNC, getMultiObj())).addValidator(Js.validator(VALIDATE_FUNC, getMultiObj())
				.setFailureStatements(alert));
		}

		@Override
		protected JSStatements createOkCallStatement(SectionTree tree)
		{
			final ObjectExpression multiObj = getMultiObj();
			return Js.call_s(getOkCallback(),
				new ArrayExpression(Js.call(VALUES_FUNC, multiObj), Js.call(TEXTS_FUNC, multiObj)));
		}

		@Override
		protected JSHandler createCancelHandler(SectionTree tree)
		{
			final OkayableDialogCancelStatements cancelStatements = createCancelCallStatement(tree);

			return new OverrideHandler(cancelStatements.getStatements(), Js.call_s(getCloseFunction()), Js.call_s(
				RESET_FUNC, getMultiObj()));
		}

		@Override
		public String getWidth()
		{
			return "717px"; //$NON-NLS-1$
		}

		protected void setTemplate(DialogTemplate template)
		{
			this.template = template;
		}

		@Override
		public DialogModel instantiateDialogModel(SectionInfo info)
		{
			return new DialogModel();
		}
	}

	public Button getOk()
	{
		return controlDialog.getOk();
	}

	@Override
	public JSCallable createDisableFunction()
	{
		if( disableFunc == null )
		{
			ScriptVariable dis = new ScriptVariable("dis");
			ScriptVariable elem = new ScriptVariable("elem");
			disableFunc = new SimpleFunction("dis", this, StatementBlock.get(new DeclarationStatement(elem, div),
				new ScriptStatement("if(dis){$(elem).addClass('disabled');}else{$(elem).removeClass('disabled');}")),
				dis);
		}

		return disableFunc;
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}