/**
 * 
 */
package com.tle.web.remoterepo.sru;

import com.tle.beans.entity.FederatedSearch;
import com.tle.web.remoterepo.event.RemoteRepoSearchEvent;
import com.tle.web.sections.SectionId;

/**
 * @author larry
 */
public class SruSearchEvent extends RemoteRepoSearchEvent<SruSearchEvent>
{
	protected SruSearchEvent(SectionId sectionId, FederatedSearch search)
	{
		super(sectionId, search);
	}
}
