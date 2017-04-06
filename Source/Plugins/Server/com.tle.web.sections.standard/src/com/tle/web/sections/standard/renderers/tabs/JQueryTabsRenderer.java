package com.tle.web.sections.standard.renderers.tabs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryTabs;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.ArrayIndexExpression;
import com.tle.web.sections.js.generic.expression.ElementValueExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.standard.model.HtmlTabState;
import com.tle.web.sections.standard.model.TabContent;
import com.tle.web.sections.standard.model.TabModel;
import com.tle.web.sections.standard.renderers.AbstractComponentRenderer;

public class JQueryTabsRenderer extends AbstractComponentRenderer
{
	// private static PluginResourceHelper urlHelper =
	// ResourcesService.getResourceHelper(JQueryTabsRenderer.class);
	private final HtmlTabState tabState;
	private List<TabContent> visibleTabs;
	private final ElementId visibleId;

	public JQueryTabsRenderer(HtmlTabState state)
	{
		super(state);
		this.tabState = state;
		visibleId = new AppendedElementId(this, "div"); //$NON-NLS-1$
	}

	@Override
	protected String getTag()
	{
		return "div"; //$NON-NLS-1$
	}

	@Override
	public ElementId getVisibleElementId()
	{
		return visibleId;
	}

	@SuppressWarnings("nls")
	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		writer.render(new HiddenInput(this, state.getName(), tabState.getCurrentTab()));
		writer.writeTag("ul");
		List<TabContent> tabs = getVisibleTabs(writer);
		for( TabContent option : tabs )
		{
			writer.writeTag("li");
			writer.writeTag("a", "href", "#" + getIdForTab(writer, option));
			writer.writeTag("span");
			writer.writeText(option.getName());
			writer.endTag("span");
			writer.endTag("a");
			writer.endTag("li");
		}
		writer.endTag("ul");
		for( TabContent tab : tabs )
		{
			writer.writeTag("div", "id", getIdForTab(writer, tab));
			if( !tabState.isRenderSelectedOnly() || tab.getValue().equals(tabState.getCurrentTab()) )
			{
				writer.render(tab.getRenderer());
			}
			writer.endTag("div");
		}
	}

	private List<TabContent> getVisibleTabs(SectionInfo info)
	{
		if( visibleTabs == null )
		{
			visibleTabs = tabState.getTabModel().getVisibleTabs(info);
		}
		return visibleTabs;
	}

	private String getIdForTab(SectionInfo info, TabContent option)
	{
		return getElementId(info) + "_" + option.getValue(); //$NON-NLS-1$
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( !event.equals(JSHandler.EVENT_CHANGE) )
		{
			super.processHandler(writer, attrs, event, handler);
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);

		info.preRender(JQueryTabs.PRERENDER);
		List<JSExpression> tabNames = new ArrayList<JSExpression>();
		TabModel tabModel = tabState.getTabModel();
		List<TabContent> tabs = tabModel.getVisibleTabs(info);
		for( TabContent tab : tabs )
		{
			tabNames.add(new StringExpression(tab.getValue()));
		}
		ArrayExpression tabNamesArray = new ArrayExpression(tabNames);
		ScriptVariable ui = Js.var("ui");

		JSStatements selectBody = new AssignStatement(new ElementValueExpression(this), ArrayIndexExpression.create(
			tabNamesArray, PropertyExpression.create(ui, "newTab.index()")));
		JSHandler changeHandler = tabState.getHandler(JSHandler.EVENT_CHANGE);
		boolean doNormal = true;
		if( changeHandler != null )
		{
			selectBody = StatementBlock.get(selectBody, changeHandler);
			doNormal = false;
		}
		selectBody = StatementBlock.get(selectBody, new ReturnStatement(doNormal));
		AnonymousFunction selectFunc = new AnonymousFunction(selectBody, Js.var("event"), ui);

		String currentTab = tabState.getCurrentTab();
		ObjectExpression tabsOptions = new ObjectExpression();
		tabsOptions.put("activate", selectFunc);
		tabsOptions.put("active", tabModel.getIndexForTab(info, currentTab));
		// add other events other than click and change:
		for( String event : tabState.getEventKeys() )
		{
			if( !event.equals("click") && !event.equals("change") )
			{
				tabsOptions.put(event, Js.function(tabState.getHandler(event)));
			}
		}

		state.addReadyStatements(Js.statement(Jq.methodCall(getVisibleElementId(), Js.function("tabs"), tabsOptions)));
	}
}
