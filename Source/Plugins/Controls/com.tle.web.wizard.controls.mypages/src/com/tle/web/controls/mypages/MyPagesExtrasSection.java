package com.tle.web.controls.mypages;

import javax.inject.Inject;

import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.mypages.service.MyPagesService;
import com.tle.mypages.web.event.SavePageEvent;
import com.tle.mypages.web.event.SavePageEventListener;
import com.tle.mypages.web.model.MyPagesContributeModel;
import com.tle.mypages.web.section.MyPagesContributeSection;
import com.tle.web.controls.mypages.MyPagesExtrasSection.MyPagesPreviewModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author dlashmar
 */
public class MyPagesExtrasSection extends AbstractPrototypeSection<MyPagesPreviewModel>
	implements
		HtmlRenderer,
		SavePageEventListener
{
	@Inject
	private MyPagesService myPagesService;

	@PlugKey("handlers.mypages.checkbox.preview")
	@Component(stateful = false)
	private Checkbox previewCheckBox;
	@PlugKey("handlers.mypages.checkbox.restrict")
	@Component(stateful = false)
	private Checkbox restrictCheckbox;

	@TreeLookup
	private MyPagesContributeSection contribSection;

	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		MyPagesContributeModel model = contribSection.getModel(context);
		HtmlAttachment page = myPagesService.getPageAttachment(context, model.getSession(), model.getItemId(),
			model.getPageUuid());
		if( page != null )
		{
			previewCheckBox.setChecked(context, page.isPreview());
			restrictCheckbox.setChecked(context, page.isRestricted());
			return viewFactory.createResult("mypagesextra.ftl", context);
		}
		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.addListener(null, SavePageEventListener.class, id);
	}

	@Override
	public void doSavePageEvent(SectionInfo info, SavePageEvent event)
	{
		MyPagesContributeModel model = contribSection.getModel(info);
		HtmlAttachment page = myPagesService.getPageAttachment(info, model.getSession(), model.getItemId(),
			model.getPageUuid());
		page.setPreview(previewCheckBox.isDisplayed(info) && previewCheckBox.isChecked(info));
		page.setRestricted(restrictCheckbox.isChecked(info));
	}

	public void setShowPreview(SectionInfo info, boolean preview)
	{
		final MyPagesPreviewModel model = getModel(info);
		model.setShowPreview(preview);
	}

	public void setShowRestrict(SectionInfo info, boolean restrict)
	{
		getModel(info).setShowRestrict(restrict);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new MyPagesPreviewModel();
	}

	public Checkbox getPreviewCheckBox()
	{
		return previewCheckBox;
	}

	public Checkbox getRestrictCheckbox()
	{
		return restrictCheckbox;
	}

	public static class MyPagesPreviewModel
	{
		private boolean showPreview;
		private boolean showRestrict;

		public boolean isShowPreview()
		{
			return showPreview;
		}

		public void setShowPreview(boolean showPreview)
		{
			this.showPreview = showPreview;
		}

		public boolean isShowRestrict()
		{
			return showRestrict;
		}

		public void setShowRestrict(boolean showRestrict)
		{
			this.showRestrict = showRestrict;
		}
	}
}
