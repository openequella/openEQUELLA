package com.tle.core.collection.extension;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public interface CollectionSaveExtension
{
	void collectionSaved(@Nullable ItemDefinition oldCollection, ItemDefinition newCollection);
}
