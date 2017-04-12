package com.tle.web.viewitem.flvviewer;

import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.viewers.AbstractNewWindowConfigDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.IfStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogControl;

public class FLVViewerConfigDialog extends AbstractNewWindowConfigDialog
{
	@PlugKey("width")
	private static Label WIDTH_LABEL;
	@PlugKey("height")
	private static Label HEIGHT_LABEL;
	@PlugKey("name")
	private static Label LABEL_TITLE;

	@Component
	private TextField width;
	@Component
	private TextField height;

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		controls.add(new DialogControl(WIDTH_LABEL, width));
		controls.add(new DialogControl(HEIGHT_LABEL, height));
		mappings.addMapMapping("attr", "flvWidth", width);
		mappings.addMapMapping("attr", "flvHeight", height);

	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@SuppressWarnings("nls")
	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		StatementBlock statementBlock = new StatementBlock();

		statementBlock.addStatements(new IfStatement(new NotExpression(new ScriptVariable("obj['attr']['flvWidth']")),
			new FunctionCallStatement(width.createSetFunction(), "640")));
		statementBlock.addStatements(new IfStatement(new NotExpression(new ScriptVariable("obj['attr']['flvHeight']")),
			new FunctionCallStatement(height.createSetFunction(), "480")));

		populateFunction.addExtraStatements(statementBlock);
	}
}
