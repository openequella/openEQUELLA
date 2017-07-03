package com.tle.core.quota.service;

import java.util.Collection;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.quota.exception.QuotaExceededException;

/**
 * @author Charles O'Farrell
 */
@NonNullByDefault
public interface QuotaService
{
	long checkQuotaAndReturnNewItemSize(Item item, StagingFile stagingFile1) throws QuotaExceededException;

	long getFileSize(FileHandle file);

	long getInstitutionalConsumption(Institution inst);

	Collection<Institution> getInstitutionsWithFilestoreLimits();

	boolean isInstitutionOverLimit(Institution inst);

	void refreshCache(Institution inst);
}
