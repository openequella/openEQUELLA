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

package com.tle.core.wizard.controls.universal.migration.v50;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.w3c.dom.Node;

import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.wizard.controls.resource.ResourceSettings;
import com.tle.common.wizard.controls.resource.ResourceSettings.AllowedSelection;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings;
import com.tle.common.wizard.controls.universal.handlers.ITunesUSettings;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlHelper;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.xml.XmlDocument;
import com.tle.core.xml.XmlDocument.NodeListIterable.NodeListIterator;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class ReplaceDeletedControlsXmlMigration extends XmlMigrator
{
	private static final String ITEM_FINDER_CLASS = "com.dytech.edge.wizard.beans.control.ItemFinder";
	private static final String LINKS_CLASS = "com.dytech.edge.wizard.beans.control.Links";
	private static final String LINK_CLASS = "com.dytech.edge.wizard.beans.control.Link";
	private static final String SINGLE_LINK_CLASS = "com.dytech.edge.wizard.beans.control.SingleLink";
	private static final String IMS_CLASS = "com.dytech.edge.wizard.beans.control.IMS";
	private static final String FILE_CLASS = "com.dytech.edge.wizard.beans.control.File";
	private static final String SINGLE_FILE_CLASS = "com.dytech.edge.wizard.beans.control.SingleFile";
	private static final String CUSTOM_CONTROL_CLASS = "com.dytech.edge.wizard.beans.control.CustomControl";

	private static final String ITEM_FINDER_XPATH = ITEM_FINDER_CLASS;
	private static final String LINKS_XPATH = LINKS_CLASS;
	private static final String LINK_XPATH = LINK_CLASS;
	private static final String SINGLE_LINK_XPATH = SINGLE_LINK_CLASS;
	private static final String IMS_XPATH = IMS_CLASS;
	private static final String FILE_XPATH = FILE_CLASS;
	private static final String SINGLE_FILE_XPATH = SINGLE_FILE_CLASS;
	private static final String RESOURCE_XPATH = "com.dytech.edge.wizard.beans.control.CustomControl[classType=\"resource\"]";
	private static final String YOUTUBE_XPATH = "com.dytech.edge.wizard.beans.control.CustomControl[classType=\"youtube\"]";
	private static final String FLICKR_XPATH = "com.dytech.edge.wizard.beans.control.CustomControl[classType=\"flickr\"]";
	private static final String GOOGLEBOOK_XPATH = "com.dytech.edge.wizard.beans.control.CustomControl[classType=\"googlebook\"]";
	private static final String ITUNESU_XPATH = "com.dytech.edge.wizard.beans.control.CustomControl[classType=\"itunesu\"]";
	private static final String MYPAGES_XPATH = "com.dytech.edge.wizard.beans.control.CustomControl[classType=\"mypages\"]";

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final TemporaryFileHandle idefFolder = new SubTemporaryFile(staging, "itemdefinition");
		if( fileSystemService.fileExists(idefFolder) )
		{
			final XStream x = createXStream(xmlHelper);
			for( String entry : xmlHelper.getXmlFileList(idefFolder) )
			{
				final XmlDocument xml = new XmlDocument(xmlHelper.readToPropBagEx(idefFolder, entry).toString());
				final Node wizardNode = xml.node("//slow/wizard");

				if( wizardNode != null && replaceAllObsoleteControls(xml, wizardNode, x) )
				{
					xmlHelper.writeFile(idefFolder, entry, xml.toString());
				}
			}
		}
	}

	public static XStream createXStream(XmlHelper xmlHelper)
	{
		final XStream x = xmlHelper.createXStream(ReplaceDeletedControlsXmlMigration.class.getClassLoader());
		x.alias(ITEM_FINDER_CLASS, FakeItemFinder.class);
		x.alias(LINK_CLASS, FakeEditBox.class);
		x.alias(SINGLE_LINK_CLASS, FakeEmptyControl.class);
		x.alias(LINKS_CLASS, FakeMultiControl.class);
		x.alias(IMS_CLASS, FakeEmptyControl.class);
		x.alias(FILE_CLASS, FakeFileControl.class);
		x.alias(SINGLE_FILE_CLASS, FakeFileControl.class);
		x.alias(CUSTOM_CONTROL_CLASS, FakeCustomControl.class);
		return x;
	}

	public static boolean replaceAllObsoleteControls(XmlDocument xml, Node wizardNode, XStream x)
	{
		boolean modified = false;

		modified |= replaceItemFinder(xml, wizardNode, x);
		modified |= replaceResourceSelector(xml, wizardNode, x);
		modified |= replaceGoogleBooks(xml, wizardNode, x);
		modified |= replaceYouTube(xml, wizardNode, x);
		modified |= replaceFlickr(xml, wizardNode, x);
		modified |= replaceITunesU(xml, wizardNode, x);
		modified |= replaceFileUpload(xml, wizardNode, x);
		modified |= replaceLinks(xml, wizardNode, x);
		modified |= replacePackageUploader(xml, wizardNode, x);
		modified |= replaceMyPages(xml, wizardNode, x);

		return modified;
	}

	private static boolean replaceItemFinder(XmlDocument xml, Node wizardNode, XStream x)
	{
		return replaceGeneric(xml, wizardNode, x, ITEM_FINDER_XPATH,
			new DefaultHandlerAttributesCallback<FakeItemFinder>("resourceHandler", "_item", true)
			{
				@Override
				protected void mapExtra(CustomControl newControl, FakeItemFinder oldControl)
				{
					final ResourceSettings settings = new ResourceSettings(newControl);
					settings.setMultipleSelection(oldControl.isMultiple());
					settings.setAllowedSelection(AllowedSelection.ITEMS);
				}
			}, 0) != 0;
	}

	private static boolean replaceResourceSelector(XmlDocument xml, Node wizardNode, XStream x)
	{
		return replaceGeneric(xml, wizardNode, x, RESOURCE_XPATH,
			new DefaultHandlerAttributesCallback<FakeCustomControl>("resourceHandler")
			{
				@Override
				public void mapHandlerAttributes(CustomControl newControl, FakeCustomControl oldControl)
				{
					final Map<Object, Object> attributes = newControl.getAttributes();
					attributes.putAll(oldControl.getAttributes());
					attributes.put("AttachmentTypes", Collections.singleton(handlerType));
				}
			}, 0) != 0;
	}

	private static boolean replaceGoogleBooks(XmlDocument xml, Node wizardNode, XStream x)
	{
		return replaceGeneric(xml, wizardNode, x, GOOGLEBOOK_XPATH,
			new DefaultHandlerAttributesCallback<FakeCustomControl>("googleBookHandler"), 0) != 0;
	}

	private static boolean replaceYouTube(XmlDocument xml, Node wizardNode, XStream x)
	{
		return replaceGeneric(xml, wizardNode, x, YOUTUBE_XPATH,
			new DefaultHandlerAttributesCallback<FakeCustomControl>("youTubeHandler"), 0) != 0;
	}

	private static boolean replaceFlickr(XmlDocument xml, Node wizardNode, XStream x)
	{
		return replaceGeneric(xml, wizardNode, x, FLICKR_XPATH,
			new DefaultHandlerAttributesCallback<FakeCustomControl>("flickrHandler"), 0) != 0;
	}

	private static boolean replaceITunesU(XmlDocument xml, Node wizardNode, XStream x)
	{
		return replaceGeneric(xml, wizardNode, x, ITUNESU_XPATH,
			new DefaultHandlerAttributesCallback<FakeCustomControl>("iTunesUHandler")
			{
				@Override
				public void mapExtra(CustomControl newControl, FakeCustomControl oldControl)
				{
					final ITunesUSettings newSettings = new ITunesUSettings(newControl);
					String id = (String) oldControl.getAttributes().get("institutionId");
					newSettings.setInstitutionId(id);
				}
			}, 0) != 0;
	}

	private static boolean replaceFileUpload(XmlDocument xml, Node wizardNode, XStream x)
	{
		final DefaultHandlerAttributesCallback<FakeFileControl> cb = new DefaultHandlerAttributesCallback<FakeFileControl>(
			"fileHandler")
		{
			@Override
			public void mapExtra(CustomControl newControl, FakeFileControl oldControl)
			{
				final FileUploadSettings settings = new FileUploadSettings(newControl);
				settings.setNoUnzip(!oldControl.isUnzip());
				settings.setPackagesOnly(false);
			}
		};

		boolean modified = false;
		int idx = 0;
		int prevIdx = idx;

		idx = replaceGeneric(xml, wizardNode, x, FILE_XPATH, cb, prevIdx);
		modified |= (idx != prevIdx);
		prevIdx = idx;

		cb.setMultiple(false);
		idx = replaceGeneric(xml, wizardNode, x, SINGLE_FILE_XPATH, cb, prevIdx);
		modified |= (idx != prevIdx);

		return modified;
	}

	private static boolean replaceLinks(XmlDocument xml, Node wizardNode, XStream x)
	{
		boolean modified = false;
		int idx = 0;
		int prevIdx = idx;

		idx = replaceGeneric(xml, wizardNode, x, LINKS_XPATH,
			new DefaultHandlerAttributesCallback<FakeMultiControl>("urlHandler", true), prevIdx);
		modified |= (idx != prevIdx);
		prevIdx = idx;

		idx = replaceGeneric(xml, wizardNode, x, LINK_XPATH,
			new DefaultHandlerAttributesCallback<FakeEditBox>("urlHandler", false), prevIdx);
		modified |= (idx != prevIdx);
		prevIdx = idx;

		idx = replaceGeneric(xml, wizardNode, x, SINGLE_LINK_XPATH,
			new DefaultHandlerAttributesCallback<FakeEmptyControl>("urlHandler", false), prevIdx);
		modified |= (idx != prevIdx);

		return modified;
	}

	private static boolean replacePackageUploader(XmlDocument xml, Node wizardNode, XStream x)
	{
		return replaceGeneric(xml, wizardNode, x, IMS_XPATH,
			new DefaultHandlerAttributesCallback<FakeEmptyControl>("fileHandler", "_pkg", false)
			{
				@Override
				public void mapExtra(CustomControl newControl, FakeEmptyControl oldControl)
				{
					final FileUploadSettings settings = new FileUploadSettings(newControl);
					settings.setNoUnzip(true);
					settings.setPackagesOnly(true);
				}
			}, 0) != 0;
	}

	private static boolean replaceMyPages(XmlDocument xml, Node wizardNode, XStream x)
	{
		return replaceGeneric(xml, wizardNode, x, MYPAGES_XPATH,
			new DefaultHandlerAttributesCallback<FakeCustomControl>("mypagesHandler"), 0) != 0;
	}

	public static String getXpathForHandler(String handlerType)
	{
		return "item/temp_" + handlerType;
	}

	@SuppressWarnings("unchecked")
	private static <T extends FakeEmptyControl> int replaceGeneric(XmlDocument xml, Node wizardNode, XStream x,
		String controlXpath, HandlerAttributesCallback<T> callback, int schemaNodeUniqueIndexStart)
	{
		final NodeListIterator it = xml.nodeList("//" + controlXpath, wizardNode).iterator();

		int i = schemaNodeUniqueIndexStart;
		while( it.hasNext() )
		{
			final Node oldControlXml = it.next();

			// not tranformed: size1, size2, items, importPaths, styles
			final Node parentXml = oldControlXml.getParentNode();

			final FakeEmptyControl oldControl = (FakeEmptyControl) x.fromXML(XmlDocument.xmlToString(oldControlXml));
			final CustomControl newControl = new CustomControl();

			newControl.setMandatory(oldControl.isMandatory());
			newControl.setReload(oldControl.isReload());
			newControl.setInclude(oldControl.isInclude());
			newControl.setClassType("universal");
			newControl.setAfterSaveScript(oldControl.getAfterSaveScript());
			newControl.setCustomName(oldControl.getCustomName());
			newControl.setScript(oldControl.getScript());
			newControl.setValidateScript(oldControl.getValidateScript());

			// Add existing target nodes, or create a new temporary one if
			// none exist.
			List<TargetNode> tns = oldControl.getTargetnodes();
			if( Check.isEmpty(tns) )
			{
				tns = tns != null ? tns : new ArrayList<TargetNode>();
				tns.add(new TargetNode(getXpathForHandler(callback.getXpath(i)), ""));
			}
			newControl.setTargetnodes(tns);

			callback.mapHandlerAttributes(newControl, (T) oldControl);

			// create the new control xml
			final Node newControlXml = parentXml.getOwnerDocument().importNode(
				new XmlDocument(x.toXML(newControl)).node("com.dytech.edge.wizard.beans.control.CustomControl"), true);

			// pull a switcheroo on the target, title and description nodes
			switcheroo(xml, "title", newControlXml, oldControlXml);
			switcheroo(xml, "description", newControlXml, oldControlXml);
			switcheroo(xml, "powerSearchFriendlyName", newControlXml, oldControlXml);

			// add new xml and remove old
			parentXml.insertBefore(newControlXml, oldControlXml);
			it.remove();

			i++;
		}

		return i;
	}

	private interface HandlerAttributesCallback<T extends FakeEmptyControl>
	{
		String getXpath(int index);

		void mapHandlerAttributes(CustomControl newControl, T oldControl);
	}

	private static class DefaultHandlerAttributesCallback<T extends FakeEmptyControl>
		implements
			HandlerAttributesCallback<T>
	{
		protected final String handlerType;
		protected final String xpathModifier;
		protected boolean multiple;

		protected DefaultHandlerAttributesCallback(String handlerType, String xpathModifier, boolean multiple)
		{
			this.handlerType = handlerType;
			this.xpathModifier = xpathModifier;
			this.multiple = multiple;
		}

		protected DefaultHandlerAttributesCallback(String handlerType, boolean multiple)
		{
			this(handlerType, null, multiple);
		}

		protected DefaultHandlerAttributesCallback(String handlerType)
		{
			this(handlerType, null, true);
		}

		@Override
		public String getXpath(int index)
		{
			return handlerType + (xpathModifier == null ? "" : xpathModifier) + (index == 0 ? "" : index);
		}

		@Override
		public void mapHandlerAttributes(CustomControl newControl, T oldControl)
		{
			UniversalSettings settings = new UniversalSettings(newControl);
			settings.setAttachmentTypes(Collections.singleton(handlerType));
			settings.setMultipleSelection(isMultiple());
			mapExtra(newControl, oldControl);
		}

		protected void mapExtra(CustomControl newControl, T oldControl)
		{
			// Nothing by default
		}

		public boolean isMultiple()
		{
			return multiple;
		}

		public void setMultiple(boolean multiple)
		{
			this.multiple = multiple;
		}
	}

	private static void switcheroo(XmlDocument xml, String nodeToSwitch, Node newParent, Node oldParent)
	{
		Node newNode = xml.node(nodeToSwitch, newParent);
		if( newNode != null )
		{
			newParent.removeChild(newNode);
		}
		final Node oldNode = xml.node(nodeToSwitch, oldParent);
		if( oldNode != null )
		{
			newParent.appendChild(oldNode);
		}
	}

	// Fake controls below
	// YouTube, Books, ITunesU and Resource selector are all CustomControl

	public static class FakeItemFinder extends FakeEmptyControl
	{
		private static final long serialVersionUID = 1;

		boolean multiple;
		List<String> importPaths;

		public boolean isMultiple()
		{
			return multiple;
		}

		public void setMultiple(boolean multiple)
		{
			this.multiple = multiple;
		}

		public List<String> getImportPaths()
		{
			return importPaths;
		}
	}

	public static class FakeFileControl extends FakeEmptyControl
	{
		private static final long serialVersionUID = 1L;

		boolean unzip;
		boolean scanmetadata;
		boolean webdav;
		boolean toplevel;

		public boolean isUnzip()
		{
			return unzip;
		}

		public void setUnzip(boolean unzip)
		{
			this.unzip = unzip;
		}

		public boolean isScanmetadata()
		{
			return scanmetadata;
		}

		public void setScanmetadata(boolean scanmetadata)
		{
			this.scanmetadata = scanmetadata;
		}

		public boolean isWebdav()
		{
			return webdav;
		}

		public void setWebdav(boolean webdav)
		{
			this.webdav = webdav;
		}

		public boolean isToplevel()
		{
			return toplevel;
		}

		public void setToplevel(boolean toplevel)
		{
			this.toplevel = toplevel;
		}
	}

	public static class FakeCustomControl extends FakeEmptyControl
	{
		private static final long serialVersionUID = 1;

		Map<Object, Object> attributes = new HashMap<Object, Object>();
		private String classType;

		public Map<Object, Object> getAttributes()
		{
			return attributes;
		}

		@Override
		public String getClassType()
		{
			return classType;
		}
	}

	// Also serves as IMS control
	public static class FakeEmptyControl implements Serializable
	{
		private static final long serialVersionUID = 1L;

		boolean mandatory;
		boolean reload;
		boolean include;
		int size1;
		int size2;
		String customName;
		LanguageBundle title;
		LanguageBundle description;
		String script;
		List<TargetNode> targetnodes = new ArrayList<TargetNode>();
		final List<WizardControlItem> items = new ArrayList<WizardControlItem>();
		String validateScript;
		String afterSaveScript;
		LanguageBundle powerSearchFriendlyName;

		public String getClassType()
		{
			return null;
		}

		public boolean isMandatory()
		{
			return mandatory;
		}

		public boolean isReload()
		{
			return reload;
		}

		public boolean isInclude()
		{
			return include;
		}

		public String getAfterSaveScript()
		{
			return afterSaveScript;
		}

		public String getCustomName()
		{
			return customName;
		}

		public String getScript()
		{
			return script;
		}

		public String getValidateScript()
		{
			return validateScript;
		}

		public List<TargetNode> getTargetnodes()
		{
			return targetnodes;
		}
	}

	// For Links control
	public static class FakeMultiControl extends FakeEmptyControl
	{
		private static final long serialVersionUID = 1;

		List<WizardControl> controls = new ArrayList<WizardControl>();

		public List<WizardControl> getControls()
		{
			return controls;
		}

		@Override
		public String getClassType()
		{
			return null;
		}
	}

	public static class FakeEditBox extends FakeEmptyControl
	{
		private static final long serialVersionUID = 1;

		static final String CLASS = "editbox";

		boolean links;
		boolean number;
		boolean forceUnique;
		boolean checkDuplication;
		boolean allowMultiLang;
	}
}