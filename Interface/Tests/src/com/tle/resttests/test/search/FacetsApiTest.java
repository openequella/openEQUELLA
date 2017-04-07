package com.tle.resttests.test.search;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.entity.Schemas;
import com.tle.json.requests.CollectionRequests;
import com.tle.json.requests.FacetRequests;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.SchemaRequests;
import com.tle.json.requests.SearchRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;

@Test(groups = "eps")
public class FacetsApiTest extends AbstractEntityCreatorTest
{
	// private ItemRequests items;
	private SchemaRequests schemas;
	private ItemRequests items;
	private SearchRequests searches;
	private FacetRequests facets;
	private FacetRequests facetsGuest;
	private CollectionRequests collections;
	private Map<String, ItemId> itemsForIndex = Maps.newHashMap();

	// @formatter:off
	private static final String[][] BOOKS = {
		//My extensive knowledge of literature on display here
		{"The hobbit", "fantasy", "1970's", "JRR Tolkien", "$50", "fiction", "popular, film, fantasy"},
		{"The Bible", "religous", "historical", "", "$30", "religous text", "christian, religous text"},
		{"The Quran", "religous", "historical", "", "$50", "religous text", "muslim, religous text"},
		{"Harry Potter", "fantasy", "1990's", "JK Rowling", "$50", "fiction", "popular, pre-teen, trash"},
		{"Twilight", "drama", "2000's", "?", "$30", "fiction", "popular, pre-teen, trash, rpatz"},
		{"Fifty shades of grey", "drama", "2000's", "?", "$50", "fiction", "popular, pre-teen, trash"}
	};
	// @formatter:on

	@Override
	public void customisePageContext()
	{
		RequestsBuilder builder = builder().user(RestTestConstants.USERID_AUTOTEST);
		items = builder.items();
		collections = builder.collections();
		schemas = builder.schemas();
		searches = builder.searches();
		facets = builder.facets();
		facetsGuest = builder().facets();
	}

	@DataProvider(name = "books")
	public Object[][] getBooks()
	{
		return BOOKS;
	}

	@Test(dataProvider = "books")
	public void contributeBook(String name, String genre, String yearRange, String author, String price,
		String bookClass, String tags) throws IOException
	{
		PropBagEx xml = new PropBagEx();
		String collection = "0ef2af13-7534-42d1-a3b7-a73145904d93";

		xml.setNode("/item/name", name);
		xml.setNode("/item/genre", genre);
		xml.setNode("/item/publish_history/written", yearRange);
		xml.setNode("/item/author/full_name", author);
		xml.setNode("/item/info/book_class", bookClass);
		xml.setNode("/item/store/price", price);

		for( String tag : tags.split(", ") )
		{
			xml.createNode("/item/tags/tag", tag);
		}

		ObjectNode itemJson = Items.jsonXml(collection, xml.toString());
		ObjectNode item = items.create(itemJson, 45);
		itemsForIndex.put(name, items.getId(item));
		if( name.equals(BOOKS[BOOKS.length - 1][0]) ) // Last one
		{
			for( Entry<String, ItemId> entry : itemsForIndex.entrySet() )
			{
				searches.waitForIndex(entry.getValue(), entry.getKey());
			}
		}
	}

	@Test(dependsOnMethods = "contributeBook")
	public void oneDeep() /* snigger */throws Exception
	{
		List<FacetResult> genres = doFacetSearch("/item/genre", null, false, true);
		FacetResult fantasyGenre = genres.get(1);
		assertEquals(fantasyGenre.term, "fantasy");
		assertEquals(fantasyGenre.count, 2);
		FacetResult dramaGenre = genres.get(2);
		assertEquals(dramaGenre.term, "drama");
		assertEquals(dramaGenre.count, 2);
		assertEquals(genres.size(), 3);
	}

	@Test(dependsOnMethods = "contributeBook")
	public void securityFilter() throws Exception
	{
		List<FacetResult> genres = doFacetSearch("/item/genre", null, false, false);
		assertEquals(genres.size(), 0, "Guest shouldn't be able to see any of these items");
	}

	@Test(dependsOnMethods = "contributeBook")
	public void twoDeep() throws Exception
	{
		List<FacetResult> bookClasses = doFacetSearch("/item/info/book_class,/item/store/price", null, false, true);
		FacetResult religousClass = bookClasses.get(1);
		assertEquals(religousClass.term, "religous text");
		assertEquals(religousClass.count, 2);
		assertEquals(religousClass.innerFacets.get(0).term, "$50");
		assertEquals(religousClass.innerFacets.get(1).term, "$30");
		assertEquals(religousClass.innerFacets.get(0).count, 1);
		assertEquals(religousClass.innerFacets.get(1).count, 1);

		FacetResult fictionClass = bookClasses.get(0);
		assertEquals(fictionClass.term, "fiction");
		assertEquals(fictionClass.count, 4);
		assertEquals(fictionClass.innerFacets.get(0).term, "$50");
		assertEquals(fictionClass.innerFacets.get(1).term, "$30");
		assertEquals(fictionClass.innerFacets.get(0).count, 3);
		assertEquals(fictionClass.innerFacets.get(1).count, 1);
	}

	// Re-enable these tests once things actually work
	@Test(dependsOnMethods = "contributeBook", enabled = false)
	public void getItems() throws Exception
	{
		List<FacetResult> dates = doFacetSearch("/item/publish_history/written", null, false, true);
		for( FacetResult date : dates )
		{
			assertNull(date.items);
		}
		dates = doFacetSearch("/item/publish_history/written", null, true, true);
		for( FacetResult date : dates )
		{
			assertNotNull(date.items);
		}
		// Historical
		assertEquals(dates.get(3).items.size(), 2);
		assertEquals(dates.get(3).items.get(0).uuid, itemsForIndex.get("The Quran"));
	}

	@Test(dependsOnMethods = "contributeBook")
	public void testMulti() throws Exception
	{
		List<FacetResult> tags = doFacetSearch("/item/tags/tag", null, false, true);

		assertEquals(tags.size(), 9);
		assertEquals(tags.get(0).term, "popular");
		assertEquals(tags.get(0).count, 4);
	}

	public void setupBranches() throws Exception
	{
		String collection = "0ef2af13-7534-42d1-a3b7-a73145904d93";
		PropBagEx xml = new PropBagEx();

		xml.setNode("/item/name", "branchLevelTest");
		PropBagEx pet1 = new PropBagEx();
		pet1.setNode("/pet/name", "Bandit");
		pet1.setNode("/pet/species", "Dog");
		PropBagEx pet2 = new PropBagEx();
		pet2.setNode("/pet/name", "Squid");
		pet2.setNode("/pet/species", "Sheep");
		PropBagEx pet3 = new PropBagEx();
		pet3.setNode("/pet/name", "Lady");
		pet3.setNode("/pet/species", "Dog");
		PropBagEx author = new PropBagEx();
		author.setNode("full_name", "branchLevelTestAuthor");
		author.appendChildren("/pets", pet1);
		author.appendChildren("/pets", pet2);
		author.appendChildren("/pets", pet3);

		xml.appendChildren("/item/author", author);

		ObjectNode itemJson = Items.jsonXml(collection, xml.toString());
		ObjectNode item = items.create(itemJson, 45);

		xml = new PropBagEx();

		xml.setNode("/item/name", "branchLevelTest 2");
		pet1 = new PropBagEx();
		pet1.setNode("/pet/species", "Dog");
		pet1.setNode("/pet/name", "Lassie");
		author = new PropBagEx();
		author.setNode("full_name", "branchLevelTestAuthor2");
		author.appendChildren("/pets", pet1);

		xml.appendChildren("/item/author", author);

		itemJson = Items.jsonXml(collection, xml.toString());
		ObjectNode item2 = items.create(itemJson, 45);

		searches.waitForIndex(items.getId(item), "branchLevelTest");
		searches.waitForIndex(items.getId(item2), "branchLevelTest 2");
	}

	/*
	 * This is the ultimate test, and the most difficult part of facets to get
	 * right, When doing a three deep facet where the nodes are based at
	 * different levels of the xml tree, the filters need to be scoped so that
	 * they apply only at that level of the tree.
	 */
	// Re-enable these tests once things actually work
	@Test(enabled = false)
	public void branchLevel() throws Exception
	{
		setupBranches();

		List<FacetResult> results = doFacetSearch(
			"/item/author/full_name,/item/author/pets/pet/species,/item/author/pets/pet/name", null, false, true);
		assertEquals(results.size(), 2);
		assertEquals(results.get(0).term, "branchLevelTestAuthor");
		List<FacetResult> pets = results.get(1).innerFacets;
		assertEquals(pets.size(), 2);
		assertEquals(pets.get(1).term, "Dog");
		assertEquals(pets.get(1).innerFacets.get(0).term, "Bandit");
		assertEquals(pets.get(1).innerFacets.size(), 1);
		// If it was broken 'Squid' would also be found under 'Dog', because
		// the species would be found under the same /item/author
		// It could also find all the dog names under /authorname1/dog instead
		// of just the ones owned by that author
	}

	/*
	 * The indexing for every schema is mashed together into the one map, with
	 * schemas sharing fields wherever the paths are identical, this has the
	 * potential to cause problems when a field that should be nested shares a
	 * path with one that shouldn't, hopefully this test will pick up any
	 * problems
	 */
	@Test
	public void nestedIndexingTest() throws Exception
	{
		final String ITEM_NESTED = context.getFullName("Nested");
		final String ITEM_NON_NESTED = context.getFullName("Not Nested");

		// Setup schemas and collections (1 nested & 1 not)
		ObjectNode schema = Schemas.json(context.getFullName("non nested schema"), "/item/name", "/item/description");
		ObjectNode itemNode = schema.with("definition").with("xml").with("item");

		itemNode.with("name").put("_indexed", true);
		itemNode.with("description").put("_indexed", true);
		itemNode.with("name").put("_field", true);
		itemNode.with("description").put("_field", true);

		ObjectNode nestedField = itemNode.with("nestornot");
		nestedField.with("field1").put("_indexed", true);
		nestedField.with("field2").put("_indexed", true);
		nestedField.with("field1").put("_field", true);
		nestedField.with("field2").put("_field", true);

		String nonNestedUuid = schemas.getId(schemas.create(schema));
		nestedField.put("_nested", true);
		String nestedUuid = schemas.getId(schemas.create(schema));

		String nestedCollection = collections
			.getId(collections.create(CollectionJson.json("nested", nestedUuid, null)));
		String nonNestedCollection = collections.getId(collections.create(CollectionJson.json("not nested",
			nonNestedUuid, null)));

		//Put an item in each one, same xml
		PropBagEx xml = new PropBagEx();
		xml.setNode("/item/name", ITEM_NESTED);
		PropBagEx nest1 = new PropBagEx();
		nest1.setNode("/nestornot/field1", "value1");
		nest1.setNode("/nestornot/field2", "value2");
		PropBagEx nest2 = new PropBagEx();
		nest2.setNode("/nestornot/field1", "value3");
		nest2.setNode("/nestornot/field2", "value4");
		xml.appendChildren("/item", nest1);
		xml.appendChildren("/item", nest2);

		ItemId nested = items.getId(items.create(Items.jsonXml(nestedCollection, xml.toString()), 45));
		xml.setNode("/item/name", ITEM_NON_NESTED);
		ItemId notNested = items.getId(items.create(Items.jsonXml(nonNestedCollection, xml.toString()), 45));

		searches.waitForIndex(nested, ITEM_NESTED);
		searches.waitForIndex(notNested, ITEM_NON_NESTED);

		List<FacetResult> nonNestedResult = doFacetSearch("/item/nestornot/field1,/item/nestornot/field2", null, false,
			true, "\"" + ITEM_NON_NESTED + "\"");
		Assert.assertEquals(nonNestedResult.size(), 2);
		Assert.assertEquals(nonNestedResult.get(0).term, "value3");
		Assert.assertEquals(nonNestedResult.get(0).count, 1);
		Assert.assertEquals(nonNestedResult.get(0).innerFacets.get(0).term, "value2");
		Assert.assertEquals(nonNestedResult.get(0).innerFacets.get(0).count, 1);
		// Assert.assertEquals(nonNestedResult.get(0).innerFacets.get(1).term,
		// "value4");
		// Assert.assertEquals(nonNestedResult.get(0).innerFacets.get(1).count,
		// 1);

		// Will be the same
		List<FacetResult> nestedResult = doFacetSearch("/item/nestornot/field1,/item/nestornot/field2", null, false,
			true, "\"" + ITEM_NESTED + "\"");
		Assert.assertEquals(nestedResult.size(), 2);
		Assert.assertEquals(nestedResult.get(0).term, "value3");
		Assert.assertEquals(nestedResult.get(0).count, 1);
		Assert.assertEquals(nestedResult.get(0).innerFacets.get(0).term, "value2");
		Assert.assertEquals(nestedResult.get(0).innerFacets.get(0).count, 1);
		// Assert.assertEquals(nestedResult.get(0).innerFacets.get(1).term,
		// "value4");
		// Assert.assertEquals(nestedResult.get(0).innerFacets.get(1).count, 1);

		// Should change
		nestedResult = doFacetSearch("/item/nestornot/field1,/item/nestornot/field2", "/item/nestornot", false, true,
			"\"" + ITEM_NESTED + "\"");
		Assert.assertEquals(nestedResult.size(), 2);
		Assert.assertEquals(nestedResult.get(0).term, "value3");
		Assert.assertEquals(nestedResult.get(0).count, 1);
		Assert.assertEquals(nestedResult.get(0).innerFacets.size(), 1);
		Assert.assertEquals(nestedResult.get(0).innerFacets.get(0).term, "value4");
		Assert.assertEquals(nestedResult.get(0).innerFacets.get(0).count, 1);
		Assert.assertEquals(nestedResult.get(1).term, "value1");
		Assert.assertEquals(nestedResult.get(1).count, 1);
		Assert.assertEquals(nestedResult.get(1).innerFacets.size(), 1);
		Assert.assertEquals(nestedResult.get(1).innerFacets.get(0).term, "value2");
		Assert.assertEquals(nestedResult.get(1).innerFacets.get(0).count, 1);

		// Both items
		nestedResult = doFacetSearch("/item/nestornot/field1,/item/nestornot/field2", "/item/nestornot", false, true,
			null);
	}

	public List<FacetResult> doFacetSearch(String nodes, String nest, boolean getItems, boolean auth) throws Exception
	{
		return doFacetSearch(nodes, nest, getItems, auth, null);
	}

	public List<FacetResult> doFacetSearch(String nodes, String nest, boolean getItems, boolean auth, String query)
		throws Exception
	{
		FacetRequests requests = auth ? facets : facetsGuest;
		ObjectNode results = requests.search(nodes, nest, getItems, query);
		return convertResults(results.get("results"));
	}

	private List<FacetResult> convertResults(JsonNode json)
	{
		List<FacetResult> allResults = Lists.newArrayList();
		for( JsonNode resultJson : json )
		{
			allResults.add(new FacetResult(resultJson));
		}
		return allResults;
	}

	private class FacetResult
	{
		public String term;
		public int count;
		public List<FacetResult> innerFacets;
		public List<ItemResult> items;

		public FacetResult(JsonNode result)
		{
			term = result.get("term").asText();
			count = result.get("count").asInt();

			JsonNode inner = result.get("innerFacets");
			if( inner != null )
			{
				innerFacets = convertResults(inner);
			}
			JsonNode itemsJson = result.get("items");
			if( itemsJson == null )
			{
				return;
			}
			items = Lists.newArrayList();

			for( JsonNode itemNode : itemsJson )
			{
				items.add(new ItemResult(itemNode));
			}
		}

		@Override
		public String toString()
		{
			String to = term + ": " + count;
			if( innerFacets != null )
			{
				to += innerFacets.toString();
			}
			return to;
		}
	}

	private class ItemResult
	{
		public String uuid;
		@SuppressWarnings("unused")
		public int version;

		public ItemResult(JsonNode item)
		{
			uuid = item.get("uuid").asText();
			version = item.get("version").asInt();
		}
	}

	@Override
	public String getDefaultUser()
	{
		// Guest
		return null;
	}
}
