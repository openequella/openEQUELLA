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

package com.tle.web.viewitem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.DrmSettings.Party;
import com.tle.beans.item.DrmSettings.Usage;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class I18nDRM
{
	public static final String TLE_PREFIX = "tle:";

	private final DrmSettings rights;

	private transient List<Party> parties;
	private transient String permission1Cache;
	private transient String permission2Cache;

	public I18nDRM(DrmSettings rights)
	{
		this.rights = rights;
	}

	public String getPermissions(int type)
	{
		return (type == 1) ? getItemMayFreelyBeText() : getAdditionallyUserMayText();
	}

	private String getPermissionList(Map<Usage, String> permissionToDisplay)
	{
		StringBuilder text = new StringBuilder();

		List<Usage> usages = rights.getUsagesAsEnum();
		for( Iterator<Usage> iter = usages.iterator(); iter.hasNext(); )
		{
			Usage usage = iter.next();

			String displayString = permissionToDisplay.get(usage);
			if( displayString != null )
			{
				if( text.length() > 0 )
				{
					if( iter.hasNext() )
					{
						text.append(get("listseparator"));
						text.append(' ');
					}
					else
					{
						text.append(' ');
						text.append(get("andor"));
						text.append(' ');
					}
				}
				text.append(displayString);
			}
		}
		return text.toString();
	}

	public synchronized String getPermissions1List()
	{
		if( permission1Cache == null )
		{
			Map<Usage, String> m = new HashMap<Usage, String>();
			m.put(Usage.DISPLAY, get("permission.displayed"));
			m.put(Usage.PRINT, get("permission.printed"));
			m.put(Usage.PLAY, get("permission.played"));
			m.put(Usage.EXECUTE, get("permission.executed"));

			permission1Cache = getPermissionList(m);
		}
		return permission1Cache;
	}

	public synchronized String getPermissions2List()
	{
		if( permission2Cache == null )
		{
			Map<Usage, String> m = new HashMap<Usage, String>();
			m.put(Usage.EXCERPT, get("permission.removeexcerpts"));
			m.put(Usage.AGGREGATE, get("permission.aggregate"));
			m.put(Usage.ANNOTATE, get("permission.annotate"));
			m.put(Usage.MODIFY, get("permission.modify"));

			permission2Cache = getPermissionList(m);
			if( permission2Cache.length() > 0 )
			{
				permission2Cache += ' ' + get("xthisitem");
			}
		}
		return permission2Cache;
	}

	public boolean canViewSummary()
	{
		return rights.isAllowSummary();
	}

	public boolean canPreview()
	{
		return rights.isPreviewAllowed();
	}

	public int partyCount()
	{
		return getParties().size();
	}

	public Party getParty(int index)
	{
		return getParties().get(index);
	}

	public synchronized List<Party> getParties()
	{
		if( parties == null )
		{
			parties = new ArrayList<Party>();

			List<Party> contentOwners = rights.getContentOwners();
			if( contentOwners != null )
			{
				parties.addAll(contentOwners);
			}
		}
		return parties;
	}

	public boolean isHasPermissions1()
	{
		return getPermissions1List().length() > 0;
	}

	public boolean isHasPermissions2()
	{
		return getPermissions2List().length() > 0;
	}

	public boolean isAttribution()
	{
		return rights.isAttributionOfOwnership();
	}

	public boolean isUseEducation()
	{
		return rights.isRestrictToSector();
	}

	public String getTerms()
	{
		return rights.getTermsOfAgreement();
	}

	public String getTermsAsHtml()
	{
		String t = getTerms();
		// If it doesn't look like HTML...
		if( Check.isEmpty(t) || !t.contains("<") )
		{
			t = t.replaceAll("\n", "<br>");
		}
		return t;
	}

	public boolean isEmbedAttribution()
	{
		return rights.isEnforceAttribution();
	}

	public String getItemMayFreelyBeText()
	{
		return get("itemfreely");
	}

	public String getAdditionallyUserMayText()
	{
		return get("additionallyusermay");
	}

	public String getEducationSectorText()
	{
		return get("educationsector");
	}

	public String getAttributeOwnersText()
	{
		return get("attributeowners");
	}

	public String getTermsText()
	{
		return get("mustagree");
	}

	private String get(String keyPart)
	{
		return CurrentLocale.get("com.tle.web.viewitem.summary.drm." + keyPart);
	}
}
