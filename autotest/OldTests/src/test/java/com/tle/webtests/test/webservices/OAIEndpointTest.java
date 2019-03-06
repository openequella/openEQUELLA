package com.tle.webtests.test.webservices;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.oai.OAIPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("fiveo")
public class OAIEndpointTest extends AbstractCleanupTest
{

	private static final String COLLECTION = "OAI Collection";
	private static final String COLLECTION_UUID = "d9615562-3e3e-43bc-8fb5-8867c25285ef";
	private static final String FORMAT = "oai_dc";

	@Test
	public void doesDcFormatExist() throws Exception
	{
		OAIPage oaiPage = new OAIPage(context);
		oaiPage.identify();
		List<String> listMetadataFormats = oaiPage.listMetadataFormats();
		Assert.assertTrue(listMetadataFormats.contains(FORMAT));
	}

	@Test
	public void testItemsWithNewEndPoint() throws Exception
	{
		String fullName = context.getFullName("New OAI test item");

		logon("AutoTest", "automated");
		WizardPageTab wiz = new ContributePage(context).load().openWizard(COLLECTION);

		wiz.editbox(1, fullName);
		wiz.save().publish();

		OAIPage newOai = new OAIPage(context);

		Map<String, String> results = newOai.listResults(FORMAT);
		Assert.assertTrue(results.containsValue(fullName));
		Iterator<Entry<String, String>> iterator = results.entrySet().iterator();
		String identifier = null;
		while( iterator.hasNext() )
		{
			Map.Entry<java.lang.String, java.lang.String> entry = (Map.Entry<java.lang.String, java.lang.String>) iterator
				.next();

			if( fullName.equals(entry.getValue()) )
			{
				identifier = entry.getKey();
			}

		}
		assertNotNull(identifier);

		List<String> listSets = newOai.listSets();
		Assert.assertTrue(listSets.contains("OAI Dynamic Collection"));

		List<String> identifiers = newOai.listIdentifiers(FORMAT);
		assertTrue(identifiers.contains(identifier));
		PropBagEx record = newOai.getRecord(FORMAT, identifier);
		String node = record.getNode("/item/name");
		Assert.assertTrue(node.equals(fullName));
	}

	@Test
	public void testItemsWithOldEndPoint() throws Exception
	{
		String fullName = context.getFullName("Old OAI test item");

		logon("AutoTest", "automated");
		WizardPageTab wiz = new ContributePage(context).load().openWizard(COLLECTION);

		wiz.editbox(1, fullName);
		wiz.save().publish();

		OAIPage oldOai = new OAIPage(context, true);

		Map<String, String> results = oldOai.listResults(FORMAT, COLLECTION_UUID);
		Iterator<Entry<String, String>> iterator = results.entrySet().iterator();
		String identifier = null;
		while( iterator.hasNext() )
		{
			Map.Entry<java.lang.String, java.lang.String> entry = (Map.Entry<java.lang.String, java.lang.String>) iterator
				.next();

			if( fullName.equals(entry.getValue()) )
			{
				identifier = entry.getKey();
			}

		}
		assertNotNull(identifier);

		List<String> listSets = oldOai.listSets();
		Assert.assertTrue(listSets.contains(COLLECTION));

		List<String> identifiers = oldOai.listIdentifiers(FORMAT, COLLECTION_UUID);
		Assert.assertTrue(identifiers.contains(identifier));

		PropBagEx record = oldOai.getRecord(FORMAT, identifier);
		Assert.assertTrue(record.getNode("/item/name").equals(fullName));
	}

	@Test
	public void resumptionTest() throws Exception
	{
		logon("AutoTest", "automated");

		List<String> names = new ArrayList<String>();
		for( int i = 0; i < 21; i++ )
		{
			String fullName = context.getFullName("OIA resume " + i);
			WizardPageTab wiz = new ContributePage(context).load().openWizard(COLLECTION);
			wiz.editbox(1, fullName);
			wiz.save().publish();
		}

		OAIPage newOai = new OAIPage(context);
		List<String> identifiers = newOai.listIdentifiers(FORMAT);
		assertTrue(identifiers.size() > 20);

		Map<String, String> results = newOai.listResults(FORMAT);
		Iterator<String> namesIterator = names.iterator();
		while( namesIterator.hasNext() )
		{
			String next = namesIterator.next();
			assertTrue(results.containsValue(next), "List '" + results.keySet() + "' doesnt contain '" + next
				+ "'.");
		}
		assertTrue(results.size() > 20);
	}

}
