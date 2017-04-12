package com.tle.web.sections.jquery.libraries.effects;

import java.util.HashMap;
import java.util.Map;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryUIEffects implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	private static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.ui.effect.js", JQueryCore.PRERENDER);

	private static final Map<String, PreRenderable> effectLibraries = new HashMap<String, PreRenderable>();

	public static PreRenderable getEffectLibrary(String effect)
	{
		return effectLibraries.get(effect);
	}

	public static PreRenderable addEffect(String effect, PreRenderable prerender)
	{
		effectLibraries.put(effect, prerender);
		return prerender;
	}

	public static JQueryUIEffectsLibrary addStdEffect(String effect)
	{
		JQueryUIEffectsLibrary lib = new JQueryUIEffectsLibrary(effect, "jquery.ui.effect-" + effect + ".js");
		effectLibraries.put(effect, lib);
		return lib;
	}

	public static final JQueryUIEffectsLibrary FADE = addStdEffect("fade");

	public static final JQueryUIEffectsLibrary BLIND = addStdEffect("blind");

	public static final JQueryUIEffectsLibrary SLIDE = addStdEffect("slide");

	public static final JQueryUIEffectsLibrary EXPLODE = addStdEffect("explode");

	public static final JQueryUIEffectsLibrary PULSATE = addStdEffect("pulsate");

	public static final JQueryUIEffectsLibrary SHAKE = addStdEffect("shake");

	public static final JQueryUIEffectsLibrary SCALE = addStdEffect("scale");

	public static final JQueryUIEffectsLibrary BOUNCE = addStdEffect("bounce");

	public static final JQueryUIEffectsLibrary CLIP = addStdEffect("clip");

	public static final JQueryUIEffectsLibrary DROP = addStdEffect("drop");

	public static final JQueryUIEffectsLibrary FOLD = addStdEffect("fold");

	public static final JQueryUIEffectsLibrary HIGHLIGHT = addStdEffect("highlight");

	public static final JQueryUIEffectsLibrary TRANSFER = addStdEffect("transfer");

	public static final PreRenderable PRERENDER_ALL = new CombinedRenderer(effectLibraries.values());

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.effects.core");
	}

	@Override
	public String getId()
	{
		return "ui-effects-core";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER_ALL;
	}

	public static class JQueryUIEffectsLibrary extends JQueryLibraryInclude
	{
		private final String effectName;

		public JQueryUIEffectsLibrary(String effectName, String filename)
		{
			super(filename, JQueryUIEffects.PRERENDER);
			this.effectName = effectName;
		}

		public String getEffectName()
		{
			return effectName;
		}

		public FunctionCallExpression show()
		{
			return new FunctionCallExpression(new ExternallyDefinedFunction("show", this), getEffectName());
		}

		public FunctionCallExpression hide()
		{
			return new FunctionCallExpression(new ExternallyDefinedFunction("hide", this), getEffectName());
		}
	}
}
