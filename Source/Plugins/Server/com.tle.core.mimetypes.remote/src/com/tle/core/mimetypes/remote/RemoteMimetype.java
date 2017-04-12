package com.tle.core.mimetypes.remote;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.NameValue;
import com.tle.common.mimetypes.RemoteMimetypeService;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;

@Bind
public class RemoteMimetype implements RemoteMimetypeService {

	@Inject
	private MimeTypeService mime;

	@Override
	public List<NameValue> listAll() {
		List<MimeEntry> mimeEntries = mime.searchByMimeType(Constants.BLANK, 0,
				-1).getResults();

		List<NameValue> mimeTypes = new ArrayList<NameValue>();

		for (MimeEntry entry : mimeEntries) {
			mimeTypes.add(new NameValue(entry.getType(), entry.getType()));
		}
		return mimeTypes;
	}

}
