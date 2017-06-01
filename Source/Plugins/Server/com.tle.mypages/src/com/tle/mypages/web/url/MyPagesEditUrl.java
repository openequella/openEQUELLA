/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
