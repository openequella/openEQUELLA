package com.tle.core.remoterepo.parser.mods;

import java.util.Set;

import com.tle.core.fedsearch.GenericRecord;

/**
 * @author aholland
 */
public interface ModsRecord extends GenericRecord
{
	Set<String> getNotes();

	@Override
	String getPhysicalDescription();

	String getTypeOfResource();
}
