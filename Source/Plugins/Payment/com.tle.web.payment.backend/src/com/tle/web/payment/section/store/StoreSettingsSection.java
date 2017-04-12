package com.tle.web.payment.section.store;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.tle.common.Check;
import com.tle.core.email.EmailService;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.filesystem.SystemFile;
import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.StoreSettings;
import com.tle.core.payment.StoreSettingsPrivilegeTreeProvider;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.StagingService;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.system.LicenseService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author dustin
 */
@Bind
@SuppressWarnings("nls")
public class StoreSettingsSection extends OneColumnLayout<StoreSettingsSection.StoreSettingsModel>
{
	private static final Set<String> CONTENT_TYPES = Sets.newHashSet("png", "jpeg", "jpg", "gif");

	private static final String DEFAULT_EXTENSION = "png";

	private static final String LARGE_ICON = "large-store-icon";
	private static final String SMALL_ICON = "small-store-icon";

	private static final String AJAX_ALL = "overallajaxdiv";
	private static final int LARGE_ICON_X = 445;
	private static final int LARGE_ICON_Y = 190;
	private static final int SMALL_ICON_X = 32;
	private static final int SMALL_ICON_Y = 32;
	// Feel free to change it once you know how big it should be

	@PlugKey("store.settings.page.title")
	private static Label TITLE_LABEL;
	@PlugKey("store.settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("store.upload.error.unsupported")
	private static Label LABEL_ERROR_BADIMAGE;
	@PlugKey("store.error.contact.email.invalid")
	private static Label LABEL_ERROR_INVALIDEMAIL;
	@PlugKey("store.upload.error.empty")
	private static Label LABEL_ERROR_UPLOAD;
	@PlugKey("settings.error.license")
	private static Label LABEL_ERROR_LICENSE;
	@PlugKey("settings.upload.error.small.unacceptable")
	private static Label LABEL_UNACCEPTABLE_SMALL_ICON;
	@PlugKey("settings.upload.error.large.unacceptable")
	private static Label LABEL_UNACCEPTABLE_LARGE_ICON;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	protected AjaxGenerator ajax;

	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private StoreSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private ImageMagickService imageMagickService;
	@Inject
	private StagingService stagingService;
	@Inject
	private UrlService urlService;
	@Inject
	private EmailService emailService;
	@Inject
	private LicenseService licenseService;

	@Component(name = "allow", stateful = false)
	private Checkbox allowStore;

	@Component(name = "name", stateful = false)
	private TextField storeName;

	@Component(name = "description", stateful = false)
	private TextField storeDescription;

	@Component
	private FileUpload smallFile;

	@Component
	private FileUpload largeFile;

	@PlugKey("store.settings.upload.button")
	@Component
	private Button smallUploadButton;

	@PlugKey("store.settings.upload.removeicon")
	@Component
	private Button smallDeleteIconButton;

	@PlugKey("store.settings.upload.button")
	@Component
	private Button largeUploadButton;

	@PlugKey("store.settings.upload.removeimage")
	@Component
	private Button largeDeleteIconButton;

	@Component(name = "contact_name", stateful = false)
	private TextField contactName;

	@Component(name = "contact_number", stateful = false)
	private TextField contactNumber;

	@Component(name = "contact_email", stateful = false)
	private TextField contactEmail;

	@PlugKey("store.settings.save.button")
	@Component
	private Button saveButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		UpdateDomFunction ajaxUpdate = ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("toggleEnabled"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), AJAX_ALL);

		allowStore.setClickHandler(ajaxUpdate);

		smallUploadButton.setClickHandler(events.getNamedHandler("upload", false, false));
		final UpdateDomFunction removeIconAjax = getAjaxUpdate(tree, events.getEventHandler("removeIcon"), AJAX_ALL);
		smallDeleteIconButton.setClickHandler(new OverrideHandler(removeIconAjax, false));
		largeUploadButton.setClickHandler(events.getNamedHandler("upload", true, false));
		largeDeleteIconButton.setClickHandler(new OverrideHandler(removeIconAjax, true));

		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();
		if( !licenseService.isFeatureEnabled(PaymentConstants.LICENSE_FEATURE_CONTENT_EXCHANGE) )
		{
			throw new AccessDeniedException(LABEL_ERROR_LICENSE.getText());
		}

		StoreSettingsModel model = getModel(info);
		StoreSettings storeSettings = getStoreSettings();

		if( !model.isLoaded() )
		{
			allowStore.setChecked(info, storeSettings.getEnabled());
			storeName.setValue(info, storeSettings.getName());
			storeDescription.setValue(info, storeSettings.getDescription());
			contactName.setValue(info, storeSettings.getContactName());
			contactNumber.setValue(info, storeSettings.getContactNumber());
			contactEmail.setValue(info, storeSettings.getContactEmail());

			String iconPath = storeSettings.getIcon();
			SystemFile systemFile = new SystemFile();

			if( !Check.isEmpty(iconPath) && fileSystemService.fileExists(systemFile, iconPath) )
			{
				StagingFile stagingFile = stagingService.createStagingArea();
				fileSystemService.copy(systemFile, iconPath, stagingFile, iconPath);
				model.setLargeStagingId(stagingFile.getUuid());
				model.setLargeFilename(iconPath);
			}

			iconPath = storeSettings.getIconSmall();
			systemFile = new SystemFile();

			if( !Check.isEmpty(iconPath) && fileSystemService.fileExists(systemFile, iconPath) )
			{
				StagingFile stagingFile = stagingService.createStagingArea();
				fileSystemService.copy(systemFile, iconPath, stagingFile, iconPath);
				model.setSmallStagingId(stagingFile.getUuid());
				model.setSmallFilename(iconPath);
			}

			model.setShowControls(storeSettings.getEnabled());
			model.setLoaded(true);
		}
		else
		{
			model.setShowControls(allowStore.isChecked(info));
		}
		model.setSmallImage(new ImageRenderer(getIconUrl(info, false), new TextLabel(model.getSmallFilename())));
		model.setLargeImage(new ImageRenderer(getIconUrl(info, true), new TextLabel(model.getLargeFilename())));

		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "storesettings.ftl", this));
	}

	private String getIconUrl(RenderContext info, boolean large)
	{
		StoreSettingsModel model = getModel(info);
		String filename = large ? model.getLargeFilename() : model.getSmallFilename();

		if( Check.isEmpty(filename) )
		{
			return null;
		}

		String stagingId = large ? model.getLargeStagingId() : model.getSmallStagingId();
		final String href = new BookmarkAndModify(info, events.getNamedModifier("getIcon", stagingId, filename))
			.getHref();
		return urlService.removeInstitution(href);
	}

	@EventHandlerMethod
	public void getIcon(SectionInfo info, String uuid, String filename) throws IOException
	{
		StagingFile stagingFile = new StagingFile(uuid);
		if( fileSystemService.fileExists(stagingFile, filename) && !fileSystemService.fileIsDir(stagingFile, filename) )
		{
			HttpServletResponse response = info.getResponse();
			response.setContentType(mimeTypeService.getMimeTypeForFilename(filename));
			try( InputStream in = fileSystemService.read(stagingFile, filename);
				OutputStream out = response.getOutputStream() )
			{
				ByteStreams.copy(in, out);

			}
			finally
			{
				info.setRendered();
			}
		}
	}

	private UpdateDomFunction getAjaxUpdate(SectionTree tree, ParameterizedEvent eventHandler, String... ajaxIds)
	{
		return ajax.getAjaxUpdateDomFunction(tree, this, eventHandler,
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), ajaxIds);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		if( saveSystemConstants(info) )
		{
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
			getModel(info).setLoaded(false);
		}
		else
		{
			info.preventGET();
		}
	}

	private StoreSettings getStoreSettings()
	{
		return configService.getProperties(new StoreSettings());
	}

	private boolean saveSystemConstants(SectionInfo info)
	{
		final StoreSettings storeSettings = getStoreSettings();
		StoreSettingsModel model = getModel(info);
		boolean large = true;

		if( !allowStore.isChecked(info) )
		{
			storeSettings.setEnabled(allowStore.isChecked(info));
			// Allow people to disable without full validation
			configService.setProperties(storeSettings);
			return true;
		}

		// Validate
		if( allowStore.isChecked(info) )
		{
			// Messy messy messy
			// Dustin will give you a dollar if you can re-factor this into 6
			// lines
			//
			// Edit: where's my dollar? Aaron
			validateNonEmpty(storeName, info, "nameErrors", "name.empty");
			validateNonEmpty(storeDescription, info, "descriptionErrors", "description.empty");
			validateNonEmpty(contactName, info, "contactNameErrors", "contact.name.empty");
			validateNonEmpty(contactNumber, info, "contactNumberErrors", "contact.number.empty");
			validateNonEmpty(contactEmail, info, "contactEmailErrors", "contact.email.empty");
			if( !emailService.isValidAddress(contactEmail.getValue(info)) )
			{
				model.addError("contactEmailErrors", LABEL_ERROR_INVALIDEMAIL);
			}
			if( !model.getErrors().isEmpty() )
			{
				return false;
			}
		}
		storeSettings.setEnabled(allowStore.isChecked(info));
		storeSettings.setName(storeName.getValue(info));
		storeSettings.setDescription(storeDescription.getValue(info));

		try
		{
			upload(info, large, true);
			large = !large;
			upload(info, large, true);
		}
		catch( IOException e )
		{
			model.addError((large ? "uploadlarge" : "uploadsmall"), LABEL_ERROR_BADIMAGE);
			return false;
		}

		if( !model.errors.isEmpty() )
		{ // problem with upload
			return false;
		}

		saveIcon(info, model.getLargeStagingId(), model.getLargeFilename(), true);
		saveIcon(info, model.getSmallStagingId(), model.getSmallFilename(), false);

		storeSettings.setContactName(contactName.getValue(info));
		storeSettings.setContactNumber(contactNumber.getValue(info));
		storeSettings.setContactEmail(contactEmail.getValue(info));

		configService.setProperties(storeSettings);
		return true;
	}

	private void validateNonEmpty(TextField field, SectionInfo info, String errorKey, String errorMessageKey)
	{
		if( Check.isEmpty(field.getValue(info)) )
		{
			getModel(info).addError(errorKey,
				new KeyLabel("com.tle.web.payment.backend.store.error." + errorMessageKey));
		}
	}

	public void saveIcon(SectionInfo info, String stagingId, String filename, boolean large)
	{
		final StoreSettings storeSettings = getStoreSettings();
		if( !Check.isEmpty(filename) )
		{
			fileSystemService.removeFile(new SystemFile(), filename);
		}

		if( !Check.isEmpty(stagingId) )
		{
			StagingFile stagingFile = new StagingFile(stagingId);
			SystemFile systemFile = new SystemFile();

			fileSystemService.copy(stagingFile, filename, systemFile, filename);
			if( large )
			{
				storeSettings.setIcon(filename);
			}
			else
			{
				storeSettings.setIconSmall(filename);
			}
		}
		else
		{
			if( large )
			{
				storeSettings.setIcon(null);
			}
			else
			{
				storeSettings.setIconSmall(null);
			}
		}
	}

	@EventHandlerMethod
	public void toggleEnabled(SectionInfo info)
	{
		boolean show = allowStore.isChecked(info);
		getModel(info).setShowControls(show);
	}

	@EventHandlerMethod
	public void upload(SectionInfo info, boolean large, boolean onSave) throws IOException
	{
		StoreSettingsModel model = getModel(info);
		FileUpload file = large ? largeFile : smallFile;
		int x = large ? LARGE_ICON_X : SMALL_ICON_X;
		int y = large ? LARGE_ICON_Y : SMALL_ICON_Y;
		String error = large ? "uploadlarge" : "uploadsmall";

		String originalName = file.getFilename(info);
		if( Check.isEmpty(originalName) || file.getFileSize(info) <= 0 )
		{
			if( onSave && Check.isEmpty(originalName) )
			{
				// nothing to upload
				return;
			}
			else
			{
				model.addError(error, LABEL_ERROR_UPLOAD);
				info.preventGET();
				return;
			}
		}

		String extension = FilenameUtils.getExtension(originalName).toLowerCase();
		// valid file?
		if( !imageMagickService.supported(mimeTypeService.getMimeTypeForFilename(originalName)) )
		{
			model.addError(error, LABEL_ERROR_BADIMAGE);
			info.preventGET();
			return;
		}
		// valid extension? if not -> make it one
		if( Check.isEmpty(extension) || !CONTENT_TYPES.contains(extension) )
		{
			extension = DEFAULT_EXTENSION;
		}
		String newName = (large ? LARGE_ICON : SMALL_ICON) + "." + extension;
		removeIcon(info, large);

		StagingFile stagingFile = stagingService.createStagingArea();
		boolean uploaded = false;
		try( InputStream stream = file.getInputStream(info) )
		{
			fileSystemService.write(stagingFile, "TEMP-" + originalName, stream, false);
			File temp = fileSystemService.getExternalFile(stagingFile, "TEMP-" + originalName);

			Image image = ImageIO.read(temp);
			int imageWidth = image.getWidth(null);
			int imageHeight = image.getHeight(null);

			// store image must be 445 X 190 and store icon must be 32 X 32
			if( imageWidth == x && imageHeight == y )
			{
				uploaded = true;
				File newFile = fileSystemService.getExternalFile(stagingFile, newName);
				imageMagickService.sampleNoRatio(temp, newFile, String.valueOf(x), String.valueOf(y));
				fileSystemService.removeFile(stagingFile, "TEMP-" + originalName);
			}
			else
			{
				if( x == SMALL_ICON_X )
				{
					model.addError(error, LABEL_UNACCEPTABLE_SMALL_ICON);
				}
				else
				{
					model.addError(error, LABEL_UNACCEPTABLE_LARGE_ICON);
				}

			}
		}

		if( uploaded )
		{
			if( large )
			{
				model.setLargeFilename(newName);
				model.setLargeStagingId(stagingFile.getUuid());
			}
			else
			{
				model.setSmallFilename(newName);
				model.setSmallStagingId(stagingFile.getUuid());
			}
		}
		info.preventGET();
	}

	@EventHandlerMethod
	public void removeIcon(SectionInfo info, boolean large)
	{
		StoreSettingsModel model = getModel(info);

		String fileName = large ? model.getLargeFilename() : model.getSmallFilename();
		String stagingId = large ? model.getLargeStagingId() : model.getSmallStagingId();
		if( large )
		{
			model.setLargeFilename(null);
			model.setLargeStagingId(null);
		}
		else
		{
			model.setSmallFilename(null);
			model.setSmallStagingId(null);
		}
		if( Check.isEmpty(stagingId) || Check.isEmpty(fileName) )
		{
			return;
		}

		StagingFile stagingFile = new StagingFile(stagingId);

		if( fileSystemService.fileExists(stagingFile, fileName) )
		{
			fileSystemService.removeFile(stagingFile, fileName);
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new StoreSettingsModel();
	}

	@Override
	public Class<StoreSettingsModel> getModelClass()
	{
		return StoreSettingsModel.class;
	}

	public static class StoreSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		@Bookmarked
		private boolean loaded;
		private boolean successful;
		private boolean showControls;
		@Bookmarked
		private String smallStagingId;
		@Bookmarked
		private String smallFilename;
		@Bookmarked
		private String largeStagingId;
		@Bookmarked
		private String largeFilename;
		private ImageRenderer smallImage;
		private ImageRenderer largeImage;

		private final Map<String, Label> errors = Maps.newHashMap();

		public boolean isShowControls()
		{
			return showControls;
		}

		public void setShowControls(boolean showControls)
		{
			this.showControls = showControls;
		}

		public boolean isSuccessful()
		{
			return successful;
		}

		public void setSuccessful(boolean successful)
		{
			this.successful = successful;
		}

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}

		public void addError(String key, Label value)
		{
			this.errors.put(key, value);
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}

		public String getSmallStagingId()
		{
			return smallStagingId;
		}

		public void setSmallStagingId(String smallStagingId)
		{
			this.smallStagingId = smallStagingId;
		}

		public String getSmallFilename()
		{
			return smallFilename;
		}

		public void setSmallFilename(String smallFilename)
		{
			this.smallFilename = smallFilename;
		}

		public String getLargeStagingId()
		{
			return largeStagingId;
		}

		public void setLargeStagingId(String largeStagingId)
		{
			this.largeStagingId = largeStagingId;
		}

		public String getLargeFilename()
		{
			return largeFilename;
		}

		public void setLargeFilename(String largeFilename)
		{
			this.largeFilename = largeFilename;
		}

		public ImageRenderer getSmallImage()
		{
			return smallImage;
		}

		public void setSmallImage(ImageRenderer smallImage)
		{
			this.smallImage = smallImage;
		}

		public ImageRenderer getLargeImage()
		{
			return largeImage;
		}

		public void setLargeImage(ImageRenderer largeImage)
		{
			this.largeImage = largeImage;
		}
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public TextField getStoreName()
	{
		return storeName;
	}

	public TextField getStoreDescription()
	{
		return storeDescription;
	}

	public Checkbox getAllowStore()
	{
		return allowStore;
	}

	public TextField getContactName()
	{
		return contactName;
	}

	public TextField getContactNumber()
	{
		return contactNumber;
	}

	public TextField getContactEmail()
	{
		return contactEmail;
	}

	public static int getLargeIconX()
	{
		return LARGE_ICON_X;
	}

	public static int getLargeIconY()
	{
		return LARGE_ICON_Y;
	}

	public static int getSmallIconX()
	{
		return SMALL_ICON_X;
	}

	public static int getSmallIconY()
	{
		return SMALL_ICON_Y;
	}

	public FileUpload getSmallFile()
	{
		return smallFile;
	}

	public FileUpload getLargeFile()
	{
		return largeFile;
	}

	public Button getSmallUploadButton()
	{
		return smallUploadButton;
	}

	public Button getSmallDeleteIconButton()
	{
		return smallDeleteIconButton;
	}

	public Button getLargeUploadButton()
	{
		return largeUploadButton;
	}

	public Button getLargeDeleteIconButton()
	{
		return largeDeleteIconButton;
	}
}
