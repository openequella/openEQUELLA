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

package com.tle.mets.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.mets.MetsConstants;
import com.tle.mets.MetsIDElementInfo;
import com.tle.mets.export.MetsExporter.MetsExporterModel;
import com.tle.mets.importerexporters.MetsAttachmentImporterExporter;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.stream.AbstractContentStream;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.ExportContentSection;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

import edu.harvard.hul.ois.mets.AmdSec;
import edu.harvard.hul.ois.mets.Div;
import edu.harvard.hul.ois.mets.FLocat;
import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.FileGrp;
import edu.harvard.hul.ois.mets.FileSec;
import edu.harvard.hul.ois.mets.Fptr;
import edu.harvard.hul.ois.mets.Loctype;
import edu.harvard.hul.ois.mets.MdWrap;
import edu.harvard.hul.ois.mets.Mdtype;
import edu.harvard.hul.ois.mets.Mets;
import edu.harvard.hul.ois.mets.MetsHdr;
import edu.harvard.hul.ois.mets.StructMap;
import edu.harvard.hul.ois.mets.TechMD;
import edu.harvard.hul.ois.mets.XmlData;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;
import edu.harvard.hul.ois.mets.helper.MetsValidator;
import edu.harvard.hul.ois.mets.helper.MetsWriter;
import edu.harvard.hul.ois.mets.helper.PreformedXML;

@SuppressWarnings("nls")
@Bind
public class MetsExporter extends AbstractPrototypeSection<MetsExporterModel> implements HtmlRenderer
{
	@PlugKey(value = "export")
	private static Label LINK_LABEL;

	@Inject
	private InstitutionService institutionService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	@PlugKey(value = "export.attachments")
	private Checkbox attachments;

	private PluginTracker<MetsAttachmentImporterExporter> attachmentExporters;
	private JSBookmarkModifier exportFunc;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		ExportContentSection.assertCanExport(ParentViewItemSectionUtils.getItemInfo(context));

		HtmlLinkState exportLink = new HtmlLinkState(new BookmarkAndModify(context, exportFunc));
		exportLink.setLabel(LINK_LABEL);
		getModel(context).setExport(exportLink);

		return viewFactory.createResult("metsexport.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		exportFunc = events.getNamedModifier("doExport");

		attachments.addEventStatements(JSHandler.EVENT_CHANGE, new StatementHandler(ajax.getAjaxUpdateDomFunction(tree,
			this, null, ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "metsExport")));
	}

	@EventHandlerMethod
	public void doExport(final SectionInfo info) throws Exception
	{
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		ExportContentSection.assertCanExport(itemInfo);

		contentStreamWriter.outputStream(info.getRequest(), info.getResponse(), new MetsContentStream(info, itemInfo));
	}

	protected String fileNodeId(MetsIDElement subnode)
	{
		String subnodeId = subnode.getID();
		if( subnodeId != null && subnodeId.contains(":") )
		{
			return subnodeId.substring(subnodeId.indexOf(':') + 1);
		}
		return subnodeId;
	}

	protected String getFilename(ItemSectionInfo iinfo, SectionInfo info)
	{
		String itemName = CurrentLocale.get(iinfo.getItem().getName());
		if( Check.isEmpty(itemName) )
		{
			itemName = iinfo.getItemId().toString();
		}
		return PathUtils.fileencode(itemName.replace(' ', '_')) + "_METS." + getExtension(info);
	}

	protected String getExtension(SectionInfo info)
	{
		if( attachments.isChecked(info) )
		{
			return "zip";
		}
		return "xml";
	}

	protected String getExportMimeType(SectionInfo info)
	{
		if( attachments.isChecked(info) )
		{
			return "application/zip";
		}
		return "text/xml";
	}

	protected String getTransferEncoding(SectionInfo info)
	{
		if( attachments.isChecked(info) )
		{
			return "binary";
		}
		return null;
	}

	public void serve(SectionInfo info, ItemSectionInfo iinfo, OutputStream out) throws IOException
	{
		// If Package
		if( attachments.isChecked(info) )
		{
			// Do not make this a try-with-resource, as we don't want to close
			// the underlying OutputStream. You'll note that we call finish() in
			// the finally block which does complete the writing of the ZIP
			// information.
			ZipOutputStream zipOut = new ZipOutputStream(out);
			try
			{
				ZipEntry entry = new ZipEntry(MetsConstants.METS_FILENAME);
				zipOut.putNextEntry(entry);
				try
				{
					createMetsObject(info, iinfo, true).write(new MetsWriter(zipOut));
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
				zipOut.closeEntry();
			}
			finally
			{
				zipOut.finish();
			}
		}
		// if just record
		else
		{
			try
			{
				createMetsObject(info, iinfo, false).write(new MetsWriter(out));
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Mets createMetsObject(SectionInfo info, ItemSectionInfo iinfo, boolean full) throws Exception
	{
		Item item = iinfo.getItem();

		final MetsHdr header = new MetsHdr();
		header.setCREATEDATE(item.getDateCreated());
		header.setLASTMODDATE(item.getDateModified());
		header.setRECORDSTATUS(item.getStatus().name());

		final AmdSec amd = new AmdSec();
		amd.setID("amdSec");

		final FileSec fileSection = new FileSec();
		fileSection.setID("fileSec");

		final Mets mets = new Mets();
		mets.setOBJID(item.getIdString());
		mets.setLABEL(CurrentLocale.get(item.getName()));
		mets.getContent().add(header);
		mets.getContent().add(amd);
		mets.getContent().add(fileSection);

		final List<AmdSec> adminInfo = new ArrayList<AmdSec>();
		final List<File> metsFiles = new ArrayList<File>();
		for( Attachment attachment : item.getAttachmentsUnmodifiable() )
		{
			final List<Pair<File, String>> attachmentMetsFiles = createMetsFiles(info, iinfo, attachment, full);
			if( attachmentMetsFiles != null )
			{
				final FileGrp fileGroup = new FileGrp();
				fileGroup.setID("grp:" + attachment.getUuid());
				for( Pair<File, String> attachmentMetsFile : attachmentMetsFiles )
				{
					final File metsFile = attachmentMetsFile.getFirst();
					final String xml = attachmentMetsFile.getSecond();

					metsFile.setGROUPID(fileGroup.getID());
					fileGroup.getContent().add(metsFile);
					metsFiles.add(metsFile);

					final String fileId = metsFile.getID();

					final TechMD techInfo = new TechMD();
					techInfo.setID("tech:" + fileId);

					metsFile.setADMID(techInfo.getID());

					final MdWrap techWrap = new MdWrap();
					techWrap.setMIMETYPE("text/xml");
					techWrap.setMDTYPE(Mdtype.OTHER);
					techWrap.setOTHERMDTYPE("equella");
					techInfo.getContent().add(techWrap);

					final XmlData xmlData = new XmlData();
					xmlData.setSchema(null, "http://www.loc.gov/standards/mets/mets.xsd");
					xmlData.getContent().add(new PreformedXML(xml));
					techWrap.getContent().add(xmlData);

					adminInfo.add(amd);

					amd.getContent().add(techInfo);
				}
				fileSection.getContent().add(fileGroup);
			}
		}

		// build struct map
		StructMap map = new StructMap();
		map.setTYPE("physical"); //$NON-NLS-1$

		Div div = new Div();
		div.setORDER(1);
		div.setLABEL(LangUtils.getString(item.getName()));
		map.getContent().add(div);
		for( File metsFile : metsFiles )
		{
			Fptr fptr = new Fptr();
			fptr.getFILEID().put(metsFile.getID(), metsFile);
			div.getContent().add(fptr);
		}
		mets.getContent().add(map);

		mets.validate(new MetsValidator());

		return mets;
	}

	/**
	 * May return null
	 * 
	 * @param info
	 * @param item
	 * @param attachment
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<Pair<File, String>> createMetsFiles(SectionInfo info, ItemSectionInfo itemInfo, Attachment attachment,
		boolean full) throws Exception
	{
		final List<Pair<File, String>> files = new ArrayList<Pair<File, String>>();
		final List<MetsIDElementInfo<? extends MetsIDElement>> converted = (full
			? convertFullAttachment(info, itemInfo, attachment)
			: convertRecordOnlyAttachment(info, itemInfo, attachment));
		if( converted != null )
		{
			for( MetsIDElementInfo<? extends MetsIDElement> con : converted )
			{
				final File file = new File();
				file.setID(fileNodeId(con.getElem()));
				file.setMIMETYPE(con.getMimeType());
				// FIXME: file.setOWNERID(con.getUrl());
				file.getContent().add(con.getElem());
				files.add(new Pair<File, String>(file, con.getXml().toString()));
			}
			return files;
		}
		return null;
	}

	/**
	 * @param info
	 * @param item
	 * @param attachment
	 * @return
	 * @throws Exception
	 */
	protected List<MetsIDElementInfo<? extends MetsIDElement>> convertRecordOnlyAttachment(SectionInfo info,
		ItemSectionInfo itemInfo, Attachment attachment) throws Exception
	{
		final ViewableResource resource = attachmentResourceService.getViewableResource(info,
			itemInfo.getViewableItem(), attachment);
		final ViewItemUrl viewerUrl = resource.createDefaultViewerUrl();
		final String url = institutionService.institutionalise(viewerUrl.getHref());

		final FLocat location = new FLocat();
		location.setID("link:" + attachment.getUuid());
		location.setXlinkHref(url);
		location.setXlinkTitle(attachment.getDescription());
		location.setLOCTYPE(Loctype.URL);

		final PropBagEx xmlData = new PropBagEx();
		xmlData.setNode("url", url);
		xmlData.setNode("uuid", attachment.getUuid());
		xmlData.setNode("description", attachment.getDescription());

		final List<MetsIDElementInfo<? extends MetsIDElement>> res = new ArrayList<MetsIDElementInfo<? extends MetsIDElement>>();
		res.add(new MetsIDElementInfo<FLocat>(location, resource.getMimeType(), xmlData));
		return res;
	}

	/**
	 * May return null
	 * 
	 * @param info
	 * @param itemInfo
	 * @param attachment
	 * @return
	 * @throws Exception
	 */
	protected List<MetsIDElementInfo<? extends MetsIDElement>> convertFullAttachment(SectionInfo info,
		ItemSectionInfo itemInfo, Attachment attachment) throws Exception
	{
		final Item item = itemInfo.getItem();
		for( MetsAttachmentImporterExporter exporter : attachmentExporters.getBeanList() )
		{
			if( exporter.canExport(item, attachment) )
			{
				return exporter.export(info, item, attachment);
			}
		}
		return null;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		attachmentExporters = new PluginTracker<MetsAttachmentImporterExporter>(pluginService, "com.tle.mets",
			"exporterimporter", "id");
		attachmentExporters.setBeanKey("bean");
	}

	@Override
	public Class<MetsExporterModel> getModelClass()
	{
		return MetsExporterModel.class;
	}

	public Checkbox getAttachments()
	{
		return attachments;
	}

	public static class MetsExporterModel
	{
		private HtmlLinkState export;

		public HtmlLinkState getExport()
		{
			return export;
		}

		public void setExport(HtmlLinkState export)
		{
			this.export = export;
		}
	}

	public class MetsContentStream extends AbstractContentStream
	{
		private final ItemSectionInfo iinfo;
		private final SectionInfo info;

		public MetsContentStream(SectionInfo info, ItemSectionInfo iinfo)
		{
			super(null, getExportMimeType(info));
			this.iinfo = iinfo;
			this.info = info;
		}

		@Override
		public boolean mustWrite()
		{
			return true;
		}

		@Override
		public String getFilenameWithoutPath()
		{
			return getFilename(iinfo, info);
		}

		@Override
		public void write(OutputStream out) throws IOException
		{
			serve(info, iinfo, out);
		}

		@Override
		public String getContentDisposition()
		{
			return "attachment";
		}

		@Override
		public boolean exists()
		{
			return true;
		}

		@Override
		public long getContentLength()
		{
			return -1;
		}

		@Override
		public InputStream getInputStream() throws IOException
		{
			return null;
		}

		@Override
		public long getLastModified()
		{
			return System.currentTimeMillis();
		}
	}
}
