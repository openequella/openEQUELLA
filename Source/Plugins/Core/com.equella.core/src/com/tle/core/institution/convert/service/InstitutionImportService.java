/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.institution.convert.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.plugin.registry.Extension;

import com.tle.beans.Institution;
import com.tle.common.NameValue;
import com.tle.common.beans.progress.ListProgressCallback;
import com.tle.common.filesystem.handle.ImportFile;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.plugins.PluginTracker;

public interface InstitutionImportService
{
	public enum ConvertType
	{
		DELETE, EXPORT, IMPORT, CLONE
	}

	Map<String, String> validate(Institution inst);

	Institution update(Institution institution);

	void delete(Institution institution, ListProgressCallback callback);

	void clone(long targetSchemaId, Institution newInstitution, long cloneFrom, ListProgressCallback callback,
		Set<String> conversions);

	String exportInstitution(Institution i, ListProgressCallback callback, Set<String> conversions);

	InstitutionInfo getInstitutionInfo(ImportFile staging);

	Institution importInstitution(ImportFile staging, long targetSchemaId, InstitutionInfo imported,
		ListProgressCallback callback);

	/**
	 * Simply deletes the staging file
	 * 
	 * @param staging
	 */
	void cancelImport(ImportFile staging);

	Collection<NameValue> getMatchingConversions(Collection<String> name);

	Set<String> getMatchingIds(Collection<String> values);

	List<String> getConverterTasks(ConvertType type, InstitutionInfo info);

	InstitutionInfo getInfoForCurrentInstitution();

	Set<String> getAllConversions();

	Set<String> convertToFlags(Set<String> conversions);

	Set<Extension> orderExtsByDependencies(PluginTracker<?> tracker, Collection<Extension> extensions);

	InstitutionInfo getInstitutionInfo(Institution institution);
}
