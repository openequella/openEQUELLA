package com.tle.web.controls.universal.handlers.fileupload.options;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.controls.universal.handlers.fileupload.PackageAttachmentHandler;
import com.tle.web.controls.universal.handlers.fileupload.TypeOptions;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
public class PackageOptions extends AbstractPrototypeSection<PackageOptions.PackageOptionsModel>
	implements
		TypeOptions,
		HtmlRenderer
{
	@PlugKey("handlers.file.packageoptions.aszip")
	private static Label LABEL_TREAT_AS_ZIP;
	@PlugKey("handlers.file.packageoptions.asfile")
	private static Label LABEL_TREAT_AS_FILE;

	@PlugKey("handlers.file.packageoption.title")
	private static Label TITLE;

	@ViewFactory
	private FreemarkerFactory view;

	@Component
	private SingleSelectionList<TreatAsOption> packageOptions;

	@Inject
	private PluginTracker<PackageAttachmentHandler> packageHandlersTracker;

	// We can safely cache this... I think
	@Nullable
	private UploadedFile uploadedFile;
	private FileUploadSettings settings;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getModel(context).setEditTitle(TITLE);
		return view.createResult("file/file-packageoptions.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		packageOptions.setListModel(new PackageOptionsListModel());
		packageOptions.setAlwaysSelect(true);
	}

	public void setSettings(FileUploadSettings settings, FileUploadHandler fileUploadHandler)
	{
		this.settings = settings;
		// this.fileUploadHandler = fileUploadHandler;
	}

	@Override
	public void loadOptions(SectionInfo info, UploadedFile uploadedFile)
	{
		this.uploadedFile = uploadedFile;

		String type = uploadedFile.getResolvedType();
		if( type == null )
		{
			type = FileUploadHandler.FILE_TYPE_PACKAGE;
		}
		if( type.equals(FileUploadHandler.FILE_TYPE_PACKAGE) )
		{
			// get the subtype and decide on that
			final String packageType = uploadedFile.getResolvedSubType();
			// If not a resolved package type rely on the 'alwaysSelect' setting
			// to choose the most relevant type.
			if( packageType != null )
			{
				packageOptions.setSelectedStringValue(info, packageType);
			}
		}
		// File/Zip etc
		else
		{
			packageOptions.setSelectedStringValue(info, type);
		}
	}

	@Override
	public boolean saveOptions(SectionInfo info, UploadedFile uploadedFile)
	{
		final TreatAsOption opt = packageOptions.getSelectedValue(info);
		final String value = opt.getValue();
		if( value.equals(FileUploadHandler.FILE_TYPE_ZIP) )
		{
			// Still potential only, need to show the zip options screen
			uploadedFile.setPotentialType(FileUploadHandler.FILE_TYPE_ZIP);
		}
		else if( value.equals(FileUploadHandler.FILE_TYPE_FILE) )
		{
			uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_FILE);
		}
		// Plugin supplied type (IMS, SCORM, QTI etc)
		else
		{
			uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_PACKAGE);

			// plugin to decide
			String packageType = value;
			PackageAttachmentHandler packageAttachmentHandler = packageHandlersTracker.getBeanMap().get(packageType);
			if( packageAttachmentHandler == null )
			{
				throw new Error("No package handler for package type " + packageType);
			}
			uploadedFile.setResolvedSubType(packageType);
			uploadedFile.setMimeType(packageAttachmentHandler.getMimeType(info, uploadedFile, packageType));
		}
		this.uploadedFile = null;
		return true;
	}

	protected List<TreatAsOption> getPackageTypeOptions(SectionInfo info)
	{
		final List<TreatAsOption> types = Lists.newArrayList();
		final List<String> packageTypes = uploadedFile.getPackageTypes();
		if( packageTypes == null )
		{
			throw new Error("Package types are unknown!");
		}
		for( String pt : packageTypes )
		{
			// ask the plugin for the label
			final PackageAttachmentHandler packageAttachmentHandler = packageHandlersTracker.getBeanMap().get(pt);
			if( packageAttachmentHandler != null )
			{
				final Label label = packageAttachmentHandler.getTreatAsLabel(info, uploadedFile, pt);
				types.add(new TreatAsOption(label, pt));
			}
		}
		return types;
	}

	public SingleSelectionList<TreatAsOption> getPackageOptions()
	{
		return packageOptions;
	}

	public class PackageOptionsListModel extends DynamicHtmlListModel<TreatAsOption>
	{
		@Override
		protected Iterable<TreatAsOption> populateModel(SectionInfo info)
		{
			final List<TreatAsOption> values = Lists.newArrayList();
			values.addAll(getPackageTypeOptions(info));
			if( !settings.isPackagesOnly() && !settings.isNoUnzip() )
			{
				values.add(new TreatAsOption(LABEL_TREAT_AS_ZIP, FileUploadHandler.FILE_TYPE_ZIP));
			}
			if( !settings.isPackagesOnly() )
			{
				values.add(new TreatAsOption(LABEL_TREAT_AS_FILE, FileUploadHandler.FILE_TYPE_FILE));
			}

			return values;
		}

		@Override
		protected Option<TreatAsOption> convertToOption(SectionInfo info, TreatAsOption obj)
		{
			return new LabelOption<TreatAsOption>(obj.getLabel(), obj.getValue(), obj);
		}
	}

	@Override
	public PackageOptionsModel instantiateModel(SectionInfo info)
	{
		return new PackageOptionsModel();
	}

	private static class TreatAsOption
	{
		private final Label label;
		private final String value;

		public TreatAsOption(Label label, String value)
		{
			this.label = label;
			this.value = value;
		}

		public Label getLabel()
		{
			return label;
		}

		public String getValue()
		{
			return value;
		}
	}

	@NonNullByDefault(false)
	public static class PackageOptionsModel
	{
		private Label editTitle;

		public Label getEditTitle()
		{
			return editTitle;
		}

		public void setEditTitle(Label editTitle)
		{
			this.editTitle = editTitle;
		}
	}
}
