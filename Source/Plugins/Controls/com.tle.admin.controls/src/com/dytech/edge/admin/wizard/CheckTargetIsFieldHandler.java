package com.dytech.edge.admin.wizard;

import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SchemaNode;
import com.tle.admin.schema.TargetListener;

public class CheckTargetIsFieldHandler implements TargetListener
{
	private final SchemaModel schema;

	public CheckTargetIsFieldHandler(SchemaModel schema)
	{
		this.schema = schema;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.schema.TargetListener#targetAdded(java.lang.String)
	 */
	@Override
	public void targetAdded(String target)
	{
		SchemaNode node = schema.getNode(target);
		if( node == null )
		{
			System.err.println("Could not find schema node for target '" + target + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.schema.TargetListener#targetRemoved(java.lang.String
	 * )
	 */
	@Override
	public void targetRemoved(String target)
	{
		// We don't care about this event.
	}
}
