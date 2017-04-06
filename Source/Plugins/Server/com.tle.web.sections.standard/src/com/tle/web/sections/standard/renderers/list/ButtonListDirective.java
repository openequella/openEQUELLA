package com.tle.web.sections.standard.renderers.list;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ElementValueExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.HtmlMutableListState;
import com.tle.web.sections.standard.model.Option;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BooleanModel;
import freemarker.ext.beans.NumberModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class ButtonListDirective extends AbstractListDirective
{
	@Override
	@SuppressWarnings("nls")
	protected TagRenderer createTagRenderer(HtmlMutableListState state, Environment env, Map<?, ?> params,
		TemplateDirectiveBody body, TemplateModel[] loopVars) throws TemplateModelException
	{
		String tag = "div";
		if( params.containsKey("tag") )
		{
			tag = ((SimpleScalar) params.get("tag")).getAsString();
		}

		boolean hideDisabledOptions = false;
		if( params.containsKey("hideDisabledOptions") )
		{
			hideDisabledOptions = ((TemplateBooleanModel) params.get("hideDisabledOptions")).getAsBoolean();
		}

		return new ButtonListTagRenderer(tag, (HtmlListState) state, env, body, loopVars, hideDisabledOptions);
	}

	public static class ButtonListTagRenderer extends TagRenderer
	{
		private final HtmlListState listState;
		private final TemplateDirectiveBody body;
		private final TemplateModel[] loopVars;
		private final BeansWrapper wrapper;
		private final AppendedElementId hiddenId;
		private final SimpleFunction clickFunc;
		private final boolean hideDisabledOptions;

		@SuppressWarnings("nls")
		public ButtonListTagRenderer(String tag, HtmlListState state, Environment env, TemplateDirectiveBody body,
			TemplateModel[] loopVars, boolean hideDisabledOptions)
		{
			super(tag, state); //$NON-NLS-1$
			this.wrapper = (BeansWrapper) env.getObjectWrapper();
			this.listState = state;
			this.body = body;
			this.loopVars = loopVars;
			this.hideDisabledOptions = hideDisabledOptions;

			this.hiddenId = new AppendedElementId(state, "_hid");
			ScriptVariable valVar = new ScriptVariable("val");
			JSStatements changeBody = new AssignStatement(new ElementValueExpression(hiddenId), valVar);

			JSHandler lsChangeHandler = state.getHandler(JSHandler.EVENT_CHANGE);
			if( lsChangeHandler != null )
			{
				changeBody = StatementBlock.get(changeBody, lsChangeHandler);
			}
			clickFunc = new SimpleFunction(JSHandler.EVENT_CHANGE, state, changeBody, valVar);
		}

		@Override
		protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException
		{
			super.writeStart(writer, attrs);
			writer.render(new HiddenInput(hiddenId, listState.getName(), getSelectedValue()));
		}

		private String getSelectedValue()
		{
			Iterator<String> iter = listState.getSelectedValues().iterator();
			if( !iter.hasNext() )
			{
				return ""; //$NON-NLS-1$
			}
			return iter.next();
		}

		@Override
		protected void writeMiddle(SectionWriter writer) throws IOException
		{
			Collection<Option<?>> options = listState.getOptions();
			if( hideDisabledOptions )
			{
				options = Collections2.filter(options, new Predicate<Option<?>>()
				{
					@Override
					public boolean apply(Option<?> option)
					{
						return !option.isDisabled();
					}
				});
			}

			int i = 0;
			String selected = getSelectedValue();
			int lastIndex = options.size() - 1;
			for( Option<?> option : options )
			{
				TextLabel label = new TextLabel(option.getName(), option.isNameHtml());

				HtmlComponentState state = new HtmlComponentState();
				state.setLabel(label);
				state.setDisabled(option.isDisabled());
				String value = option.getValue();
				state.setClickHandler(new OverrideHandler(clickFunc, value));
				loopVars[0] = new BeanModel(option, wrapper);
				loopVars[1] = new BeanModel(state, wrapper);
				loopVars[2] = new BooleanModel(value.equals(selected), wrapper);
				if( loopVars.length > 3 )
				{
					loopVars[3] = new BooleanModel(i == lastIndex, wrapper);
				}
				if( loopVars.length > 4 )
				{
					loopVars[4] = new NumberModel(i, wrapper);
				}
				try
				{
					body.render(writer);
				}
				catch( TemplateException e )
				{
					SectionUtils.throwRuntime(e);
				}
				i++;
			}
		}
	}
}
