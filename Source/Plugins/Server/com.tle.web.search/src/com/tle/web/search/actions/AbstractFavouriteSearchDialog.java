package com.tle.web.search.actions;

import java.util.Collection;
import java.util.Collections;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

public abstract class AbstractFavouriteSearchDialog extends EquellaDialog<DialogModel>
{
	@PlugKey("actions.favourite.dialog.title")
	private static Label LABEL_TITLE;

	private JSCallable reloadParent;

	@Component
	@PlugKey("actions.favourite.button.name")
	private Button okButton;

	protected abstract AbstractFavouriteSearchSection getContentSection();

	protected AbstractFavouriteSearchDialog()
	{
		setAjax(true);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		AbstractFavouriteSearchSection contentSection = getContentSection();
		contentSection.setContainerDialog(this);
		tree.registerInnerSection(contentSection, id);

		okButton.setComponentAttribute(ButtonType.class, ButtonType.SAVE);
		okButton.setClickHandler(contentSection.getAddHandler());
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		reloadParent = addParentCallable(new ReloadFunction(false));
		super.treeFinished(id, tree);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		return Collections.singleton(okButton);
	}

	public void close(SectionInfo info)
	{
		closeDialog(info, reloadParent);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public DialogModel instantiateDialogModel(SectionInfo info)
	{
		return new DialogModel();
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return SectionUtils.renderSection(context, getContentSection());
	}
}
