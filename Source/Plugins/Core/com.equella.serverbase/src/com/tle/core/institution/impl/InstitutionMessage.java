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

package com.tle.core.institution.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.beans.Institution;
import com.tle.core.institution.InstitutionStatus;
import com.tle.core.institution.InstitutionValidationError;

public class InstitutionMessage implements Serializable
{
	public enum Type
	{
		SETENABLED, EDIT, DELETE, CREATE, SCHEMAS, VALIDATE
	}

	private static final long serialVersionUID = 1L;

	private Type type;

	public InstitutionMessage(Type type)
	{
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public static abstract class SimpleInstitutionMessage extends InstitutionMessage
	{
		private static final long serialVersionUID = 1L;
		private final Institution institution;

		public SimpleInstitutionMessage(Type type, Institution institution)
		{
			super(type);
			this.institution = institution;
		}

		public Institution getInstitution()
		{
			return institution;
		}
	}

	public static class EditInstitutionMessage extends SimpleInstitutionMessage
	{
		private static final long serialVersionUID = 1L;

		public EditInstitutionMessage(Institution institution)
		{
			super(Type.EDIT, institution);
		}
	}

	public static class ValidateInstitutionMessage extends SimpleInstitutionMessage
	{
		private static final long serialVersionUID = 1L;

		public ValidateInstitutionMessage(Institution institution)
		{
			super(Type.VALIDATE, institution);
		}
	}

	public static class DeleteInstitutionMessage extends SimpleInstitutionMessage
	{
		private static final long serialVersionUID = 1L;

		public DeleteInstitutionMessage(Institution institution)
		{
			super(Type.DELETE, institution);
		}
	}

	public static class CreateInstitutionMessage extends SimpleInstitutionMessage
	{
		private static final long serialVersionUID = 1L;
		private final long schemaId;

		public CreateInstitutionMessage(Institution institution, long schemaId)
		{
			super(Type.CREATE, institution);
			this.schemaId = schemaId;
		}

		public long getSchemaId()
		{
			return schemaId;
		}
	}

	public static class SetEnabledMessage extends InstitutionMessage
	{
		private static final long serialVersionUID = 1L;
		private final long institutionId;
		private final boolean enabled;

		public SetEnabledMessage(long instId, boolean enabled)
		{
			super(Type.SETENABLED);
			this.institutionId = instId;
			this.enabled = enabled;
		}

		public long getInstitutionId()
		{
			return institutionId;
		}

		public boolean isEnabled()
		{
			return enabled;
		}
	}

	public static class SchemaMessage extends InstitutionMessage
	{
		private static final long serialVersionUID = 1L;
		private final Collection<Long> schemaIds;
		private final boolean available;

		public SchemaMessage(Collection<Long> schemaIds, boolean available)
		{
			super(Type.SCHEMAS);
			this.schemaIds = schemaIds;
			this.available = available;
		}

		public Collection<Long> getSchemaIds()
		{
			return schemaIds;
		}

		public boolean isAvailable()
		{
			return available;
		}
	}

	public static class InstitutionMessageResponse implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Map<Long, InstitutionStatus> institutionMap;
		private Throwable error;
		private Institution institution;
		private List<InstitutionValidationError> validationErrors;

		public Map<Long, InstitutionStatus> getInstitutionMap()
		{
			return institutionMap;
		}

		public void setInstitutionMap(Map<Long, InstitutionStatus> institutionMap)
		{
			this.institutionMap = institutionMap;
		}

		public Throwable getError()
		{
			return error;
		}

		public void setError(Throwable error)
		{
			this.error = error;
		}

		public Institution getInstitution()
		{
			return institution;
		}

		public void setInstitution(Institution institution)
		{
			this.institution = institution;
		}

		public List<InstitutionValidationError> getValidationErrors()
		{
			return validationErrors;
		}

		public void setValidationErrors(List<InstitutionValidationError> validationErrors)
		{
			this.validationErrors = validationErrors;
		}
	}
}
