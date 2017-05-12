package com.tle.webtests.pageobject.cal;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.DuplicatesTab;
import com.tle.webtests.pageobject.wizard.SubWizardPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.RepeaterControl;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.ResourceUniversalControlType;

public class CALWizardPage
{
	private final WizardPageTab wizardPage;
	private final List<SubWizardPage> sections = new ArrayList<SubWizardPage>();

	public CALWizardPage(PageContext context, WizardPageTab wizardPage)
	{
		this(context, wizardPage, 1);
	}

	public CALWizardPage(PageContext context, WizardPageTab wizardPage, int initialSections)
	{
		this.wizardPage = wizardPage;
		while( initialSections > 0 )
		{
			int sectionUpto = sections.size();
			int offset = 13 + (sectionUpto * 6);
			sections.add(new SubWizardPage(context, wizardPage, 2 + sectionUpto, offset));
			initialSections--;
		}
	}

	public void setTotalPages(String pages)
	{
		wizardPage.editbox(14, pages);
	}

	public void setPublisher(String publisher)
	{
		wizardPage.editbox(12, publisher);
	}

	public void setYearOfPublication(String year)
	{
		wizardPage.editbox(11, year);
	}

	public void addISBN(String isbn)
	{
		wizardPage.addToShuffleList(2, isbn);
	}

	public void setHoldingTitle(String title)
	{
		wizardPage.editbox(1, title);
	}

	public void setRange(int section, String range)
	{
		getSection(section).editbox(0, range);
	}

	private SubWizardPage getSection(int section)
	{
		return sections.get(section);
	}

	public void selectBook(String book)
	{
		wizardPage.selectDropDownReload(1, "Book");

		selectHolding(wizardPage.universalControl(2), book);
		
	}

	private void selectHolding(UniversalControl control, String title)
	{
		ResourceUniversalControlType resource = control.addDefaultResource(new ResourceUniversalControlType(control));
		SelectionSession session = resource.getSelectionSession();
		ItemListPage results = session.homeSearch('"' + title + '"');
		results.viewFromTitle(title).selectItem(resource.editPage()).save();
	}

	public SummaryPage publish()
	{
		return wizardPage.save().publish();
	}

	public CALViolationPage saveWithViolation()
	{
		wizardPage.saveNoConfirm();
		return new CALViolationPage(wizardPage.getContext()).get();
	}

	public void uploadSectionFile(int section, URL file)
	{
		getSection(section).addSingleFile(1, file);
	}

	public void addSection()
	{
		int sectionUpto = sections.size();
		int offset = 13 + (sectionUpto * 6);
		RepeaterControl repeater = wizardPage.repeater(11);
		sections.add(repeater.add(2 + sectionUpto, offset));
	}

	public void setPortionTitle(String title)
	{
		wizardPage.editbox(7, title);
	}

	public void setChapter(String chapter)
	{
		wizardPage.editbox(4, chapter);
	}

	public void setJournalNotes(String notes)
	{
		wizardPage.editbox(3, notes);
	}

	public void setJournalVolume(String volume)
	{
		wizardPage.editbox(11, volume);
	}

	public void selectJournal(String journal)
	{
		wizardPage.selectDropDownReload(1, "Journal");
		selectHolding(wizardPage.universalControl(3), journal);
	}

	public void addTopic(String topic)
	{
		wizardPage.addToShuffleList(9, topic);
	}

	public void addAuthor(String author)
	{
		wizardPage.addToShuffleList(5, author);
	}

	public void setISSN(String issn)
	{
		wizardPage.editbox(2, issn);
	}

	public DuplicatesTab publishDuplicate()
	{
		return wizardPage.save().finishInvalid(new DuplicatesTab(wizardPage.getContext()));
	}
}
