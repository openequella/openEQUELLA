package com.tle.web.sections.equella.render;

import static com.tle.web.sections.render.CssInclude.include;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.equella.component.model.BoxState;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageButtonRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;

@SuppressWarnings("nls")
public class BoxRenderer extends TagRenderer
{
	private static final Set<String> INTERNAL_EVENTS = new HashSet<String>(Arrays.asList("edit", "close", "minimise"));

	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(BoxRenderer.class);

	private static final PreRenderable INCLUDE = new IncludeFile(RESOURCES.url("scripts/component/boxrenderer.js"),
		JQueryUIEffects.BLIND);
	public static final ExternallyDefinedFunction MIN_MAX_EFFECT = new ExternallyDefinedFunction("toggleMinimise",
		INCLUDE);
	private static final CssInclude CSS = include(RESOURCES.url("css/component/box.css")).hasRtl().make();

	private static final Label EXPAND_LABEL = new KeyLabel(RESOURCES.key("component.box.expand"));
	private static final String EXPAND_LTR_IMAGE_URL = RESOURCES.url("images/component/box_head_closed.ltr.gif");
	private static final String EXPAND_RTL_IMAGE_URL = RESOURCES.url("images/component/box_head_closed.rtl.gif");

	private static final Label CONTRACT_LABEL = new KeyLabel(RESOURCES.key("component.box.contract"));
	private static final String CONTRACT_IMAGE_URL = RESOURCES.url("images/component/box_head_open.gif");

	private static final Label EDIT_LABEL = new KeyLabel(RESOURCES.key("component.box.edit"));
	private static final String EDIT_IMAGE_URL = RESOURCES.url("images/component/box_edit.png");

	private static final Label CLOSE_LABEL = new KeyLabel(RESOURCES.key("component.box.close"));
	private static final String CLOSE_IMAGE_URL = RESOURCES.url("images/component/box_close.png");

	private final BoxState boxState;

	private ImageRenderer minimise;
	private ImageButtonRenderer edit;
	private ImageButtonRenderer close;
	private DivRenderer boxHead;
	private SectionRenderable result;
	private boolean acMode;

	public BoxRenderer(FreemarkerFactory view, BoxState state, boolean acMode)
	{
		super("div", state);
		this.boxState = state;
		this.acMode = acMode;
		if( acMode )
		{
			boxState.setNoMinMaxOnHeader(true);
			boxState.setMinimised(false);
		}
		addClass("box");
		nestedRenderable = view.createResultWithModel("component/box.ftl", this);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(CSS);
		info.preRender(getNestedRenderable());
	}

	@Override
	public TagRenderer setNestedRenderable(SectionRenderable nested)
	{
		this.result = nested;
		return this;
	}

	public String getTitle()
	{
		return boxState.getLabelText();
	}

	public boolean isMinimised()
	{
		return boxState.isMinimised();
	}

	public ImageRenderer getMinimise()
	{
		if( minimise == null )
		{
			final HtmlComponentState minState;
			if( boxState.isNoMinMaxOnHeader() )
			{
				minState = makeState("minimise", false);
			}
			else
			{
				minState = new HtmlComponentState();

			}
			if( minState != null )
			{
				final boolean isMin = isMinimised();
				String url = !isMin ? CONTRACT_IMAGE_URL : CurrentLocale.isRightToLeft() ? EXPAND_RTL_IMAGE_URL
					: EXPAND_LTR_IMAGE_URL;
				minimise = new ImageRenderer(minState, url, isMin ? EXPAND_LABEL : CONTRACT_LABEL);
			}
		}
		return minimise;
	}

	public ImageButtonRenderer getEdit()
	{
		if( edit == null )
		{
			final HtmlComponentState state = makeState("edit", false);
			if( state != null )
			{
				edit = new ImageButtonRenderer(state);
				edit.setSource(EDIT_IMAGE_URL);
				edit.setNestedRenderable(new LabelRenderer(EDIT_LABEL));
			}
		}
		return edit;
	}

	public ImageButtonRenderer getClose()
	{
		if( close == null )
		{
			HtmlComponentState state = makeState("close", false);
			if( state != null )
			{
				close = new ImageButtonRenderer(state);
				close.setSource(CLOSE_IMAGE_URL);
				close.setNestedRenderable(new LabelRenderer(CLOSE_LABEL));
			}
		}
		return close;
	}

	public DivRenderer getBoxHead()
	{
		if( boxHead == null )
		{
			final HtmlComponentState headState;
			if( !boxState.isNoMinMaxOnHeader() )
			{
				headState = makeState("minimise", true);
			}
			else
			{
				headState = new HtmlComponentState();
			}

			boxHead = new DivRenderer(headState);
		}
		return boxHead;
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		String divId = getVisibleElementId().getElementId(writer);
		AjaxRenderContext ajaxContext = writer.getAttributeForClass(AjaxRenderContext.class);
		if( ajaxContext != null )
		{
			writer = new SectionWriter(ajaxContext.startCapture(writer, divId, null, false), writer);
		}
		super.writeMiddle(writer);
		if( ajaxContext != null )
		{
			ajaxContext.endCapture(divId);
		}
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( !INTERNAL_EVENTS.contains(event) )
		{
			super.processHandler(writer, attrs, event, handler);
		}
	}

	private HtmlComponentState makeState(String event, boolean dontRequireHandler)
	{
		final JSHandler handler = boxState.getHandler(event);
		if( (dontRequireHandler || handler != null) && !acMode )
		{
			final HtmlComponentState state = new HtmlComponentState();
			if( handler != null )
			{
				if( !event.equals("minimise") )
				{
					state.setClickHandler(handler);
					state.addClass("action");
				}
				else
				{
					state.setClickHandler(new OverrideHandler(boxState.getToggleMinimise(), MIN_MAX_EFFECT));
				}
			}
			final String id = getId();
			if( !Check.isEmpty(id) )
			{
				state.setId(id + '_' + event);
			}
			return state;
		}
		return null;
	}

	public SectionRenderable getResult()
	{
		return result;
	}
}