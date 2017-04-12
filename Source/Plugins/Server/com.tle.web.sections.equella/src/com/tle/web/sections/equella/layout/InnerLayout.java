package com.tle.web.sections.equella.layout;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.template.Decorations;

/**
 * The reality is, there is only one inner layout. The dialog inner layout is
 * set using the Decorations.layoutSelectors TODO: it would be nice to roll
 * these into the same thing
 * 
 * @author aholland
 */
public class InnerLayout implements LayoutSelector
{
	// used in plugin extension points (point-id="sectionTree")
	private static final String TREE_LAYOUT_KEY = "LAYOUT"; //$NON-NLS-1$

	private static final String INNER_LAYOUT_KEY = "$INNER_LAYOUT$"; //$NON-NLS-1$
	private static final String SELECTORS_KEY = "$LAYOUT_SELECTORS$"; //$NON-NLS-1$

	public static final InnerLayout STANDARD = new InnerLayout("layouts/inner/standard.ftl"); //$NON-NLS-1$
	public static final InnerLayout DIALOG = new InnerLayout("layouts/inner/dialog.ftl"); //$NON-NLS-1$

	private final String ftl;

	public InnerLayout(String ftl)
	{
		this.ftl = ftl;
	}

	public String getFtl()
	{
		return ftl;
	}

	@Override
	public TemplateResult getLayout(Decorations decorations, RenderContext info, TemplateResult templateResult)
		throws Exception
	{
		return null;
	}

	@Override
	public void preProcess(Decorations decorations)
	{
	}

	public static void setLayout(SectionInfo info, InnerLayout layout)
	{
		if( layout == null )
		{
			throw new IllegalArgumentException("layout cannot be null"); //$NON-NLS-1$
		}
		addLayoutSelector(info, layout);
		info.setAttribute(INNER_LAYOUT_KEY, layout);
	}

	/**
	 * The current outer layout (frameset, standard).
	 * 
	 * @param info
	 * @return Will never return null, default layout is InnerLayout.STANDARD
	 */
	public static InnerLayout getLayout(SectionInfo info)
	{
		InnerLayout layout = info.getAttribute(INNER_LAYOUT_KEY);
		if( layout == null )
		{
			layout = InnerLayout.STANDARD;
			setLayout(info, layout);
		}
		return layout;
	}

	/**
	 * Used by RenderTemplate only.
	 * 
	 * @param info
	 * @return
	 */
	public static List<LayoutSelector> getLayoutSelectors(SectionInfo info)
	{
		final List<LayoutSelector> layoutSelectors = getLayoutSelectorsObj(info).getLayoutSelectors();
		// add any tree specific selector
		final LayoutSelector selector = info.getTreeAttribute(TREE_LAYOUT_KEY);
		if( selector != null )
		{
			layoutSelectors.add(selector);
		}
		return layoutSelectors;
	}

	private static LayoutSelectors getLayoutSelectorsObj(SectionInfo info)
	{
		LayoutSelectors selectors = info.getAttribute(SELECTORS_KEY);
		if( selectors == null )
		{
			selectors = new LayoutSelectors();
			info.setAttribute(SELECTORS_KEY, selectors);
		}
		return selectors;
	}

	public static void addLayoutSelector(SectionInfo info, LayoutSelector selector)
	{
		getLayoutSelectorsObj(info).addLayoutSelector(selector);
	}

	private static class LayoutSelectors
	{
		private final Set<LayoutSelector> selectors = new LinkedHashSet<LayoutSelector>();

		public void addLayoutSelector(LayoutSelector layoutSelector)
		{
			selectors.add(layoutSelector);
		}

		public List<LayoutSelector> getLayoutSelectors()
		{
			return new ArrayList<LayoutSelector>(selectors);
		}
	}
}
