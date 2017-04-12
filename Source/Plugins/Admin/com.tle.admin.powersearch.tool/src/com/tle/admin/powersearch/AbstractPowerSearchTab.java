/*
 * Created on Oct 7, 2005
 */

package com.tle.admin.powersearch;

import com.dytech.gui.Changeable;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.PowerSearch;

public abstract class AbstractPowerSearchTab extends BaseEntityTab<PowerSearch>
{
	protected Changeable parentChangeDetector;
	protected SchemaModel schema;

	public void setSchemaModel(SchemaModel schema)
	{
		this.schema = schema;
	}

	public void setParentChangeDetector(Changeable parentChangeDetector)
	{
		this.parentChangeDetector = parentChangeDetector;
	}
}
