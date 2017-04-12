package com.tle.web.bulk.metadata.operations;

import java.util.List;
import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.web.bulk.metadata.model.Modification.ModificationKeys;

@BindFactory
public interface EditMetadataBulkFactory
{
	EditMetadataBulkOperation editMetada(@Assisted("nodes") List<List<String>> nodes,
		@Assisted("mods") List<Map<ModificationKeys, String>> mods);
}
