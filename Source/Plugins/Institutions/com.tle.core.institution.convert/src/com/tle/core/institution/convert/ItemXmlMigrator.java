package com.tle.core.institution.convert;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;

public interface ItemXmlMigrator
{
	void beforeMigrate(ConverterParams params, TemporaryFileHandle staging, SubTemporaryFile file) throws Exception;

	boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename) throws Exception;

	void afterMigrate(ConverterParams params, SubTemporaryFile file) throws Exception;
}
