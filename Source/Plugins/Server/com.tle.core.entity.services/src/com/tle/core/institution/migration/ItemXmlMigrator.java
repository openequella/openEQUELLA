package com.tle.core.institution.migration;

import com.dytech.devlib.PropBagEx;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.institution.convert.ConverterParams;

public interface ItemXmlMigrator
{
	void beforeMigrate(ConverterParams params, TemporaryFileHandle staging, SubTemporaryFile file) throws Exception;

	boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename) throws Exception;

	void afterMigrate(ConverterParams params, SubTemporaryFile file) throws Exception;
}
