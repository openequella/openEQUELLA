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
