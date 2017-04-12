package com.tle.web.institution;

import javax.inject.Inject;

import com.tle.core.filesystem.AllExportFile;
import com.tle.core.filesystem.AllImportFile;
import com.tle.core.filesystem.AllInstitutionsFile;
import com.tle.core.filesystem.AllStagingFile;
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
