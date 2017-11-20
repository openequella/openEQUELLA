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

package com.tle.common.wizard.controls.universal.handlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * @author larry
 *
 */
public class LTISettings extends UniversalSettings
{
	@SuppressWarnings("nls")
	public static final String KEY_EXTTOOLS = "EXT_TOOLS";

	public LTISettings(CustomControl wrapped)
	{
		super(wrapped);
	}

	public LTISettings(UniversalSettings settings)
	{
		super(settings.getWrapped());
	}

	@SuppressWarnings("unchecked")
	public List<LTICustomised> getLTIs()
	{
		List<Pair<String, Map<String, String>>> persistedObj = (List<Pair<String, Map<String, String>>>) wrapped
			.getAttributes().get(KEY_EXTTOOLS);
		if( persistedObj == null )
		{
			return null;
		}
		List<LTICustomised> retlist = new ArrayList<LTICustomised>();
		for( Pair<String, Map<String, String>> pobj : persistedObj )
		{
			retlist.add(new LTICustomised(pobj.getFirst(), pobj.getSecond()));
		}
		return retlist;
	}

	public void setLTIs(List<LTICustomised> ltiCustomised)
	{
		if( Check.isEmpty(ltiCustomised) )
		{
			wrapped.getAttributes().remove(KEY_EXTTOOLS);
		}
		else
		{
			List<Pair<String, Map<String, String>>> persistObj = new ArrayList<Pair<String, Map<String, String>>>();
			for( LTICustomised ltiCust : ltiCustomised )
			{
				persistObj.add(new Pair<String, Map<String, String>>(ltiCust.getUuid(), ltiCust.getCustomParams()));
			}
			wrapped.getAttributes().put(KEY_EXTTOOLS, persistObj);
		}
	}

	/**
	 * On adding an LTI to an itemDefinition, we allow the collection designer
	 * to add custom parameters to the LTI, being of the form Name/Value
	 * 
	 * @author larry
	 */
	public static final class LTICustomised implements Serializable
	{
		private static final long serialVersionUID = 8809641651842931333L;

		private String uuid;
		private Map<String, String> customParams;

		public LTICustomised(String uuid, Map<String, String> customParams)
		{
			this.uuid = uuid;
			this.customParams = customParams;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public Map<String, String> getCustomParams()
		{
			return customParams/*
								 * != null ? customParams : new HashMap<String,
								 * String>()
								 */;
		}

		public void setCustomParams(Map<String, String> customParams)
		{
			this.customParams = customParams;
		}

		@Override
		public boolean equals(Object obj)
		{
			if( obj == null )
			{
				return false;
			}
			if( !(obj instanceof LTICustomised) )
			{
				return false;
			}
			LTICustomised rhs = (LTICustomised) obj;
			if( rhs == this )
			{
				return true;
			}
			return (this.uuid.equals(rhs.getUuid()) && this.getCustomParams().equals(rhs.getCustomParams()));
		}

		@Override
		public int hashCode()
		{
			int hash = 0;
			if( this.uuid != null )
			{
				hash += this.uuid.hashCode();
			}
			if( this.customParams != null )
			{
				hash += this.customParams.hashCode();
			}
			if( hash != 0 )
			{
				return hash;
			}
			return super.hashCode();
		}
	}
}

