package com.tle.web.sections.render;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.AbstractDirectEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.RenderEventListener;

@NonNullByDefault
public class ModalListener extends AbstractDirectEvent implements RenderEventListener, SectionId
{
	private ModalRenderer modal;
	private SectionTree tree;

	public ModalListener(String id, ModalRenderer modal, SectionTree tree)
	{
		super(PRIORITY_MODAL_LOGIC, id);
		this.modal = modal;
		this.tree = tree;
	}

	@Override
	public void render(RenderEventContext context)
	{
		if( modal.isModal(context) )
		{
			try
			{
				SectionResult result = modal.renderModal(context);
				if( result != null )
				{
					context.getRenderEvent().returnResult(result);
				}
			}
			catch( Exception e )
			{
				SectionUtils.throwRuntime(e);
			}
		}
	}

	@Override
	public void fireDirect(SectionId sectionId, SectionInfo info) throws Exception
	{
		if( modal.isModal(info) )
		{
			RenderContext renderContext = info.getRootRenderContext();
			renderContext.setModalId(forSectionId.getSectionId());
			info.renderNow();
		}
	}

	@Override
	public String getSectionId()
	{
		return forSectionId.getSectionId();
	}

	@Override
	public Section getSectionObject()
	{
		return (Section) modal;
	}

	@Override
	public SectionTree getTree()
	{
		return tree;
	}
}
