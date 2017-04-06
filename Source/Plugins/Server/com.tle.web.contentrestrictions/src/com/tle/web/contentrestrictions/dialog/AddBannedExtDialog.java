package com.tle.web.contentrestrictions.dialog;

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.FunctionCallValidator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class AddBannedExtDialog extends AbstractOkayableDialog<DialogModel>
{
	private static final IncludeFile INCLUDE = new IncludeFile(ResourcesService.getResourceHelper(
		AddBannedExtDialog.class).url("scripts/dialog/addbannedext.js"));
	private static final ExternallyDefinedFunction CHECK_EXTENSION_FUNCTION = new ExternallyDefinedFunction(
		"checkExtension", INCLUDE);

	@PlugKey("addbannedext.dialog.title")
	private static Label LABEL_TITLE;
	@PlugKey("addbannedext.dialog.error.validation.invalidextension")
	private static Label LABEL_INVALID_EXTENSION;

	@Component(stateful = false)
	private TextField bannedExtText;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("dialog/addbannedext.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return new OverrideHandler(jscall(getOkCallback(), bannedExtText.createGetExpression()),
			jscall(getCloseFunction())).addValidator(new FunctionCallValidator(CHECK_EXTENSION_FUNCTION, Jq
			.$(bannedExtText)).setFailureStatements(Js.alert_s(LABEL_INVALID_EXTENSION)));
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "bed";
	}

	public TextField getBannedExtText()
	{
		return bannedExtText;
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public DialogModel instantiateDialogModel(SectionInfo info)
	{
		return new DialogModel();
	}
}
