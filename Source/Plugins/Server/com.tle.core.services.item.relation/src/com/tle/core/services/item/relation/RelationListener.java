package com.tle.core.services.item.relation;

import com.tle.beans.item.Relation;

public interface RelationListener
{
	void relationCreated(Relation relation);

	void relationDeleted(Relation relation);
}
