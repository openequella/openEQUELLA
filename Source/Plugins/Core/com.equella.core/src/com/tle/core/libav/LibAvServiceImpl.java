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

package com.tle.core.libav;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.services.FileSystemService;

@Bind(LibAvService.class)
@Singleton
@SuppressWarnings("nls")
public class LibAvServiceImpl implements LibAvService
{
	private String libAvPath;
	private File avconvExe;
	private File avprobeExe;
	@Inject
	private ObjectMapperService objectMapperService;
	@Inject
	private FileSystemService fileSystemService;

	final int MAX_VIDEO_DIMENSTIONS = 320;
	final int MIN_VIDEO_LENGTH = 5;
	final int TRANSCODE_LENGTH = 10;

	@Inject(optional = true)
	public void setLibAvPath(@Named("libav.path") String libAvPath)
	{
		this.libAvPath = libAvPath.trim();
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception
	{
		if( libAvPath != null )
		{
			final File libAvDir = new File(libAvPath);
			avconvExe = ExecUtils.findExe(libAvDir, "avconv");
			avprobeExe = ExecUtils.findExe(libAvDir, "avprobe");
			if( avprobeExe == null || avconvExe == null )
			{
				throw new RuntimeException(
					"LibAv was not found, specifically the avconvert and avprobe programs.  The configured path is "
						+ libAvDir.getCanonicalPath());
			}
		}
	}

	@Override
	public void screenshotVideo(File srcFile, File dstFile) throws IOException
	{
		ObjectNode videoJson = getVideoInfo(srcFile);
		int videoDuration = getVideoDuration(videoJson);
		ExecResult result;
		// no duration in metadata, lets just take the first frame
		if( videoDuration == 0 )
		{
			result = ExecUtils.exec(avconvExe.getAbsolutePath(), "-i",
				new String(srcFile.getAbsolutePath().getBytes("UTF-8")), "-b", "350k", "-q:v", "1", "-vframes", "1",
				new String(dstFile.getAbsolutePath().getBytes("UTF-8")));
			result.ensureOk();
		}
		else if( videoDuration > MIN_VIDEO_LENGTH )
		{
			result = ExecUtils.exec(avconvExe.getAbsolutePath(), "-ss", "00:00:0" + MIN_VIDEO_LENGTH, "-i",
				new String(srcFile.getAbsolutePath().getBytes("UTF-8")), "-b", "350k", "-q:v", "1", "-vframes", "1",
				new String(dstFile.getAbsolutePath().getBytes("UTF-8")));
			result.ensureOk();
		}
		// video is under 5 seconds, screenshot half way into video
		else
		{
			result = ExecUtils.exec(avconvExe.getAbsolutePath(), "-ss", "00:00:0" + videoDuration / 2, "-i",
				new String(srcFile.getAbsolutePath().getBytes("UTF-8")), "-b", "350k", "-q:v", "1", "-vframes", "1",
				new String(dstFile.getAbsolutePath().getBytes("UTF-8")));
			result.ensureOk();
		}
	}

	@Override
	public void generatePreviewVideo(FileHandle handle, String filename) throws IOException
	{
		if( !isLibavInstalled() )
		{
			return;
		}
		List<String> opts = new ArrayList<String>();
		final String thumbFile = FileSystemService.VIDEO_PREVIEW_FOLDER + '/' + filename + ".mp4";
		final File srcFile = fileSystemService.getExternalFile(handle, filename);
		final File dstFile = fileSystemService.getExternalFile(handle, thumbFile);
		boolean madeDirs = dstFile.getParentFile().mkdirs();
		ObjectNode videoJson = getVideoInfo(srcFile);
		Dimension vidDimensions = getVideoDimensions(videoJson);
		int videoDuration = getVideoDuration(videoJson);

		if( !(madeDirs || dstFile.getParentFile().exists()) )
		{
			throw new IOException("Could not create/confirm directory " + dstFile.getParentFile().getAbsolutePath());
		}

		opts.add(avconvExe.getAbsolutePath());
		// skip 5 into the video, and capture 10 seconds. but only if it's long
		// enough
		if( videoDuration > MIN_VIDEO_LENGTH )
		{
			// video is long enough to capture 10 seconds from 5 seconds into
			// the
			// video
			if( videoDuration >= MIN_VIDEO_LENGTH + TRANSCODE_LENGTH )
			{
				opts.add("-ss");
				opts.add("00:00:0" + MIN_VIDEO_LENGTH);
			}
			// how long to transcode
			opts.add("-t");
			opts.add(Integer.toString(TRANSCODE_LENGTH));
		}
		opts.add("-i");
		opts.add(new String(srcFile.getAbsolutePath().getBytes("UTF-8")));
		// bitrate of transcoded video
		opts.add("-b");
		opts.add("350k");
		// audio sampling rate (experimental)
		opts.add("-ar");
		opts.add("44100");
		// audio codec
		opts.add("-acodec");
		opts.add("libvo_aacenc");
		// overwrite if file already exists
		opts.add("-y");
		if( vidDimensions.width > MAX_VIDEO_DIMENSTIONS || vidDimensions.height > MAX_VIDEO_DIMENSTIONS )
		{
			// scaling needful
			opts.add("-vf");
			if( vidDimensions.width > vidDimensions.height )
			{
				// wide video
				opts.add("scale=320:trunc\\(ow/a/2\\)*2");
			}
			else if( vidDimensions.height > vidDimensions.width )
			{
				// tall video
				opts.add("scale=trunc\\(oh*a/2\\)*2:320");
			}
			else
			{
				// it's hip to be a square
				opts.add("scale=320:320");
			}
		}
		opts.add(new String(dstFile.getAbsolutePath().getBytes("UTF-8")));
		ExecResult result = ExecUtils.exec(opts);
		result.ensureOk();
	}

	private int getVideoDuration(ObjectNode videoJson)
	{
		return videoJson.findValue("duration").asInt();
	}

	private Dimension getVideoDimensions(ObjectNode videoJson)
	{
		return new Dimension(videoJson.findValue("width").asInt(), videoJson.findValue("height").asInt());
	}

	@Override
	public ObjectNode getVideoInfo(File srcFile) throws IOException
	{
		final ObjectMapper mapper = objectMapperService.createObjectMapper();
		ObjectNode videoJson;
		ExecResult result = ExecUtils.exec(avprobeExe.getAbsolutePath(), "-of", "json", "-show_streams",
			srcFile.getAbsolutePath());
		result.ensureOk();
		videoJson = (ObjectNode) mapper.readTree(result.getStdout());
		return videoJson;
	}

	@Override
	public boolean isLibavInstalled()
	{
		if( libAvPath != null )
		{
			return true;
		}
		return false;
	}
}
