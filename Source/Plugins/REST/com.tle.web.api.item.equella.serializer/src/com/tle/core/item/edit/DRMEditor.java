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
