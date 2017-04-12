package com.tle.core.connectors.blackboard.service;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.exception.LmsUserNotFoundException;

public interface BlackboardConnectorService
{
	@SuppressWarnings("nls")
	String REGISTER_PROXY_TOOL_RESULT_ALREADY_REGISTERED = "$ALREADY_REG$";

	/**
	 * @param serverUrl The BB server
	 * @param password
	 * @return An error message, or null if successful. It may return
	 *         {@link BlackboardConnectorService.REGISTER_PROXY_TOOL_RESULT_ALREADY_REGISTERED}
	 *         so you should be prepared to handle this.
	 */
	String registerProxyTool(String serverUrl, String password);

	/**
	 * Invokes the synchronise content webservice method on the Blackboard
	 * server
	 * 
	 * @param connector
	 */
	void synchroniseEquellaContent(Connector connector, String username) throws LmsUserNotFoundException;

	/**
	 * Secrets are stored in a per-URL manner. I.e. if you have two BB
	 * connectors with an identical URL then the same secret is used. The secret
	 * is only updated upon registerProxyTool (or via a manual override but this
	 * is an edge case)
	 * 
	 * @param serverUrl
	 * @return
	 */
	String getSecret(String serverUrl);

	/**
	 * Only invoke this if user chooses to manually override the previously
	 * stored one
	 * 
	 * @param serverUrl
	 * @param secret
	 */
	void setSecret(String serverUrl, String secret);

	/**
	 * @param connector
	 * @param username Actual username, not modified at all
	 * @return
	 */
	String testConnection(String serverUrl, String username);
}
