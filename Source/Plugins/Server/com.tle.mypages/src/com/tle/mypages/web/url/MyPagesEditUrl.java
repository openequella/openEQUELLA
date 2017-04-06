package com.tle.mypages.web.url;

import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.web.model.MyPagesContributeModel;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.ModalSessionCallback;
import com.tle.web.sections.equella.ModalSessionService;

/**
 * @author aholland
 */
public class MyPagesEditUrl implements Bookmark
{
	private final SectionInfo originalInfo;
	private final String wizid;
	private final String pageUuid;
	private final ModalSessionCallback finishedCallback;
	private final ModalSessionService modalService;

	private SectionInfo forwardInfo;

	public MyPagesEditUrl(SectionInfo originalInfo, String wizid, String pageUuid,
		ModalSessionCallback finishedCallback, ModalSessionService modalService)
	{
		this.originalInfo = originalInfo;
		this.wizid = wizid;
		this.pageUuid = pageUuid;
		this.finishedCallback = finishedCallback;
		this.modalService = modalService;
	}

	public Bookmark getBookmark()
	{
		return getForwardInfo().getPublicBookmark();
	}

	@Override
	public String getHref()
	{
		return getBookmark().getHref();
	}

	public SectionInfo getForwardInfo()
	{
		if( forwardInfo == null )
		{
			forwardInfo = modalService.createForward(originalInfo, MyPagesConstants.URL_MYPAGESEDIT, finishedCallback);

			final MyPagesContributeModel model = forwardInfo.getModelForId(MyPagesConstants.SECTION_CONTRIBUTE);
			model.setSession(wizid);
			model.setPageUuid(pageUuid);
			model.setModal(finishedCallback != null);
			model.setLoad(true);
		}
		return forwardInfo;
	}
}
