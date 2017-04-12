package com.tle.common.search.searchset;

import java.util.List;

import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;

public interface SearchSet
{
	String getFreetextQuery();

	void setFreetextQuery(String query);

	List<ItemDefinitionScript> getItemDefs();

	void setItemDefs(List<ItemDefinitionScript> itemDefs);

	List<SchemaScript> getSchemas();

	void setSchemas(List<SchemaScript> schemas);

	// // The following are for hierarchical SearchSets ///////////////////////

	String getId();

	SearchSet getParent();

	boolean isInheritFreetext();

	void setInheritFreetext(boolean inheritFreetext);

	List<SchemaScript> getInheritedSchemas();

	void setInheritedSchemas(List<SchemaScript> schemas);

	List<ItemDefinitionScript> getInheritedItemDefs();

	void setInheritedItemDefs(List<ItemDefinitionScript> itemDefs);

	// // The following are for set virtualisation ////////////////////////////

	String getVirtualiserPluginId();

	void setVirtualiserPluginId(String pluginId);

	String getVirtualisationPath();

	void setVirtualisationPath(String path);

	// // Generic config storage //////////////////////////////////////////////

	String getAttribute(String key);

	void setAttribute(String key, String value);

	void removeAttribute(String key);
}
