package com.tle.web.search.actions;

import com.tle.web.search.actions.AbstractShareSearchQueryAction.ShareSearchQueryModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Link;

@SuppressWarnings("nls")
public abstract class AbstractShareSearchQueryAction extends AbstractPrototypeSection<ShareSearchQueryModel>
	implements
		HtmlRenderer
{
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		Link button = getDialog().getOpener();
		button.setLabel(getLabel());
		button.setStyleClass("share-search");
		button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( getModel(context).isDisabled() )
		{
			return null;
		}
		return SectionUtils.renderSectionResult(context, getDialog().getOpener());
	}

	public void disable(SectionInfo info)
	{
		getModel(info).setDisabled(true);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ShareSearchQueryModel();
	}

	public static class ShareSearchQueryModel
	{
		private boolean disabled;

		public boolean isDisabled()
		{
			return disabled;
		}

		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}
	}

	public abstract Label getLabel();

	public abstract EquellaDialog<?> getDialog();
}
