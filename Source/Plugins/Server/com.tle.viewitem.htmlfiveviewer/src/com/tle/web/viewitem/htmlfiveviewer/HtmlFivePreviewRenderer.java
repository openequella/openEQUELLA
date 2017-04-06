package com.tle.web.viewitem.htmlfiveviewer;

import com.tle.beans.item.attachments.Attachment;
import com.tle.core.services.FileSystemService;
import com.tle.web.searching.VideoPreviewRenderer;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.viewable.ViewableItem;

public class HtmlFivePreviewRenderer implements VideoPreviewRenderer
{
	@Override
	public void preRender(PreRenderContext context)
	{
		//Nothing
	}

	@Override
	public SectionRenderable renderPreview(RenderContext context, Attachment attachment, ViewableItem<?> vitem,
		String mimeType)
	{
		if( supports(mimeType) )
		{
			String video = "video controls style=\"width:320px;height:180px;\"";
			TagRenderer videoTag = new TagRenderer(video, new TagState());

			String previewVideoPath = attachment.getThumbnail();
			if( previewVideoPath != null )
			{
				previewVideoPath = previewVideoPath.replaceFirst(FileSystemService.THUMBS_FOLDER,
					FileSystemService.VIDEO_PREVIEW_FOLDER).replaceAll(FileSystemService.THUMBNAIL_EXTENSION,
					FileSystemService.VIDEO_PREVIEW_EXTENSION);

				String videoUrl = vitem.createStableResourceUrl(previewVideoPath).getHref();
				String source = "source src=\"" + videoUrl + "\" type=\"video/mp4\"";
				TagRenderer sourceTag = new TagRenderer(source, new TagState());

				videoTag.setNestedRenderable(sourceTag);

				return videoTag;
			}
		}
		return null;
	}

	@Override
	public boolean supports(String mimeType)
	{
		if( mimeType.startsWith("video") )
		{
			return true;
		}
		return false;
	}

}
