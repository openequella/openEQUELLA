package com.tle.core.services;

import java.util.Collection;

import com.dytech.edge.exceptions.QuotaExceededException;
import com.tle.beans.Institution;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.core.filesystem.StagingFile;

/**
 * @author Charles O'Farrell
 */
public interface QuotaService
{
	long checkQuotaAndReturnNewItemSize(Item item, StagingFile stagingFile1) throws QuotaExceededException;

	long getFileSize(FileHandle file);

	long getInstitutionalConsumption(Institution inst);

	Collection<Institution> getInstitutionsWithFilestoreLimits();

	boolean isInstitutionOverLimit(Institution inst);

	void refreshCache(Institution inst);
}
