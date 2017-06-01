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

package com.tle.web.scripting.advanced.types;

import java.io.Serializable;
import java.util.List;

/**
 * @author aholland
 */
public interface DrmSettingsScriptType extends Serializable
{
	boolean isAllowSummary();

	void setAllowSummary(boolean allowSummary);

	/**
	 * true if owner required to accept rights statement, otherwise false.
	 * 
	 * @return primitive boolean true or false
	 */
	boolean isOwnerMustAccept();

	/**
	 * Specify that the owner is (also) required to accept rights statement,
	 * otherwise false.
	 * 
	 * @param ownerMustAccept primitive boolean true or false
	 */
	void setOwnerMustAccept(boolean ownerMustAccept);

	/**
	 * true if user may view a preview of the item without having explicitly
	 * accepted rights statement, otherwise false.
	 * 
	 * @return primitive boolean true or false
	 */
	boolean isPreviewAllowed();

	/**
	 * Specify that user may view a preview of the item without having
	 * explicitly accepted rights statement.
	 * 
	 * @param previewAllowed primitive boolean true or false
	 */
	void setPreviewAllowed(boolean previewAllowed);

	/**
	 * true if users still required to accept the licence statement if the item
	 * is used in a composition.
	 * 
	 * @return primitive boolean true or false
	 */
	boolean isStudentsMustAcceptIfInCompilation();

	/**
	 * Specify that users still required to accept the licence statement if the
	 * item is used in a composition.
	 * 
	 * @param studentsMustAcceptIfInCompilation primitive boolean true or false
	 */
	void setStudentsMustAcceptIfInCompilation(boolean studentsMustAcceptIfInCompilation);

	/**
	 * Owners of the rights over item content
	 * 
	 * @return List<DrmPartyScriptType> owners of the rights of item content
	 * @see com.tle.web.scripting.advanced.types.DrmPartyScriptType
	 */
	List<DrmPartyScriptType> getContentOwners();

	void setContentOwners(List<DrmPartyScriptType> contentOwners);

	/**
	 * @param party A DrmPartyScriptType object obtained via
	 *            {@link com.tle.web.scripting.advanced.objects.DrmScriptObject#createPartyFromUserId(String)}
	 *            or
	 *            {@link com.tle.web.scripting.advanced.objects.DrmScriptObject#createParty(String, String)}
	 */
	void addContentOwner(DrmPartyScriptType party);

	List<String> getUsages();

	void setUsages(List<String> usages);

	/**
	 * @param usage One of: DISPLAY, PRINT, PLAY, EXECUTE, AGGREGATE, MODIFY,
	 *            EXCERPT, ANNOTATE
	 * @return true if the usage did not already exist and was added.
	 */
	boolean addUsage(String usage);

	/**
	 * @param usage One of: DISPLAY, PRINT, PLAY, EXECUTE, AGGREGATE, MODIFY,
	 *            EXCERPT, ANNOTATE
	 * @return true if the given usage was found and removed.
	 */
	boolean removeUsage(String usage);

	/**
	 * true if , otherwise false.
	 * 
	 * @return primitive boolean true or false
	 */
	boolean isAttributionOfOwnership();

	/**
	 * Specify that
	 * 
	 * @param attributionOfOwnership primitive boolean true or false
	 */
	void setAttributionOfOwnership(boolean attributionOfOwnership);

	/**
	 * true if , otherwise false.
	 * 
	 * @return primitive boolean true or false
	 */
	boolean isEnforceAttribution();

	/**
	 * Specify that
	 * 
	 * @param enforceAttribution primitive boolean true or false
	 */
	void setEnforceAttribution(boolean enforceAttribution);

	// Pair<Date, Date> getRestrictedToDateRange();

	// List<Triple<String, String, String>> getRestrictedToIpRanges();

	List<String> getRestrictedToRecipients();

	void setRestrictedToRecipients(List<String> restrictedToRecipients);

	/**
	 * true if , otherwise false.
	 * 
	 * @return primitive boolean true or false
	 */
	boolean isRestrictToSector();

	/**
	 * Specify that
	 * 
	 * @param restrictToSector primitive boolean true or false
	 */
	void setRestrictToSector(boolean restrictToSector);

	String getTermsOfAgreement();

	void setTermsOfAgreement(String termsOfAgreement);

	int getMaximumUsageCount();

	void setMaximumUsageCount(int maximumUsageCount);

	/**
	 * true if , otherwise false.
	 * 
	 * @return primitive boolean true or false
	 */
	boolean isHideLicencesFromOwner();

	/**
	 * Specify that
	 * 
	 * @param hideLicencesFromOwner primitive boolean true or false
	 */
	void setHideLicencesFromOwner(boolean hideLicencesFromOwner);

	/**
	 * true if , otherwise false.
	 * 
	 * @return primitive boolean true or false
	 */
	boolean isShowLicenceCount();

	/**
	 * Specify that
	 * 
	 * @param showLicenceCount primitive boolean true or false
	 */
	void setShowLicenceCount(boolean showLicenceCount);

	String getRequireAcceptanceFrom();

	void setRequireAcceptanceFrom(String requireAcceptanceFrom);
}