package com.tle.beans.item.cal;

import java.util.Arrays;

import junit.framework.TestCase;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.cal.CitationGenerator;

public class CitationTest extends TestCase
{
	private CitationGenerator citate;

	@Override
	protected void setUp() throws Exception
	{
		citate = new CitationGenerator();
	}

	public void testSimpleBook()
	{
		CALHolding holding = new CALHolding();
		holding.setTitle("Simple Book");
		PropBagEx copyrightXml = new PropBagEx();
		assertEquals("<b><i>Simple Book</i></b> n.d., n.p.", citate.citeBook(holding, copyrightXml));
	}

	public void testNoPortionDetails()
	{
		CALHolding holding = new CALHolding();
		holding.setTitle("Simple Book");
		CALPortion portion = new CALPortion();
		CALSection section = new CALSection();
		section.setRange("1-10");
		portion.setSections(Arrays.asList(section));
		PropBagEx holdingXml = new PropBagEx();
		PropBagEx portionXml = new PropBagEx();
		assertEquals("<b><i>Simple Book</i></b> n.d., n.p., pp. 1-10.",
			citate.citeBookPortion(holding, portion, holdingXml, portionXml));
	}

	public void testSimplePortion()
	{
		CALHolding holding = new CALHolding();
		holding.setTitle("Simple Book");
		CALPortion portion = new CALPortion();
		portion.setTitle("Simple Portion");
		CALSection section = new CALSection();
		section.setRange("1-10");
		portion.setSections(Arrays.asList(section));
		PropBagEx holdingXml = new PropBagEx();
		PropBagEx portionXml = new PropBagEx();
		assertEquals("'Simple Portion' n.d. in <b><i>Simple Book</i></b>, n.p., pp. 1-10.",
			citate.citeBookPortion(holding, portion, holdingXml, portionXml));
	}

	public void testAuthoredPortion()
	{
		CALHolding holding = new CALHolding();
		holding.setTitle("Simple Book");
		CALPortion portion = new CALPortion();
		portion.setTitle("Authored Portion");
		CALSection section = new CALSection();
		section.setRange("100-110");
		portion.setSections(Arrays.asList(section));
		portion.setAuthors(Arrays.asList("Author1", "Author2", "Author3"));
		PropBagEx holdingXml = new PropBagEx();
		PropBagEx portionXml = new PropBagEx();
		assertEquals(
			"Author1, Author2 &amp; Author3 n.d., 'Authored Portion' in <b><i>Simple Book</i></b>, n.p., pp. 100-110.",
			citate.citeBookPortion(holding, portion, holdingXml, portionXml));
	}

	public void testConferenceNoAuthors()
	{
		PropBagEx copyrightXml = new PropBagEx();
		CALHolding holding = new CALHolding();
		holding.setTitle("Book Title");
		holding.setPubDate("2008");
		copyrightXml.setNode("conference/name", "Simple Conference");
		copyrightXml.setNode("conference/location", "Hobart");
		copyrightXml.setNode("conference/year", "2008");
		assertEquals("Simple Conference 2008, Hobart, 2008.", citate.citeBook(holding, copyrightXml));
	}

	public void testConferenceAuthors()
	{
		PropBagEx copyrightXml = new PropBagEx();
		CALHolding holding = new CALHolding();
		holding.setTitle("Book Title");
		holding.setPubDate("2008");
		holding.setAuthors(Arrays.asList("Jolse Maginnis", "Doolse Mahinnis", "Bruce Bruce"));
		copyrightXml.setNode("conference/name", "Simple Conference");
		copyrightXml.setNode("conference/location", "Hobart");
		copyrightXml.setNode("conference/year", "2008");
		assertEquals("Jolse Maginnis, Doolse Mahinnis &amp; Bruce Bruce 2008, <b><i>Book Title</i></b>, n.p.",
			citate.citeBook(holding, copyrightXml));
	}

	public void testConferenceAuthorsEditors()
	{
		PropBagEx copyrightXml = new PropBagEx();
		CALHolding holding = new CALHolding();
		holding.setTitle("Book Title");
		holding.setPubDate("2008");
		holding.setAuthors(Arrays.asList("Jolse Maginnis", "Doolse Mahinnis", "Bruce Bruce"));
		copyrightXml.createNode("editors/editor", "Editor 1");
		copyrightXml.createNode("editors/editor", "Editor 2");
		copyrightXml.setNode("conference/name", "Simple Conference");
		copyrightXml.setNode("conference/location", "Hobart");
		copyrightXml.setNode("conference/year", "2008");
		assertEquals("Jolse Maginnis, Doolse Mahinnis &amp; Bruce Bruce 2008, <b><i>Book Title</i></b>, n.p.",
			citate.citeBook(holding, copyrightXml));
	}

	public void testConferenceEditors()
	{
		PropBagEx copyrightXml = new PropBagEx();
		CALHolding holding = new CALHolding();
		holding.setTitle("Book Title");
		holding.setPubDate("2008");
		copyrightXml.createNode("editors/editor", "Editor 1");
		copyrightXml.createNode("editors/editor", "Editor 2");
		copyrightXml.setNode("conference/name", "Simple Conference");
		copyrightXml.setNode("conference/location", "Hobart");
		copyrightXml.setNode("conference/year", "2008");
		assertEquals("Editor 1 &amp; Editor 2 (eds.) 2008, <b><i>Book Title</i></b>, n.p.",
			citate.citeBook(holding, copyrightXml));
	}

	public void testConferenceEditor()
	{
		PropBagEx copyrightXml = new PropBagEx();
		CALHolding holding = new CALHolding();
		holding.setTitle("Book Title");
		holding.setPubDate("2008");
		copyrightXml.createNode("editors/editor", "Lone Editor");
		copyrightXml.setNode("conference/name", "Simple Conference");
		copyrightXml.setNode("conference/location", "Hobart");
		copyrightXml.setNode("conference/year", "2008");
		assertEquals("Lone Editor (ed.) 2008, <b><i>Book Title</i></b>, n.p.", citate.citeBook(holding, copyrightXml));
	}

	public void testSimpleJournal()
	{
		CALHolding holding = new CALHolding();
		holding.setTitle("Simple Journal");
		PropBagEx copyrightXml = new PropBagEx();
		assertEquals("<b><i>Simple Journal</i></b> n.d.", citate.citeJournal(holding, copyrightXml));
	}

	public void testJournalIssueNumber()
	{
		CALHolding holding = new CALHolding();
		holding.setTitle("Simple Journal");
		PropBagEx holdingXml = new PropBagEx();
		holding.setVolume("10");
		holdingXml.setNode("issue/value", "190");
		holdingXml.setNode("issue/type", "number");
		assertEquals("<b><i>Simple Journal</i></b> n.d., vol. 10, no. 190.", citate.citeJournal(holding, holdingXml));
	}

	public void testJournalPortion()
	{
		CALHolding holding = new CALHolding();
		holding.setTitle("Simple Journal");
		PropBagEx portionXml = new PropBagEx();
		PropBagEx holdingXml = new PropBagEx();
		holding.setVolume("10");
		holdingXml.setNode("issue/value", "190");
		holdingXml.setNode("issue/type", "number");
		CALPortion portion = new CALPortion();
		portion.setTitle("Authored Portion");
		CALSection section = new CALSection();
		section.setRange("100-110");
		portion.setSections(Arrays.asList(section));
		portion.setAuthors(Arrays.asList("Author1", "Author2", "Author3"));
		assertEquals(
			"Author1, Author2 &amp; Author3 n.d., Authored Portion, <b><i>Simple Journal</i></b>, vol. 10, no. 190, pp. 100-110.",
			citate.citeJournalPortion(holding, portion, holdingXml, portionXml));
	}
}
