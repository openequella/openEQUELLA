package com.tle.mypages.web.model;

import java.util.List;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
public class MyPagesContributeModel
{
	@Bookmarked(name = "m")
	private boolean modal;
	@Bookmarked(name = "s")
	private String session;
	@Bookmarked(name = "p")
	private String pageUuid;
	@Bookmarked(name = "i")
	private String itemId;
	@Bookmarked(name = "c")
	private String finishedCallback;

	// used just once upon entry, then reset
	@Bookmarked(name = "l")
	private boolean load;

	private boolean showPreviewCheckBox;

	public boolean isShowPreviewCheckBox()
	{
		return showPreviewCheckBox;
	}

	public void setShowPreviewCheckBox(boolean showPreviewCheckBox)
	{
		this.showPreviewCheckBox = showPreviewCheckBox;
	}

	private List<SectionRenderable> renderables;

	public boolean isModal()
	{
		return modal;
	}

	public void setModal(boolean modal)
	{
		this.modal = modal;
	}

	public String getSession()
	{
		return session;
	}

	public void setSession(String session)
	{
		this.session = session;
	}

	public String getPageUuid()
	{
		return pageUuid;
	}

	public void setPageUuid(String pageUuid)
	{
		this.pageUuid = pageUuid;
	}

	public String getItemId()
	{
		return itemId;
	}

	public void setItemId(String itemId)
	{
		this.itemId = itemId;
	}

	public boolean isLoad()
	{
		return load;
	}

	public void setLoad(boolean load)
	{
		this.load = load;
	}

	public String getFinishedCallback()
	{
		return finishedCallback;
	}

	public void setFinishedCallback(String finishedCallback)
	{
		this.finishedCallback = finishedCallback;
	}

	public List<SectionRenderable> getRenderables()
	{
		return renderables;
	}

	public void setRenderables(List<SectionRenderable> renderables)
	{
		this.renderables = renderables;
	}
}
