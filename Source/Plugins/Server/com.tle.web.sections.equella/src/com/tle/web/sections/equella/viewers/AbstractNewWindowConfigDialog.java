package com.tle.web.sections.equella.viewers;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogControl;
import com.tle.web.sections.standard.js.impl.DisableComponentsFunction;

@SuppressWarnings("nls")
public abstract class AbstractNewWindowConfigDialog extends AbstractResourceViewerConfigDialog
{
	private static PluginResourceHelper urlHelper = ResourcesService
		.getResourceHelper(AbstractNewWindowConfigDialog.class);

	private final boolean allowThickbox;

	@Component
	private Checkbox openInNewWindow;
	@Component
	private Checkbox openInThickbox;
	@Component
	private TextField widthField;
	@Component
	private TextField heightField;

	private DisableComponentsFunction disablerFunc;
	private JSExpression shouldDisable;

	public AbstractNewWindowConfigDialog()
	{
		this(true);
	}

	@Override
	protected abstract Label getTitleLabel(RenderContext context);

	protected AbstractNewWindowConfigDialog(boolean allowThickbox)
	{
		this.allowThickbox = allowThickbox;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		controls.add(new DialogControl(new KeyLabel(urlHelper.key("openinnewwindow")), openInNewWindow));
		if( allowThickbox )
		{
			controls.add(new DialogControl(new KeyLabel(urlHelper.key("openinfancybox")), openInThickbox));
		}
		controls.add(new DialogControl(new KeyLabel(urlHelper.key("windowwidth")), widthField, getWindowSizeHelpTextLabel()));
		controls.add(new DialogControl(new KeyLabel(urlHelper.key("windowheight")), heightField, getWindowSizeHelpTextLabel()));

		mappings.addMapping("openInNewWindow", openInNewWindow);
		if( allowThickbox )
		{
			mappings.addMapping("thickbox", openInThickbox);
		}
		mappings.addMapping("width", widthField);
		mappings.addMapping("height", heightField);

		if( allowThickbox )
		{
			disablerFunc = new DisableComponentsFunction("change" + id, openInThickbox, widthField, heightField);
		}
		else
		{
			disablerFunc = new DisableComponentsFunction("change" + id, widthField, heightField);
		}

		shouldDisable = new NotExpression(openInNewWindow.createGetExpression());
		openInNewWindow.setEventHandler(JSHandler.EVENT_CHANGE, new StatementHandler(disablerFunc, shouldDisable));
		openInNewWindow.setStyleClass("focus");
	}

	protected Label getWindowSizeHelpTextLabel()
	{
		return new KeyLabel(urlHelper.key("help.windowsize"));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		populateFunction.addExtraStatements(new FunctionCallStatement(disablerFunc, shouldDisable));
	}

	public Checkbox getOpenInNewWindow()
	{
		return openInNewWindow;
	}

	public TextField getWidthField()
	{
		return widthField;
	}

	public TextField getHeightField()
	{
		return heightField;
	}
}
