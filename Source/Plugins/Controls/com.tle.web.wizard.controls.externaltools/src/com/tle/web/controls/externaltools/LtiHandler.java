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

package com.tle.web.controls.externaltools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.web.controls.externaltools.LtiHandler.LtiHandlerModel;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.BasicAbstractAttachmentHandler;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * @author larry
 */
@Bind
@NonNullByDefault
public class LtiHandler extends BasicAbstractAttachmentHandler<LtiHandlerModel>
{
	// rather than null as UUID for manually input values
	public static final String AUTOMATIC_UUID = ExternalToolConstants.AUTOMATIC_UUID;

	@PlugKey("handlers.lti.select.automatic")
	private static String AUTOMATIC;

	@PlugKey("externaltoolresource.details.mimetype")
	private static String FALLBACK_DESCR;

	@PlugKey("handlers.lti.name")
	private static Label NAME_LABEL;
	@PlugKey("handlers.lti.description")
	private static Label DESCRIPTION_LABEL;

	@PlugKey("handlers.lti.add.title")
	private static Label TITLE_ADD_LABEL;
	@PlugKey("handlers.lti.edit.title")
	private static Label TITLE_EDIT_LABEL;

	@PlugKey("handlers.lti.error.invalid")
	private static Label LABEL_ERROR_NOLAUNCHURL;

	@PlugKey("handlers.lti.warning.unmatched")
	private static Label LABEL_ALT_MISMATCH_URL;
	@PlugKey("handlers.lti.error.badurl")
	private static Label LABEL_ERROR_INVALID;

	@Inject
	private ExternalToolsService externalToolService;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	private Map<ExternalTool, String> enabledExternalToolMap;

	@Component(name = "ltisel")
	private SingleSelectionList<NameValue> ltiSelector;

	@Component
	private TextField launchUrl;

	@Component
	private Div warningDiv;
	@SuppressWarnings("nls")
	private static final String WARNING_DIV = "warningDiv";

	@SuppressWarnings("nls")
	private final String warningPngPath = ResourcesService.getResourceHelper(LtiHandler.class)
		.url("images/warning.png");
	@SuppressWarnings("nls")
	private static final IncludeFile INCLUDE_FILE = new IncludeFile(ResourcesService
		.getResourceHelper(LtiHandler.class).url("js/updateBaseUrlMatches.js"));

	/**
	 * The method to evaluate if the text in the user-entered launchUrl field
	 * matches the (non-automatic) selection in the LTI selector, and renders a
	 * warning if mismatch identified.
	 */
	@SuppressWarnings("nls")
	private static final JSCallable FUNC_UPDATE_BASE_MATCH = new ExternallyDefinedFunction("updateBaseUrlMatches",
		INCLUDE_FILE);
	/**
	 * A javascript timer wrapper for the above method, so that when entering
	 * keystrokes in user-time, there is a discrete pause (3 seconds) before
	 * invoking the match.
	 */
	@SuppressWarnings("nls")
	private static final JSCallable TIMER_UPDATE_BASE_MATCH = new ExternallyDefinedFunction(
		"timerUpdateBaseUrlMatches", INCLUDE_FILE);

	@Component
	private TextField consumerKey;
	@Component
	private TextField sharedSecret;
	@Component
	private TextField customParams;

	@Component
	private TextField iconUrl;

	@PlugKey("handlers.lti.privacy.default")
	@Component
	private Checkbox useDefaultPrivacy;
	@PlugKey("handlers.lti.sharename")
	@Component
	private Checkbox shareName;
	@PlugKey("handlers.lti.shareemail")
	@Component
	private Checkbox shareEmail;

	// Probably don't need an explicit TagState for the icon renderer ...
	@SuppressWarnings("nls")
	private static final String URL_MISMATCH_WARNING_TAG_STATE = "urlmismatchwarningtagstate";

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(NAME_LABEL, DESCRIPTION_LABEL);
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		if( attachment instanceof CustomAttachment )
		{
			CustomAttachment ca = (CustomAttachment) attachment;
			return ExternalToolConstants.CUSTOM_ATTACHMENT_TYPE.equals(ca.getType());
		}
		return false;
	}

	@Override
	@SuppressWarnings("nls")
	public String getHandlerId()
	{
		return "ltiHandler";
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		enabledExternalToolMap = new HashMap<ExternalTool, String>();
		for( ExternalTool externalTool : externalToolService.enumerateEnabled() )
		{
			enabledExternalToolMap.put(externalTool, LangUtils.getString(externalTool.getName()));
		}

		List<NameValue> displayNamesWithUuids = new ArrayList<NameValue>(enabledExternalToolMap.size());
		for( ExternalTool lti : enabledExternalToolMap.keySet() )
		{
			displayNamesWithUuids.add(new NameValue(enabledExternalToolMap.get(lti), lti.getUuid()));
		}
		// Case insensitive
		Collections.sort(displayNamesWithUuids, new NumberStringComparator<NameValue>()
		{
			@Override
			public String convertToString(NameValue nv)
			{
				return nv.getName();
			}
		});

		// Put the automatic selection option at the head of the list
		NameValue defaultNameValue = automaticLtiSelection();
		displayNamesWithUuids.add(0, defaultNameValue);
		ltiSelector.setListModel(new SimpleHtmlListModel<NameValue>(displayNamesWithUuids));
		setupMismatchEval();

		useDefaultPrivacy.addClickStatements(
			new OverrideHandler(shareEmail.createDisableFunction(), useDefaultPrivacy.createGetExpression()),
			new OverrideHandler(shareName.createDisableFunction(), useDefaultPrivacy.createGetExpression()));
	}

	@Override
	public void createNew(SectionInfo info)
	{
		super.createNew(info);
		clearAllInputControls(info);
	}

	@Override
	protected List<Attachment> createAttachments(SectionInfo info)
	{
		CustomAttachment ltiAttachment = new CustomAttachment();
		ltiAttachment.setType(ExternalToolConstants.CUSTOM_ATTACHMENT_TYPE);

		// If non-null, becomes the EXTERNAL_TOOL_UUID value in the data map
		NameValue selectedValue = ltiSelector.getSelectedValue(info);
		ExternalTool lti = null;

		if( !automaticLtiSelection().equals(selectedValue) )
		{
			String selectedUuid = selectedValue.getValue();
			lti = externalToolService.getByUuid(selectedUuid);
		}
		String enteredUrl = launchUrl.getValue(info);
		populateAttachmentFromControls(info, ltiAttachment, enteredUrl, lti);
		// keep the model in step with controls
		LtiHandlerModel model = getModel(info);
		model.setCurrentLtiSelection(selectedValue);
		model.setEnteredLaunchUrl(enteredUrl);

		return Collections.singletonList((Attachment) ltiAttachment);
	}

	@Override
	protected boolean validateAddPage(SectionInfo info)
	{
		getModel(info).clearErrors();
		return super.validateAddPage(info) && validateUrlFields(info);
	}

	/**
	 * With the same controls as the add page, we apply the same validation (as
	 * well as the edit page's additional validation of display link.
	 */
	@Override
	protected boolean validateDetailsPage(SectionInfo info)
	{
		getModel(info).clearErrors();
		return super.validateDetailsPage(info) && this.validateAddPage(info);
	}

	@Override
	protected void saveDetailsToAttachment(SectionInfo info, Attachment attachment)
	{
		super.saveDetailsToAttachment(info, attachment);
		String enteredUrl = launchUrl.getValue(info);
		ExternalTool lti = null;
		// If the selector's not on automatic, get the ExternalTool
		if( !ltiSelector.getSelectedValue(info).equals(automaticLtiSelection()) )
		{
			lti = externalToolService.getByUuid(ltiSelector.getSelectedValue(info).getValue());
		}

		populateAttachmentFromControls(info, (CustomAttachment) attachment, enteredUrl, lti);
	}

	/**
	 * The lti-edit screen repeats the controls of the lti-add screen, with the
	 * addition of a text box requiring the user to provide/edit the display
	 * name of the link..
	 */
	@Override
	@SuppressWarnings("nls")
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		final Attachment attachment = getDetailsAttachment(context);

		LtiHandlerModel model = getModel(context);
		// Do we already have values entered in the model? (ie are we revisiting
		// this page?)
		NameValue modelSelection = model.getCurrentLtiSelection();
		String modelUrl = model.getEnteredLaunchUrl();
		// Common details
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		ViewableResource resource = attachmentResourceService.getViewableResource(context, itemInfo.getViewableItem(),
			attachment);
		addAttachmentDetails(context, resource.getCommonAttachmentDetails());

		// Attachment description defaults to the name of the LTI external tool.
		String editTitle = checkEditTitle(context, model);
		if( Check.isEmpty(attachment.getDescription()) )
		{
			attachment.setDescription(editTitle);
		}
		getDisplayName().setValue(context, attachment.getDescription());

		// load up the attachment ...
		setAttachmentValueToControls(context, attachment);

		// .. but reimpose the model values if they existed
		if( modelSelection != null )
		{
			model.setCurrentLtiSelection(modelSelection);
			ltiSelector.setSelectedValue(context, modelSelection);
		}

		if( !Check.isEmpty(modelUrl) )
		{
			model.setEnteredLaunchUrl(modelUrl);
			launchUrl.setValue(context, modelUrl);
		}
		ImageRenderer warningRenderer = new ImageRenderer(new TagState(URL_MISMATCH_WARNING_TAG_STATE), warningPngPath,
			LABEL_ALT_MISMATCH_URL);

		boolean hideWarningIcon = checkForMismatchWarning(context, this.launchUrl.getValue(context));

		model.setHideMismatchWarning(hideWarningIcon);
		warningRenderer.setStyles(null, "warningimg", null);

		model.setMatchUrlWarning(warningRenderer);

		String iconUrlStr = iconUrl.getValue(context);
		boolean badThumbnailEntered = false;
		if( !Check.isEmpty(iconUrlStr) )
		{
			badThumbnailEntered = !URLUtils.isAbsoluteUrl(iconUrlStr);
			if( badThumbnailEntered )
			{
				iconUrlStr = null;
			}
		}
		if( Check.isEmpty(iconUrlStr) && !badThumbnailEntered )
		{
			// nothing explicit (valid or otherwise) for this attachment, so
			// look for LTI url
			iconUrlStr = externalToolService.findApplicableIconUrl(attachment);
		}
		model.setThumbnail(new ImageRenderer(iconUrlStr, null));

		return viewFactory.createResult("lti-edit.ftl", this);
	}

	@Override
	@SuppressWarnings("nls")
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		LtiHandlerModel model = getModel(context);
		// If the model is already errored, it's because we're reloading the
		// addPage for the second time, after causing an error (eg bad URL), in
		// which case we keep whatever's in the launchUrl (provided it's not
		// null)
		if( !model.isError() || launchUrl.getValue(context) == null )
		{
			populateLtiSelector(context, model.getCurrentLtiSelection());
		}

		boolean hideWarningIcon = true;
		// badlaunchurl error if it's present takes precedence over mismatch
		// warning
		if( !model.getErrors().containsKey("badlaunchurl") )
		{
			hideWarningIcon = checkForMismatchWarning(context, model.getEnteredLaunchUrl());
		}

		model.setHideMismatchWarning(hideWarningIcon);

		ImageRenderer warningRenderer = new ImageRenderer(new TagState(URL_MISMATCH_WARNING_TAG_STATE), warningPngPath,
			LABEL_ALT_MISMATCH_URL);
		warningRenderer.setStyles(null, "warningimg", null);
		model.setMatchUrlWarning(warningRenderer);
		return viewFactory.createResult("lti-add.ftl", this);
	}

	private void setupMismatchEval()
	{
		// For the benefit of Javascript, create a simple array of paired LTI
		// BaseUrls & uuids strings
		String[][] selectablePairs = new String[enabledExternalToolMap.size()][2];
		int i = 0;
		for( ExternalTool lti : enabledExternalToolMap.keySet() )
		{
			selectablePairs[i][0] = lti.getBaseURL();
			selectablePairs[i][1] = lti.getUuid();
			i++;
		}
		// The selector gets the direct javascript method to determine if a
		// warning icon should be displayed ...
		ltiSelector.addChangeEventHandler(new OverrideHandler(FUNC_UPDATE_BASE_MATCH, launchUrl.createGetExpression(),
			selectablePairs, ltiSelector.createGetExpression(), Jq.$(new TagState(WARNING_DIV))));

		// .. whereas the text field gets the timer-wrapped version of same.
		launchUrl.addEventStatements(
			JSHandler.EVENT_KEYUP,
			new OverrideHandler(TIMER_UPDATE_BASE_MATCH, launchUrl.createGetExpression(), selectablePairs, ltiSelector
				.createGetExpression(), Jq.$(new TagState(WARNING_DIV))));
	}

	protected void populateLtiSelector(SectionInfo info, @Nullable NameValue selection)
	{
		// Use the passed in value if it exists, otherwise set the default as
		// the selection
		if( selection == null )
		{
			selection = automaticLtiSelection();
		}
		// ensure model and control are singing from the same song-sheet
		ltiSelector.setSelectedValue(info, selection);
		getModel(info).setCurrentLtiSelection(selection);
	}

	private void populateAttachmentFromControls(SectionInfo info, CustomAttachment ltiAttachment, String enteredUrl,
		@Nullable ExternalTool lti)
	{
		setNonNullValueOrRemoveIfNull(ltiAttachment, (lti != null ? lti.getUuid() : AUTOMATIC_UUID),
			ExternalToolConstants.EXTERNAL_TOOL_PROVIDER_UUID);

		setNonNullValueOrRemoveIfNull(ltiAttachment, enteredUrl, ExternalToolConstants.LAUNCH_URL);

		if( !Check.isEmpty(customParams.getValue(info)) )
		{
			ltiAttachment.setData(ExternalToolConstants.CUSTOM_PARAMS,
				externalToolService.parseCustomParamsString(customParams.getValue(info)));
		}
		else
		{
			ltiAttachment.getDataAttributes().remove(ExternalToolConstants.CUSTOM_PARAMS);
		}

		setNonNullValueOrRemoveIfNull(ltiAttachment, consumerKey.getValue(info), ExternalToolConstants.CONSUMER_KEY);

		setNonNullValueOrRemoveIfNull(ltiAttachment, sharedSecret.getValue(info), ExternalToolConstants.SHARED_SECRET);

		setNonNullValueOrRemoveIfNull(ltiAttachment, iconUrl.getValue(info), ExternalToolConstants.ICON_URL);
		// default privacy means the attachement's shareEmail & shareName
		// settings are voided, and the negation of default privacy means the T
		// or F settings of the shareEmail & shareName booleans are explicitly
		// preserved.
		if( useDefaultPrivacy.isChecked(info) )
		{
			ltiAttachment.getDataAttributes().remove(ExternalToolConstants.SHARE_NAME);
			ltiAttachment.getDataAttributes().remove(ExternalToolConstants.SHARE_EMAIL);
		}
		else
		{
			ltiAttachment.setData(ExternalToolConstants.SHARE_NAME, shareName.isChecked(info));
			ltiAttachment.setData(ExternalToolConstants.SHARE_EMAIL, shareEmail.isChecked(info));
		}
	}

	private Object setNonNullValueOrRemoveIfNull(Attachment ltiAttachment, String val, final String key)
	{
		if( !Check.isEmpty(val) )
		{
			ltiAttachment.setData(key, val);
			return val;
		}
		else if( ltiAttachment.getData(key) != null )
		{
			return ltiAttachment.getDataAttributes().remove(key);
		}
		return null;
	}

	/**
	 * We validate the URL here, whether or not it's been entered/edited
	 * manually
	 */
	@SuppressWarnings("nls")
	private boolean validateUrlFields(SectionInfo info)
	{
		LtiHandlerModel model = getModel(info);
		String ltiUrl = getLaunchUrl().getStringValue(info);
		model.setEnteredLaunchUrl(ltiUrl);
		if( Check.isEmpty(ltiUrl) )
		{
			// Empty URL field requires an actual selection of LTI
			if( ltiSelector.getSelectedValue(info).equals(automaticLtiSelection()) )
			{
				model.addError("nolaunchurl", LABEL_ERROR_NOLAUNCHURL);
			}
		}
		else
		{
			if( !URLUtils.isAbsoluteUrl(ltiUrl) )
			{
				model.addError("badlaunchurl", LABEL_ERROR_INVALID);
			}
		}
		// The icon url must be a valid URL - rather than simply ignoring a bad
		// URL It seems meet to force either validity or nix.
		String iconUrlStr = iconUrl.getValue(info);
		if( !Check.isEmpty(iconUrlStr) )
		{
			if( !URLUtils.isAbsoluteUrl(iconUrlStr) )
			{
				model.addError("badiconurl", LABEL_ERROR_INVALID);
			}
		}

		return !model.isError();
	}

	/**
	 * Convenience method to set the control values from the attachment.
	 * 
	 * @param info
	 * @param ltiAttachment
	 */
	private void setAttachmentValueToControls(SectionInfo info, Attachment ltiAttachment)
	{
		// if attachment has an ExternalTool UUID which matches a current
		// (enabled) LTI, present that LTI as the current selection in the LTI
		// drop down, otherwise LTI dropdown defaults to 'automatic'.
		String attachmentLtiUuid = (String) ltiAttachment.getData(ExternalToolConstants.EXTERNAL_TOOL_PROVIDER_UUID);
		boolean selectionSet = false;
		if( !Check.isEmpty(attachmentLtiUuid) )
		{
			for( ExternalTool lti : enabledExternalToolMap.keySet() )
			{
				if( lti.getUuid().equals(attachmentLtiUuid) )
				{
					populateLtiSelector(info, new NameValue(enabledExternalToolMap.get(lti), lti.getUuid()));
					selectionSet = true;
					break;
				}
			}
		}
		if( !selectionSet )
		{
			populateLtiSelector(info, automaticLtiSelection());
		}
		String enteredUrl = (String) ltiAttachment.getData(ExternalToolConstants.LAUNCH_URL);
		launchUrl.setValue(info, enteredUrl);
		getModel(info).setEnteredLaunchUrl(enteredUrl);
		consumerKey.setValue(info, (String) ltiAttachment.getData(ExternalToolConstants.CONSUMER_KEY));
		sharedSecret.setValue(info, (String) ltiAttachment.getData(ExternalToolConstants.SHARED_SECRET));
		Object val = ltiAttachment.getData(ExternalToolConstants.CUSTOM_PARAMS);
		@SuppressWarnings("unchecked")
		List<NameValue> dataCustomParams = (List<NameValue>) val;
		if( dataCustomParams != null && !Check.isEmpty(dataCustomParams) )
		{
			customParams.setValue(info, externalToolService.customParamListToString(dataCustomParams));
		}
		iconUrl.setValue(info, (String) ltiAttachment.getData(ExternalToolConstants.ICON_URL));

		// If either shareEmail or shareName values explicitly exist in the
		// attachment, the 'useDefaultPrivacy' must be false
		if( ltiAttachment.getData(ExternalToolConstants.SHARE_NAME) != null
			|| ltiAttachment.getData(ExternalToolConstants.SHARE_EMAIL) != null )
		{
			useDefaultPrivacy.setChecked(info, false);
			shareName.enable(info);
			shareEmail.enable(info);
			if( ltiAttachment.getData(ExternalToolConstants.SHARE_NAME) != null )
			{
				shareName.setChecked(info, (Boolean) ltiAttachment.getData(ExternalToolConstants.SHARE_NAME));
			}
			else
			{
				shareName.setChecked(info, false);
			}
			if( ltiAttachment.getData(ExternalToolConstants.SHARE_EMAIL) != null )
			{
				shareEmail.setChecked(info, (Boolean) ltiAttachment.getData(ExternalToolConstants.SHARE_EMAIL));
			}
			else
			{
				shareEmail.setChecked(info, false);
			}
		}
		else
		{
			useDefaultPrivacy.setChecked(info, true);
			shareName.setChecked(info, false);
			shareEmail.setChecked(info, false);
			shareName.disable(info);
			shareEmail.disable(info);
		}
	}

	/**
	 * Make sure we have some sort of descriptive string as edit dialog title.
	 * When editing an existing attachment, the edit title will be that
	 * attachment's display string. Here we make sure we've got something
	 * presentable ahead of providing a the mandatory display value for an LTI
	 * attachment.
	 * 
	 * @param context
	 * @param model
	 */
	private String checkEditTitle(RenderContext context, LtiHandlerModel model)
	{
		String editTitle = null;
		if( Check.isEmpty(model.getEditTitle()) )
		{
			if( !Check.isEmpty(launchUrl.getValue(context)) )
			{
				editTitle = launchUrl.getValue(context);
			}
			else
			{
				if( model.getCurrentLtiSelection() != null )
				{
					editTitle = model.getCurrentLtiSelection().getName();
				}
				// we really wouldn't expect to be rendering details without one
				// of the above clauses holding
			}
			if( Check.isEmpty(editTitle) )
			{
				// ... so if all else fails, fall back to something bland
				editTitle = CurrentLocale.get(FALLBACK_DESCR);
			}
			model.setEditTitle(editTitle);
		}
		return editTitle;
	}

	/**
	 * @param info
	 * @param enteredLaunchUrl
	 * @return true if warning should remain hidden, otherwise false to display
	 *         warning
	 */
	private boolean checkForMismatchWarning(SectionInfo info, String url)
	{
		// display our error and/or warnings if appropriate ...
		if( !Check.isEmpty(url) && !ltiSelector.getSelectedValue(info).equals(automaticLtiSelection()) )
		{
			String uuid = ltiSelector.getSelectedValue(info).getValue();
			ExternalTool externalTool = externalToolService.getByUuid(uuid);
			return url.equals(externalTool.getBaseURL());
		}
		return true;
	}

	private NameValue automaticLtiSelection()
	{
		return new NameValue(CurrentLocale.get(AUTOMATIC), AUTOMATIC_UUID);
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? TITLE_EDIT_LABEL : TITLE_ADD_LABEL;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return MimeTypeConstants.MIME_LINK;
	}

	@Override
	public boolean isMultipleAllowed(SectionInfo info)
	{
		return false;
	}

	public TextField getLaunchUrl()
	{
		return launchUrl;
	}

	public TextField getConsumerKey()
	{
		return consumerKey;
	}

	public TextField getSharedSecret()
	{
		return sharedSecret;
	}

	public TextField getCustomParams()
	{
		return customParams;
	}

	public TextField getIconUrl()
	{
		return iconUrl;
	}

	public Checkbox getUseDefaultPrivacy()
	{
		return useDefaultPrivacy;
	}

	public Checkbox getShareName()
	{
		return shareName;
	}

	public Checkbox getShareEmail()
	{
		return shareEmail;
	}

	public SingleSelectionList<NameValue> getLtiSelector()
	{
		return ltiSelector;
	}

	public Label getWarningDivLabel()
	{
		return LABEL_ALT_MISMATCH_URL;
	}

	public Div getWarningDiv()
	{
		return warningDiv;
	}

	private void clearAllInputControls(SectionInfo info)
	{
		LtiHandlerModel model = getModel(info);
		model.clearErrors();
		model.setCurrentLtiSelection(null);
		model.setEnteredLaunchUrl(null);
		launchUrl.setValue(info, null);
		consumerKey.setValue(info, null);
		iconUrl.setValue(info, null);
		sharedSecret.setValue(info, null);
		customParams.setValue(info, null);
		useDefaultPrivacy.setChecked(info, Boolean.TRUE);
		shareName.disable(info);
		shareEmail.disable(info);
		shareName.setChecked(info, false);
		shareEmail.setChecked(info, false);
	}

	@Override
	public Class<LtiHandlerModel> getModelClass()
	{
		return LtiHandlerModel.class;
	}

	@NonNullByDefault(false)
	public static class LtiHandlerModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel
	{
		@Bookmarked
		private NameValue currentLtiSelection;
		@Bookmarked
		private String enteredLaunchUrl;
		@Bookmarked
		private boolean hideMismatchWarning;

		private ImageRenderer matchUrlWarning;

		public NameValue getCurrentLtiSelection()
		{
			return currentLtiSelection;
		}

		public void setCurrentLtiSelection(NameValue currentLtiSelection)
		{
			this.currentLtiSelection = currentLtiSelection;
		}

		public String getEnteredLaunchUrl()
		{
			return enteredLaunchUrl;
		}

		public void setEnteredLaunchUrl(String enteredLaunchUrl)
		{
			this.enteredLaunchUrl = enteredLaunchUrl;
		}

		public boolean isHideMismatchWarning()
		{
			return hideMismatchWarning;
		}

		public void setHideMismatchWarning(boolean hideMismatchWarning)
		{
			this.hideMismatchWarning = hideMismatchWarning;
		}

		public ImageRenderer getMatchUrlWarning()
		{
			return matchUrlWarning;
		}

		public void setMatchUrlWarning(ImageRenderer renderer)
		{
			matchUrlWarning = renderer;
		}

		public boolean isError()
		{
			return getErrors().size() > 0;
		}

		public void clearErrors()
		{
			this.getErrors().clear();
		}
	}
}
