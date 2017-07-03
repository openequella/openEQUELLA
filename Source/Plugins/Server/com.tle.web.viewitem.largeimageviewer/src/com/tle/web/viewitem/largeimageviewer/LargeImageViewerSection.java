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

package com.tle.web.viewitem.largeimageviewer;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.services.FileSystemService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.NavBar;
import com.tle.web.sections.equella.component.NavBarBuilder;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryDraggable;
import com.tle.web.sections.jquery.libraries.JQueryDroppable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.ElementIdExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.modules.StandardModule;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.FullScreen;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.UseViewer;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

@SuppressWarnings("nls")
@Bind
public class LargeImageViewerSection extends AbstractViewerSection<LargeImageViewerSection.LargeImageViewerModel>
	implements
		ViewItemFilter
{
	private static final Logger LOGGER = Logger.getLogger(LargeImageViewerSection.class);
	private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);
	private static final long RELOAD_INTERVAL = TimeUnit.SECONDS.toMillis(7);
	private static final int[] ROTATION_TO_ANGLE = new int[]{270, 180, 90};

	@PlugKey("navviewer.title")
	private static Label TITLE_LABEL;
	@PlugKey("hideThumb")
	private static String HIDE_THUMB_KEY;
	@PlugKey("showThumb")
	private static String SHOW_THUMB_KEY;

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private LargeImageViewer largeImageViewer;
	@Inject
	private ViewItemUrlFactory itemUrls;
	@Inject
	private BundleCache bundleCache;

	@EventFactory
	private EventGenerator events;
	@Component
	private Button startButton;
	@TreeLookup
	private RootItemFileSection rootSection;

	@Inject
	@Component
	private NavBar navBar;
	@Component
	private Link title;

	@Component
	@PlugKey(value = "nav.zoomin", icon = Icon.ZOOM_IN)
	private Link zoomIn;
	@Component
	@PlugKey(value = "nav.zoomout", icon = Icon.ZOOM_OUT)
	private Link zoomOut;

	@Component
	@PlugKey(value = "nav.rotateleft", icon = Icon.ROTATE_LEFT)
	private Link rotateLeft;
	@Component
	@PlugKey(value = "nav.rotateright", icon = Icon.ROTATE_RIGHT)
	private Link rotateRight;

	@Component
	@PlugKey("hideThumb")
	private Link hideThumb;

	@Override
	public String getDefaultPropertyName()
	{
		return "liv";
	}

	@Override
	public Class<LargeImageViewerModel> getModelClass()
	{
		return LargeImageViewerModel.class;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		startButton.setClickHandler(events.getNamedHandler("startProcessing"));

		navBar.setTitle(title);
		NavBarBuilder nbb = navBar.buildRight();
		nbb.divider().action(zoomIn).action(zoomOut);
		nbb.divider().action(rotateLeft).action(rotateRight);
		nbb.divider().action(hideThumb);

		// Init the javascript
		JSCallable function = new ExternallyDefinedFunction("initializeGraphic", JQueryDraggable.PRERENDER,
			JQueryDroppable.PRERENDER);
		title.addReadyStatements(new ScriptStatement(new FunctionCallExpression(function, new ElementIdExpression(
			zoomIn), new ElementIdExpression(zoomOut), new ElementIdExpression(rotateLeft), new ElementIdExpression(
			rotateRight), new ElementIdExpression(hideThumb), CurrentLocale.get(HIDE_THUMB_KEY), CurrentLocale
			.get(SHOW_THUMB_KEY))));
	}

	public Button getStartButton()
	{
		return startButton;
	}

	public NavBar getNavBar()
	{
		return navBar;
	}

	@EventHandlerMethod
	public void startProcessing(SectionInfo info)
	{
		LargeImageViewerModel model = getModel(info);
		model.setStartProcess(true);
		model.setAutoPageRefresh(false); // this gets changed later
		model.setMethod("view");
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addFilterViewer(this);
	}

	@Override
	public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
	{
		LargeImageViewerModel model = getModel(info);
		String method = model.getMethod();
		if( method == null )
		{
			return resource;
		}
		if( "tile".equals(method) )
		{
			int rotation = model.getRotation();
			if( rotation < 1 || rotation > 3 )
			{
				return resource;
			}
		}
		return new UseViewer(resource, this);
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		LargeImageViewerModel model = getModel(info);
		if( !Objects.equals(model.getMethod(), "tile") )
		{
			FileHandle tileBaseHandle = largeImageViewer.getTileBaseHandle(resource.getViewableItem(),
				resource.getFilepath());

			if( getTiledImageProperties(info, tileBaseHandle) != null )
			{
				return resource.getViewAuditEntry();
			}
		}
		return null;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		final Decorations decorations = Decorations.getDecorations(info);
		decorations.setFullscreen(FullScreen.YES_WITH_TOOLBAR);
		decorations.setTitle(TITLE_LABEL);

		final LargeImageViewerModel model = getModel(info);
		if( Objects.equals(model.getMethod(), "tile") )
		{
			decorations.clearAllDecorations();
			return tile(info, resource);
		}

		final ViewableItem<Item> viewableItem = resource.getViewableItem();
		final FileHandle tileBaseHandle = largeImageViewer.getTileBaseHandle(viewableItem, resource.getFilepath());

		final File tileBaseFolder = fileSystemService.getExternalFile(tileBaseHandle, null);

		final Properties tiledImageProps = getTiledImageProperties(info, tileBaseHandle);
		if( tiledImageProps != null )
		{
			decorations.clearAllDecorations();

			// Image has been successfully tiled previously
			model.setTileBaseUri(URLUtils.urlEncode(
				viewableItem.getItemdir() + largeImageViewer.getTileBasePath(resource.getFilepath()) + '/', false));
			model.setJsGeometry(tiledImageProps.getProperty("geometry"));

			final Item item = viewableItem.getItem();
			title
				.setLabel(info, new IconLabel(Icon.BACK, new BundleLabel(item.getName(), item.getUuid(), bundleCache)));
			title.setBookmark(info, itemUrls.createItemUrl(info, resource.getViewableItem().getItem().getItemId(),
				ViewItemUrl.FLAG_IS_RESOURCE));

			return viewFactory.createTemplateResult("viewimage.ftl", this);
		}

		decorations.setMenuMode(MenuMode.HIDDEN);

		boolean confirmStart = false;

		final long lastModified = fileSystemService.mostRecentModification(tileBaseHandle, null);
		final String error = largeImageViewer.getError(tileBaseFolder);

		if( error == null && lastModified > System.currentTimeMillis() - TIMEOUT )
		{
			// Tell them it's processing
			setupModelForProcessing(model);
		}
		else if( model.isStartProcess() )
		{
			// Start the process
			startTileProcessor(resource, tileBaseHandle);
			setupModelForProcessing(model);
		}
		else
		{
			if( error != null )
			{
				// Ask them if they want to restart the processing
				model.setMessageKey("msg.processcrash");
				model.setStackTrace(error);
				confirmStart = true;
			}
			// Note: isAutoPageRefresh means it's already processing
			else if( lastModified == 0 && !model.isAutoPageRefresh() )
			{
				// Ask if they want to process the image for the first time
				model.setMessageKey("msg.unprocessed");
				confirmStart = true;
			}
		}

		model.setImageFilePath(resource.getFilepath());
		model.setConfirmStartProcess(confirmStart);

		if( !confirmStart )
		{
			// append reloading script
			JQueryCore.appendReady(info, new FunctionCallStatement(StandardModule.SET_TIMEOUT,
				new ReloadFunction(false), RELOAD_INTERVAL));
		}

		return viewFactory.createTemplateResult("imageprocessing.ftl", this);
	}

	public SectionResult tile(SectionInfo info, ViewItemResource resource)
	{
		LargeImageViewerModel model = getModel(info);

		int rotation = model.getRotation();
		if( rotation < 1 || rotation > 3 )
		{
			info.forwardToUrl(resource.createCanonicalURL().getHref());
			return null;
		}
		int angle = ROTATION_TO_ANGLE[rotation - 1];
		try
		{
			String filepath = getRotatedFilename(resource.getFilepath(), angle);
			File rotatedFile = fileSystemService.getExternalFile(resource.getViewableItem().getFileHandle(), filepath);

			if( !rotatedFile.exists() )
			{
				// image magick rotate on demand!
				File sourceFile = fileSystemService.getExternalFile(resource.getViewableItem().getFileHandle(),
					resource.getFilepath());
				largeImageViewer.rotateImage(sourceFile, rotatedFile, angle);
			}
			info.forwardToUrl(resource.getViewableItem().createStableResourceUrl(filepath).getHref());
		}
		catch( Exception ex )
		{
			LOGGER.error("Error rotating tile", ex);
			throw new SectionsRuntimeException(ex);
		}
		return null;
	}

	private String getRotatedFilename(String originalFilename, int angle)
	{
		Pair<String, String> fileBits = PathUtils.fileParts(originalFilename);
		return fileBits.getFirst() + "_r" + angle + '.' + fileBits.getSecond();
	}

	/**
	 * @param tilesFolder
	 * @return Can return null if the previous tiling never finised without
	 *         error (or no tiling has ever occurred)
	 */
	private Properties getTiledImageProperties(SectionInfo info, FileHandle tileBaseHandle)
	{
		LargeImageViewerModel model = getModel(info);
		Properties tiledImageProperties = model.getTiledImageProperties();
		if( tiledImageProperties == null )
		{
			tiledImageProperties = largeImageViewer.getTileProperties(fileSystemService.getExternalFile(tileBaseHandle,
				null));
			model.setTiledImageProperties(tiledImageProperties);
		}
		return tiledImageProperties;
	}

	private void startTileProcessor(ViewItemResource resource, FileHandle tileBaseHandle)
	{
		final File originalImage = fileSystemService.getExternalFile(resource.getViewableItem().getFileHandle(),
			resource.getFilepath());
		final File destFolder = fileSystemService.getExternalFile(tileBaseHandle, null);

		largeImageViewer.startTileProcessor(Collections.singleton(new Pair<File, File>(originalImage, destFolder)));
	}

	private void setupModelForProcessing(LargeImageViewerModel model)
	{
		model.setMessageKey("msg.processing");
		model.setAutoPageRefresh(true);
		model.setStartProcess(false);
	}

	public static class LargeImageViewerModel
	{
		@Bookmarked
		private String method;
		@Bookmarked(name = "s")
		private boolean startProcess;
		@Bookmarked(stateful = false)
		private int rotation;
		@Bookmarked(name = "a")
		private boolean autoPageRefresh;

		private Properties tiledImageProperties;
		private String tileBaseUri;
		private String jsGeometry;
		private String messageKey;
		private boolean confirmStartProcess;
		private String imageFilePath;
		private String stackTrace;

		public String getTileBaseUri()
		{
			return tileBaseUri;
		}

		public void setTileBaseUri(String tileBaseUri)
		{
			this.tileBaseUri = tileBaseUri;
		}

		public String getJsGeometry()
		{
			return jsGeometry;
		}

		public void setJsGeometry(String jsGeometry)
		{
			this.jsGeometry = jsGeometry;
		}

		public int getRotation()
		{
			return rotation;
		}

		public void setRotation(int rotation)
		{
			this.rotation = rotation;
		}

		public String getMessageKey()
		{
			return messageKey;
		}

		public void setMessageKey(String message)
		{
			this.messageKey = message;
		}

		public boolean isConfirmStartProcess()
		{
			return confirmStartProcess;
		}

		public void setConfirmStartProcess(boolean showConfirm)
		{
			this.confirmStartProcess = showConfirm;
		}

		public boolean isAutoPageRefresh()
		{
			return autoPageRefresh;
		}

		public void setAutoPageRefresh(boolean autoPageRefresh)
		{
			this.autoPageRefresh = autoPageRefresh;
		}

		public boolean isStartProcess()
		{
			return startProcess;
		}

		public void setStartProcess(boolean startProcess)
		{
			this.startProcess = startProcess;
		}

		public String getImageFilePath()
		{
			return imageFilePath;
		}

		public void setImageFilePath(String imageFilePath)
		{
			this.imageFilePath = imageFilePath;
		}

		public String getMethod()
		{
			return method;
		}

		public void setMethod(String method)
		{
			this.method = method;
		}

		public Properties getTiledImageProperties()
		{
			return tiledImageProperties;
		}

		public void setTiledImageProperties(Properties tiledImageProperties)
		{
			this.tiledImageProperties = tiledImageProperties;
		}

		public String getStackTrace()
		{
			return stackTrace;
		}

		public void setStackTrace(String stackTrace)
		{
			this.stackTrace = stackTrace;
		}
	}

	@Override
	public int getOrder()
	{
		return 0;
	}
}
