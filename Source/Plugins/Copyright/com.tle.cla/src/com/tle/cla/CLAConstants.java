package com.tle.cla;

/*
 * @author jmagz
 * @author aholland
 */
@SuppressWarnings("nls")
public final class CLAConstants
{
	public static final String HOLDING = "CLA_HOLDING";

	public static final String ENABLED = "com.tle.cla-Enabled";
	public static final String AGREEMENTFILE = "com.tle.cla-AgreementFile";
	public static final String ACTIVATIONERROR = "com.tle.cla-ActivationError";
	public static final String INACTIVEERROR = "com.tle.cla-InActiveError";
	public static final String HASAGREEMENT = "com.tle.cla-HasAgreement";

	public static final String COPYRIGHTSTATUS = "copyright";
	public static final String KEY_SELECTREQUEST = "ActivateRequest";

	public static final String XML_CLA_ROOT = "item/copyright";
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

	public static final String TYPE_BOOK = "book";
	public static final String TYPE_JOURNAL = "journal";
	public static final String TYPE_CONFERENCE = "conference";
	public static final String TYPE_JUDICIAL_PROCEEDINGS = "judicial";
	public static final String TYPE_STORIES = "stories";
	public static final String COPYRIGHT_STATUS = "copyright";
	public static final int PERCENTAGE = 5;
	public static final int MAX_STORY_PAGES = 10;

	public static final String VIEW_ACTIVATION_ITEM = "VIEW_ACTIVATION_ITEM";
	public static final String VIEW_ACTIVATION_ITEM_PFX = "ACLVA-";
	public static final String DEACTIVATE_ACTIVATION_ITEM = "DEACTIVATE_ACTIVATION_ITEM";
	public static final String DELETE_ACTIVATION_ITEM = "DELETE_ACTIVATION_ITEM";
	public static final String DELETE_ACTIVATION_ITEM_PFX = "ACLDA-";
	public static final String COPYRIGHT_ITEM = "COPYRIGHT_ITEM";

	public static final String INDEX_ID = "cla";
	public static final String ACTIVATION_TYPE = "cla";

	private CLAConstants()
	{
		throw new Error();
	}
}
