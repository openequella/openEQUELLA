package com.tle.web.api.staging.interfaces.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tle.web.api.interfaces.beans.RestBean;

public class MultipartBean implements RestBean
{
	private final String uploadId;

	@JsonCreator
	public MultipartBean(@JsonProperty("uploadId") String uploadId)
	{
		this.uploadId = uploadId;
	}

	public String getUploadId()
	{
		return uploadId;
	}
}