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

package com.tle.web.institution;

import javax.inject.Inject;

import com.tle.common.filesystem.handle.AllExportFile;
import com.tle.common.filesystem.handle.AllImportFile;
import com.tle.common.filesystem.handle.AllInstitutionsFile;
import com.tle.common.filesystem.handle.AllStagingFile;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.FileSystemService;
import com.tle.freetext.FreetextIndex;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;

public class AutoTestSetupSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@Component
	private Button clearButton;

	@EventFactory
	private EventGenerator events;

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private FreetextIndex freetextIndex;
	@Inject
	private InstitutionService institutionService;

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		clearButton.setClickHandler(events.getNamedHandler("clearData"));
		clearButton.setLabel(new TextLabel("Clear filestore and freetext"));
	}

	@EventHandlerMethod
	public void clearData(SectionInfo info)
	{
		fileSystemService.removeFile(new AllStagingFile());
		fileSystemService.removeFile(new AllInstitutionsFile());
		fileSystemService.removeFile(new AllImportFile());
		fileSystemService.removeFile(new AllExportFile());
		freetextIndex.deleteIndexes();
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( institutionService.getAllInstitutions().size() > 0 )
		{
			return new SimpleSectionResult("You must have no institutions before using this");
		}
		return renderSection(context, clearButton);
	}

}
