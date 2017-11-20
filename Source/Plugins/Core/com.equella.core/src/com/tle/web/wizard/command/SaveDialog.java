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

package com.tle.web.wizard.command;

import java.util.Collection;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.wizard.WebWizardService;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.section.WizardBodySection;
import com.tle.web.wizard.section.WizardSectionInfo;
import com.tle.web.workflow.tasks.ModerationService;

@SuppressWarnings("nls")
@NonNullByDefault
public class SaveDialog extends EquellaDialog<SaveDialog.SaveDialogModel>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@PlugKey("command.save.title")
	private static Label LABEL_TITLE;
	@PlugKey("command.save.unfinishedmsg")
	private static Label LABEL_UNFINISHEDMSG;

	@PlugKey("command.save.publishmsg")
	private static Label LABEL_PUBLISHMSG;
	@PlugKey("command.save.publishnoworkflowmsg")
	private static Label LABEL_PUBLISH_NOMOD_MSG;

	@PlugKey("command.save.successreceipt")
	private static Label SUCCESS_RECEIPT_LABEL;

	@Inject
	private ModerationService moderationService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private WizardService wizardService;
	@Inject
	private WebWizardService webWizardService;
	@Inject
	private ItemOperationFactory workflowFactory;

	@Component(stateful = false, name = "m")
	private TextField message;
	@TreeLookup
	private WizardBodySection wizardBodySection;

	@Component
	@PlugKey("command.save.savedraft")
	private Button draft;
	@Component
	@PlugKey("command.save.cancel")
	private Button cancel;
	@Component
	@PlugKey("command.save.complete")
	private Button complete;
	@Component
	@PlugKey("command.save.submit")
	private Button submit;
	@Component
	@PlugKey("command.save.submitnoworkflow")
	private Button publish;

	public SaveDialog()
	{
		setAjax(true);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		final JSCallable commandExec = addParentCallable(events.getSubmitValuesFunction("save"));
		final ScriptVariable type = new ScriptVariable("type");
		final ScriptVariable msg = new ScriptVariable("msg");
		final SimpleFunction execFunc = new SimpleFunction("exec", this,
			StatementBlock.get(Js.call_s(commandExec, type, msg), Js.call_s(getCloseFunction())), type, msg);

		draft.setClickHandler(new OverrideHandler(execFunc, "draft", ""));
		cancel.setClickHandler(new OverrideHandler(getCloseFunction()));
		complete.setClickHandler(new OverrideHandler(execFunc, "check", ""));
		submit.setClickHandler(new OverrideHandler(execFunc, "submit", message.createGetExpression()));
		publish.setClickHandler(new OverrideHandler(execFunc, "submit", ""));

		submit.setComponentAttribute(ButtonType.class, ButtonType.SAVE);
		publish.setComponentAttribute(ButtonType.class, ButtonType.SAVE);

		super.treeFinished(id, tree);
	}

	@Override
	public String getWidth()
	{
		return "500px";
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public SaveDialogModel instantiateDialogModel(@Nullable SectionInfo info)
	{
		return new SaveDialogModel();
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		final Collection<Button> buttons = Lists.newArrayList();
		final Label prompt;

		final SaveDialogModel model = getModel(context);
		if( !wizardBodySection.isSaveableApartFromCurrent(context) )
		{
			prompt = LABEL_UNFINISHEDMSG;
			buttons.add(complete);
		}
		else
		{
			WizardState state = context.getAttributeForClass(WizardSectionInfo.class).getWizardState();
			if( state.getItemDefinition().getWorkflow() != null )
			{
				prompt = LABEL_PUBLISHMSG;
				buttons.add(submit);
				model.setShowMessage(true);
			}
			else
			{
				prompt = LABEL_PUBLISH_NOMOD_MSG;
				buttons.add(publish);
			}
		}
		buttons.add(draft);
		buttons.add(cancel);

		model.setPrompt(prompt);
		model.setActions(buttons);

		return viewFactory.createResult("wizard/savedialog.ftl", this);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		return getModel(context).getActions();
	}

	@EventHandlerMethod
	public void save(SectionInfo info, String type, String message)
	{
		WizardSectionInfo winfo = info.getAttributeForClass(WizardSectionInfo.class);
		WizardState state = winfo.getWizardState();
		WorkflowOperation[] ops = new WorkflowOperation[]{};
		boolean doSubmit = type.equals("submit");
		boolean doCheck = type.equals("check");

		if( doSubmit || !state.isInDraft() || doCheck )
		{
			if( !wizardBodySection.isSaveable(info) && wizardBodySection.goToFirstUnfinished(info) )
			{
				return;
			}
		}
		if( doCheck )
		{
			return;
		}
		boolean stayInWizard = !state.isEntryThroughEdit() && !state.isNewItem();
		if( doSubmit )
		{
			ops = new WorkflowOperation[]{workflowFactory.submit(message)};
			stayInWizard = false;
		}

		wizardService.doSave(state, true, ops);

		if( !stayInWizard )
		{
			webWizardService.forwardToViewItem(info, state);
			receiptService.setReceipt(SUCCESS_RECEIPT_LABEL);
		}
		else
		{
			wizardService.reload(state, false);
			moderationService.setEditing(info, false);
		}
	}

	public static class SaveDialogModel extends DialogModel
	{
		private Label prompt;
		private boolean showMessage;
		private Collection<Button> actions;

		public Label getPrompt()
		{
			return prompt;
		}

		public void setPrompt(Label prompt)
		{
			this.prompt = prompt;
		}

		public boolean isShowMessage()
		{
			return showMessage;
		}

		public void setShowMessage(boolean showMessage)
		{
			this.showMessage = showMessage;
		}

		public Collection<Button> getActions()
		{
			return actions;
		}

		public void setActions(Collection<Button> actions)
		{
			this.actions = actions;
		}
	}

	public TextField getMessage()
	{
		return message;
	}
}
