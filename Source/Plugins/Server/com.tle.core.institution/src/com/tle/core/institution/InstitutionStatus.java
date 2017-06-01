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

package com.tle.core.institution;

import java.io.Serializable;

import com.tle.beans.Institution;

public class InstitutionStatus implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The lower case version of these enums is mapped to i18n properties such
	 * as unlicensed.reason.wrongequellaversion etc, so must be kept in sync.
	 */
	public enum InvalidReason
	{
		EXPIRED, INVALID, HOSTNAME, COUNT, WRONGEQUELLAVERSION
	}

	private final Institution institution;
	private final InvalidReason invalidReason;
	private final long schemaId;

	public InstitutionStatus(Institution institution, long schemaId)
	{
		this(institution, schemaId, null);
	}

	public InstitutionStatus(Institution institution, long schemaId, InvalidReason reason)
	{
		this.institution = institution;
		this.invalidReason = reason;
		this.schemaId = schemaId;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public boolean isValid()
	{
		return invalidReason == null;
	}

	public InvalidReason getInvalidReason()
	{
		return invalidReason;
	}

	@Override
	public int hashCode()
	{
		return institution.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}
		if( !(obj instanceof InstitutionStatus) )
		{
			return false;
		}

		InstitutionStatus status2 = (InstitutionStatus) obj;
		return institution.equals(status2.institution);
	}

	public long getSchemaId()
	{
		return schemaId;
	}
}
