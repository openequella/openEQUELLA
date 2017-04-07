package com.tle.web.sections.generic;

import java.util.List;

import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Utils;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.RenderResultListener;
import com.tle.web.sections.registry.handler.StandardInterfaces;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TemplateResultCollector;

/**
 * An Abstract {@link Section}. This is the base class which most
 * {@code Section}'s will end up extending this class. Mostly this class
 * contains convenience methods for common rendering operations.
 * 
 * @author jmaginnis
 */
@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractSection implements Section
{
	private boolean treeIndexed;

	public AbstractSection()
	{
		treeIndexed = true;
	}

	/**
	 * The section will be registered by class in the SectionTree
	 * 
	 * @see StandardInterfaces
	 * @return The class which will be registered in the tree.
	 */
	@Override
	public boolean isTreeIndexed()
	{
		return treeIndexed;
	}

	/**
	 * @see StandardInterfaces
	 * @param treeIndexed The section will be registered by class in the
	 *            SectionTree
	 */
	public void setTreeIndexed(boolean treeIndexed)
	{
		this.treeIndexed = treeIndexed;
	}

	protected abstract Class<?> getModelClass();

	/**
	 * Instantiate an instance of the model for this Section. <br>
	 * Just calls newInstance() on the class returned from
	 * {@link #getModelClass()}.
	 */
	@Override
	public Object instantiateModel(SectionInfo info)
	{
		try
		{
			return getModelClass().newInstance();
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Render a single Section to a TemplateResult. <br>
	 * It uses a {@link TemplateResultCollector} to collect the result.
	 * 
	 * @param info The current info
	 * @param id The section's id
	 * @return The result of the render as a TemplateResult
	 */
	protected TemplateResult renderToTemplate(RenderContext info, String id)
	{
		TemplateResultCollector collector = new TemplateResultCollector();
		RenderEvent renderEvent = new RenderEvent(info, id, collector);
		info.processEvent(renderEvent);
		return collector.getTemplateResult();
	}

	/**
	 * Send {@link RenderEvent}'s to each child until one returns a
	 * {@link SectionResult}.
	 * 
	 * @param context The context
	 * @return The first {@code SectionResult} returned by a child that is not
	 *         null.
	 */
	@Nullable
	public SectionResult renderFirstResult(RenderEventContext context)
	{
		List<SectionId> children = context.getChildIds(context);
		for( SectionId childId : children )
		{
			SectionResult first = SectionUtils.renderSectionResult(context, childId);
			if( first != null )
			{
				return first;
			}
		}
		return null;
	}

	/**
	 * Render a single {@code Section} as a {@link SectionRenderable}.<br>
	 * This version takes a {@link SectionId}. {@link AbstractPrototypeSection}
	 * implements {@code SectionId}, so this method is convenient for the common
	 * case.
	 * 
	 * @param info The current info
	 * @param section The {@code SectionId} to render
	 * @return The result as a {@link SectionRenderable}.
	 */
	@Nullable
	protected SectionRenderable renderSection(RenderContext info, SectionId section)
	{
		return SectionUtils.renderSection(info, section);
	}

	/**
	 * Send {@code RenderEvent}'s to all child {@code Section}s using the given
	 * {@code RenderResultListener}.
	 * 
	 * @param <T> The type of the {@code RenderResultListener}.
	 * @param context The context
	 * @param listener The listener
	 * @return The passed in listener
	 */
	protected <T extends RenderResultListener> T renderChildren(RenderEventContext context, T listener)
	{
		return SectionUtils.renderChildren(context, context, listener);
	}

	protected <T extends RenderResultListener> T renderChildren(RenderContext context, SectionId id, T listener)
	{
		return SectionUtils.renderChildren(context, id, listener);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		// nothing
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		// nothing
	}

	@Override
	public String getDefaultPropertyName()
	{
		String className = getClass().getSimpleName();
		if( className.endsWith("Section") )
		{
			className = Utils.safeSubstring(className, 0, -("Section".length()));
		}
		String[] caps = className.split("[a-z0-9]*");
		return Utils.join(caps, "").toLowerCase();
	}
}
