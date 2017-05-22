package com.tle.core.adminconsole;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.dytech.edge.common.Constants;
import com.tle.admin.boot.Bootstrap;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.web.resources.ResourcesService;

/**
 * @author jolz
 */
@Bind
public class TleJnlpDownloadServlet extends HttpServlet
{
	/**
	 * auto-generated
	 */
	private static final long serialVersionUID = -8559259386758382735L;
	// required by inner classes
	@Inject
	private UrlService urlService;
	@Inject
	private UserService userService;
	@Inject
	private ResourcesService resourcesService;

	private Document jnlpDocument;

	private XMLOutputter xmlOut;

	@SuppressWarnings("nls")
	@Override
	public void init() throws ServletException
	{
		Format format = Format.getRawFormat();
		format.setOmitDeclaration(true);
		// prettyFormat.setIndent("\t");
		xmlOut = new XMLOutputter(format);
		SAXBuilder saxBuilder = new SAXBuilder(false);
		try
		{
			jnlpDocument = saxBuilder.build(getClass().getResource("admin.jnlp"));
		}
		catch( JDOMException e )
		{
			throw new RuntimeException(e);
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		super.service(request, response);
	}

	@SuppressWarnings("nls")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setContentType("application/x-java-jnlp-file");
		resp.setHeader("Cache-Control", "private, max-age=5, must-revalidate");
		Document locJnlpDocument = (Document) this.jnlpDocument.clone();
		Element jnlpElem = locJnlpDocument.getRootElement();
		String instUrl = urlService.getInstitutionUrl().toString();
		jnlpElem.setAttribute("codebase", instUrl);
		Element resources = jnlpElem.getChild("resources");

		String token = userService.getGeneratedToken(Constants.APPLET_SECRET_ID, CurrentUser.getUsername());

		String tokenEncoded = Base64.encodeBase64String(token.getBytes("UTF-8")).replace("\r", "").replace("\n", "");
		resources.addContent(createJar(resourcesService.getUrl("com.tle.web.adminconsole", "adminconsole.jar")));
		resources.addContent(createProperty(Bootstrap.TOKEN_PARAMETER, tokenEncoded));
		resources.addContent(createProperty(Bootstrap.ENDPOINT_PARAMETER, instUrl));
		resources.addContent(createProperty(Bootstrap.LOCALE_PARAMETER, CurrentLocale.getLocale().toString()));
		resources.addContent(createProperty(Bootstrap.INSTITUTION_NAME_PARAMETER, CurrentInstitution.get().getName()));
		xmlOut.output(locJnlpDocument, resp.getWriter());
	}

	@SuppressWarnings("nls")
	private Element createProperty(String name, String value)
	{
		Element prop = new Element("property");
		prop.setAttribute("name", name);
		prop.setAttribute("value", value);
		return prop;
	}

	@SuppressWarnings("nls")
	private Element createJar(String url)
	{
		Element jarElem = new Element("jar");
		jarElem.setAttribute("href", url);
		return jarElem;
	}

}
