package com.tle.web.pss.viewer;

import com.tle.core.pss.util.PSSConstants;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.viewers.AbstractNewWindowConfigDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.DummyRenderContext;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.IfStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogControl;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;

public class PearsonScormServicesViewerConfigDialog extends AbstractNewWindowConfigDialog
{
	@PlugKey("viewer.configdialog.windowsize.help")
	private static Label HELP_LABEL;
	@PlugKey("viewer.configdialog.title")
	private static Label LABEL_TITLE;
	@PlugKey("viewer.configdialog.uifeatures")
	private static Label UIFEATURES_LABEL;
	@PlugKey("viewer.configdialog.uifeatures.help")
	private static String UIFEATURES_HELP;
	@PlugKey("viewer.configdialog.uifeatures.link")
	private static Label UIFEATURES_LINK;
	@Component
	private TextField uiFeatures;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		SimpleBookmark bookmark = new SimpleBookmark("http://developer.pearson.com/apis/launch-player#custom_params");
		HtmlLinkState htmlLinkState = new HtmlLinkState(UIFEATURES_LINK, bookmark);
		htmlLinkState.setTarget("_blank");
		LinkRenderer linkRenderer = new LinkRenderer(htmlLinkState);

		controls.add(new DialogControl(UIFEATURES_LABEL, uiFeatures, new KeyLabel(UIFEATURES_HELP, SectionUtils
			.renderToString(new DummyRenderContext(), linkRenderer))));
		mappings.addMapMapping("attr", PSSConstants.VIEWER_UI_FEATURES, uiFeatures);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{

		super.treeFinished(id, tree);
		StatementBlock statementBlock = new StatementBlock();

		statementBlock.addStatements(new IfStatement(
			new NotExpression(new ScriptVariable("obj['attr']['uiFeatures']")), new FunctionCallStatement(uiFeatures
				.createSetFunction(), PSSConstants.DEFAULT_UI_FEATURES)));

		populateFunction.addExtraStatements(statementBlock);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	protected Label getWindowSizeHelpTextLabel()
	{
		return HELP_LABEL;
	}

}
