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

package com.tle.core.item.edit;

import java.util.Date;
import java.util.List;

import com.tle.beans.item.DrmSettings.Party;
import com.tle.beans.item.DrmSettings.Usage;
import com.tle.common.Triple;

public interface DRMEditor
{
	void editDrmPageUuid(String drmPageUuid);

	void editHideLicencesFromOwner(boolean val);

	void editShowLicenceCount(boolean val);

	void editAllowSummary(boolean val);

	void editOwnerMustAccept(boolean val);

	void editStudentsMustAcceptIfInCompilation(boolean val);

	void editPreviewAllowed(boolean val);

	void editAttributionOfOwnership(boolean val);

	void editEnforceAttribution(boolean val);

	void editContentOwners(List<Party> parties);

	void editUsages(List<Usage> usages);

	void editTermsOfAgreement(String terms);

	void editRequireAcceptanceFrom(String expr);

	void editNetworks(List<Triple<String, String, String>> networks);

	void editDateRange(Date startDate, Date endDate);

	void editUsersExpression(List<String> users);

	void editEducationalSector(boolean val);

	void editMaximumUsage(int val);

	void remove();

	// Not editable
	// private List<DrmAcceptanceBean> acceptances;
}
