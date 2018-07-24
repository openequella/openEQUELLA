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

package com.tle.web.errors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.registry.TreeRegistry;
import com.tle.web.template.Decorations;
import com.tle.web.template.RenderNewTemplate;

@Bind
@Singleton
public class DefaultExceptionHandler extends AbstractExceptionHandler
{
	private static final String ERRORTREE_KEY = "/error.do"; //$NON-NLS-1$
	@Inject
	private TreeRegistry treeRegistry;

	protected SectionInfo createNewInfo(Throwable exception, SectionInfo info, SectionsController controller)
	{
		SectionTree errorTree = treeRegistry.getTreeForPath(ERRORTREE_KEY);
		MutableSectionInfo newInfo = controller.createInfoFromTree(errorTree, info);
		newInfo.setAttribute(RenderNewTemplate.DisableNewUI(), true);
		newInfo.preventGET();
		newInfo.setAttribute(SectionInfo.KEY_ORIGINAL_EXCEPTION, exception);
		newInfo.setAttribute(SectionInfo.KEY_MATCHED_EXCEPTION, getFirstCause(exception));
		newInfo.setErrored();
		Decorations.setDecorations(newInfo, Decorations.getDecorations(info));
		return newInfo;
	}

	protected boolean checkRendered(SectionInfo info)
	{
		if( !info.isRendered() )
		{
			info.setRendered();
		}
		else if( info.getResponse().isCommitted() )
		{
			return true;
		}
		return false;
	}

	@Override
	public void handle(Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event)
	{
		if( event != null )
		{
			SectionUtils.throwRuntime(exception);
		}
		if( checkRendered(info) )
		{
			return;
		}
		markHandled(info);
		SectionInfo newInfo = createNewInfo(exception, info, controller);
		controller.execute(newInfo);
	}
}
