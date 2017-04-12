package com.tle.core.services;

import hurl.build.QueryBuilder;

import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.tle.beans.Institution;

/**
 * @author Nicholas Read
 */
public interface UrlService
{
	/**
	 * In nearly every single case, you should be calling getInstitutionUrl().
	 * This method will not do as expected in 3.1 or above.
	 */
	URL getAdminUrl();

	URL getInstitutionUrl();

	URI getInstitutionUri();

	URL getInstitutionUrl(Institution institution);

	String institutionalise(String url);

	URI getUriForRequest(HttpServletRequest request, String parameters);

	QueryBuilder getQueryBuilderForRequest(HttpServletRequest request);

	/**
	 * Does not check to see if the url is actually an institution URL in the
	 * first place. Take care.
	 * 
	 * @param url
	 * @return
	 */
	String removeInstitution(String url);

	boolean isInstitutionUrl(String url);

	boolean isRelativeUrl(String url);

	URI getBaseUriFromRequest(HttpServletRequest request);
}
