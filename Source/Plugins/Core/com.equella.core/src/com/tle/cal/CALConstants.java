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

package com.tle.cal;

/*
 * @author jmagz
 * @author aholland
 */
@SuppressWarnings("nls")
public final class CALConstants
{
	public static final String CAL_HOLDING = "CAL_HOLDING";

	public static final String ENABLED = "com.tle.cal-Enabled";
	public static final String AGREEMENTFILE = "com.tle.cal-AgreementFile";
	public static final String ACTIVATIONERROR = "com.tle.cal-ActivationError";
	public static final String INACTIVEERROR = "com.tle.cal-InActiveError";
	public static final String HASAGREEMENT = "com.tle.cal-HasAgreement";
	public static final String HAS_RESTRICTIVE_VALIDATION = "com.tle.cal-restrictiveValidation";
	public static final String HAS_PERCOURSE_VALIDATION = "come.tle.cal-perCourseValidation";
	public static final String KEY_USE_CITATION_AS_NAME = "com.tle.cal-useCitationAsName";
	public static final String KEY_PERCENTAGE_REQUIREMENT = "com.tle.cal-percentageRequirement";

	public static final String CAL_COPYRIGHTSTATUS = "copyright";

	public static final String XML_CAL_ROOT = "item/copyright";
	public static final String XML_PORTIONS = "portions";
	public static final String XML_PORTION = XML_PORTIONS + "/portion";
	public static final String XML_AUTHORS = "authors";
	public static final String XML_AUTHOR = XML_AUTHORS + "/author";
	public static final String XML_TOPICS = "topics";
	public static final String XML_TOPIC = XML_TOPICS + "/topic";
	public static final String XML_SECTIONS = "sections";
	public static final String XML_SECTION = XML_SECTIONS + "/section";

	public static final String BOOK = "book";
	public static final String JOURNAL = "journal";

	public static final String VIEW_ACTIVATION_ITEM = "VIEW_ACTIVATION_ITEM";
	public static final String VIEW_ACTIVATION_ITEM_PFX = "ACLVA-";
	public static final String DEACTIVATE_ACTIVATION_ITEM = "DEACTIVATE_ACTIVATION_ITEM";
	public static final String DELETE_ACTIVATION_ITEM = "DELETE_ACTIVATION_ITEM";
	public static final String DELETE_ACTIVATION_ITEM_PFX = "ACLDA-";
	public static final String COPYRIGHT_ITEM = "COPYRIGHT_ITEM";

	public static final String CAL_INDEX_ID = "cal";

	public static final String ACTIVATION_TYPE = "cal";

	private CALConstants()
	{
		throw new Error();
	}
}
