package com.tle.common.harvester;

import java.net.URL;
import java.util.List;

import com.tle.common.NameValue;

public interface RemoteOAIHarvesterService
{
	/**
	 * Gets a list of sets for an OAI endpoint
	 * 
	 * @param url Server url including the oai context
	 * @return NameValue list of name/spec id
	 */
	List<NameValue> listSets(URL url) throws Exception;

	List<String> listMetadataFormats(URL url) throws Exception;
}
