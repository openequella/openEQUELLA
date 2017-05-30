package com.tle.web.controls.universal.handlers.fileupload;

import java.io.Serializable;
import java.util.List;

import com.tle.beans.item.attachments.Attachment;
import com.tle.common.FileSizeUtils;
import com.tle.common.PathUtils;
import com.tle.web.controls.universal.UniversalAttachment;

/**
 * @author Aaron
 */
public class UploadedFile implements Serializable, UniversalAttachment
{
	private static final long serialVersionUID = 1L;

	private final String uuid;
	private String fileUploadUuid;

	private boolean detailEditing;
	private String intendedFilepath;
	private String filepath;
	private String description;
	private boolean finished;
	private long size;
	private String md5;
	private String mimeType;
	private String viewer;
	private String problemKey;

	private String potentialType;
	private String resolvedType;
	// E.g if resolvedType == package then a sub type could be QTI
	private String resolvedSubType;
	private Attachment attachment;

	private String filename;

	// Zip stuff
	private String extractedPath;
	// Will be null if not a package or if unknown
	private List<String> packageTypes;
	private boolean cancelled;

	public UploadedFile(String uuid)
	{
		this.uuid = uuid;
	}

	public String getUuid()
	{
		return uuid;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isFinished()
	{
		return finished;
	}

	public void setFinished(boolean finished)
	{
		this.finished = finished;
	}

	public String getPotentialType()
	{
		return potentialType;
	}

	public void setPotentialType(String potentialType)
	{
		this.potentialType = potentialType;
	}

	public String getResolvedType()
	{
		return resolvedType;
	}

	public void setResolvedType(String resolvedType)
	{
		setPotentialType(resolvedType);
		this.resolvedType = resolvedType;
	}

	public String getResolvedSubType()
	{
		return resolvedSubType;
	}

	public void setResolvedSubType(String resolvedSubType)
	{
		this.resolvedSubType = resolvedSubType;
	}

	public long getSize()
	{
		return size;
	}

	public String getHumanReadableSize()
	{
		return FileSizeUtils.humanReadableFileSize(size);
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public String getMd5()
	{
		return md5;
	}

	public void setMd5(String md5)
	{
		this.md5 = md5;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public String getViewer()
	{
		return viewer;
	}

	public void setViewer(String viewer)
	{
		this.viewer = viewer;
	}

	public String getProblemKey()
	{
		return problemKey;
	}

	public void setProblemKey(String problemKey)
	{
		this.problemKey = problemKey;
	}

	public boolean isErrored()
	{
		return problemKey != null;
	}

	@Override
	public Attachment getAttachment()
	{
		return attachment;
	}

	public String getFileUploadUuid()
	{
		return fileUploadUuid;
	}

	public void setFileUploadUuid(String fileUploadUuid)
	{
		this.fileUploadUuid = fileUploadUuid;
	}

	public void setAttachment(Attachment attachment)
	{
		this.attachment = attachment;
	}

	/**
	 * @return The filepath sans _uploads folder
	 */
	public String getIntendedFilepath()
	{
		return intendedFilepath;
	}

	/**
	 * @param intendedFilepath The filepath sans _uploads folder
	 */
	public void setIntendedFilepath(String intendedFilepath)
	{
		this.intendedFilepath = intendedFilepath;
		this.filename = PathUtils.getFilenameFromFilepath(intendedFilepath);
	}

	public String getFilepath()
	{
		return filepath;
	}

	public void setFilepath(String filepath)
	{
		this.filepath = filepath;
	}

	public String getFilename()
	{
		return filename;
	}

	public String getExtractedPath()
	{
		return extractedPath;
	}

	public void setExtractedPath(String extractedPath)
	{
		this.extractedPath = extractedPath;
	}

	public boolean isDetailEditing()
	{
		return detailEditing;
	}

	public void setDetailEditing(boolean detailEditing)
	{
		this.detailEditing = detailEditing;
	}

	/**
	 * @return If the file is a supported package type then return types that it
	 *         could be (e.g "QTITEST" + "IMS" are both possibilities for the
	 *         same package). Note that the user may elect to not treat it like
	 *         a package. Returns null if not yet known (or empty list if not a
	 *         package).
	 */
	public List<String> getPackageTypes()
	{
		return packageTypes;
	}

	/**
	 * @param packageType If the file is a supported package type then this is
	 *            the lowest level type that it could be. Note that the user may
	 *            elect to not treat it like a package.
	 */
	public void setPackageTypes(List<String> packageTypes)
	{
		this.packageTypes = packageTypes;
	}

	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
	}

	public boolean isCancelled()
	{
		return cancelled;
	}
}
