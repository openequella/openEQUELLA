package com.tle.conversion.wmf;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class WMFHandleTable
{
	private static final Logger LOGGER = Logger.getLogger(WMFHandleTable.class);

	private Vector<Integer> handleTable;
	private Hashtable<Integer, MetaRecord> MRecordTable;

	public WMFHandleTable()
	{
		handleTable = new Vector<Integer>();
		this.MRecordTable = new Hashtable<Integer, MetaRecord>();
	}

	public MetaRecord selectObject(int index)
	{
		MetaRecord m;

		Integer i = new Integer(-1);
		try
		{
			i = handleTable.elementAt(index);
		}
		catch( StringIndexOutOfBoundsException e )
		{
			LOGGER.error("Error", e);
		}

		m = MRecordTable.get(i);
		return (m);
	}

	public void deleteObject(int index)
	{
		Integer i = handleTable.get(index);
		handleTable.setElementAt(new Integer(-1), index);
		MRecordTable.remove(i);

	}

	public void addObject(int recordValue, MetaRecord m)
	{
		int index;
		Integer h;
		Integer i;

		h = new Integer(recordValue);

		i = new Integer(-1); // -1

		if( handleTable.contains(i) )
		{ // if there is a free handle due to delete
			index = handleTable.indexOf(i); // get the index of the deleted
			// record
			handleTable.setElementAt(h, index); // set the new value
		}
		else
		{
			handleTable.addElement(h);
		}

		MRecordTable.put(h, m);
	}
}
