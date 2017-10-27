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

package com.tle.core.usermanagement.standard.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.apache.log4j.Logger;

import com.dytech.edge.common.Constants;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.tle.beans.usermanagement.standard.LDAPSettings;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.valuebean.DefaultGroupBean;
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.encryption.EncryptionService;
import com.tle.exceptions.BadCredentialsException;

/**
 * Class which authenticates a user from their LDAP account.
 * <p>
 * Currently both methods are static due to the lack of required functionality.
 * In future it may be required to alter this so that the connection is made to
 * the database, which then retrieves information on that user. However,
 * currently only the authentication is required.
 * 
 * @author cofarrell
 */
@SuppressWarnings("nls")
public class LDAP
{
	public static final String OBJECTCLASS = "objectclass";
	public static final String[] ATTRIBUTES_NONE = {};

	private static final Logger LOGGER = Logger.getLogger(LDAP.class);

	protected final LDAPSettings config;

	private final String defaultDomain;
	private final String atDefaultDomain;

	private final String adminUsername;
	private final String adminPassword;

	private String idField;
	private String usernameField;
	private String firstnameField;
	private String surnameField;
	private String emailField;
	private String memberField;
	private String memberOfField;
	private String groupIdField;
	private String groupNameField;
	private String memberKeyField;

	private final String personObject;
	private final String groupObject;

	private final String[] userAttributes;
	private final String[] groupAttributes;
	private final String[] extGroupAttributes;

	private List<String> baseStrings;
	private List<Name> bases;

	private Map<String, String> env;
	private boolean blind;

	private transient GroupSearch groupSearch;

	public LDAP(LDAPSettings config, EncryptionService encryptionService)
	{
		this.config = config;

		this.personObject = getString(config.getPersonObject(), "person");
		this.groupObject = getString(config.getGroupObject(), Constants.BLANK);

		this.defaultDomain = config.getDefaultDomain();
		this.atDefaultDomain = '@' + defaultDomain;

		this.adminUsername = getString(config.getAdminUsername(), Constants.BLANK);
		String pwd = config.getAdminPassword();
		this.adminPassword = getString(encryptionService.decrypt(pwd), Constants.BLANK);

		initContext();

		this.baseStrings = config.getBases();
		if( Check.isEmpty(this.baseStrings) )
		{
			this.baseStrings = getDNs();
		}

		Map<String, String> revmap = config.getAttributes();
		if( !Check.isEmpty(revmap) )
		{
			idField = revmap.get("id");
			usernameField = revmap.get("username");
			emailField = revmap.get("email");
			firstnameField = revmap.get("givenname");
			surnameField = revmap.get("surname");
			memberField = revmap.get("member");
			memberOfField = revmap.get("memberOf");
			groupIdField = revmap.get("groupId");
			groupNameField = revmap.get("groupName");
			memberKeyField = revmap.get("memberKey");

			if( groupNameField == null )
			{
				groupNameField = groupIdField;
			}

			if( usernameField == null )
			{
				usernameField = idField;
			}
		}
		else
		{
			idField = "cn";
			usernameField = "cn";
			firstnameField = "givenName";
			surnameField = "sn";
			emailField = "mail";
		}

		userAttributes = new String[]{idField, usernameField, emailField, firstnameField, surnameField};
		groupAttributes = new String[]{groupIdField, groupNameField};
		extGroupAttributes = new String[]{groupIdField, groupNameField, memberOfField, memberField};
	}

	private String getString(String id, String defaultString)
	{
		if( Check.isEmpty(id) )
		{
			id = defaultString;
		}
		return id;
	}

	public void initContext()
	{
		String url = config.getUrl();
		if( Check.isEmpty(url) )
		{
			throw new RuntimeException("LDAP URL is empty");
		}

		LOGGER.info("LDAP URL: " + url);

		// Hashtable for environmental information
		env = new Hashtable<String, String>();

		// Specify which class to use for our JNDI Provider
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

		// Specify the host and port to use for directory service
		env.put(Context.PROVIDER_URL, url);

		// read timeout and connection pooling timeout
		env.put("com.sun.jndi.ldap.read.timeout", "15000");

		env.put(Context.REFERRAL, config.isFollow() ? "follow" : "ignore");

		// Java by default handles this just fine
		String version = config.getVersion();
		if( version != null && version.length() > 0 )
		{
			env.put("java.naming.ldap.version", version);
		}

		// Note that validateCert hasn't actually been tested fully
		if( url.startsWith("ldaps") && !config.isValidateCert() )
		{
			env.put("java.naming.ldap.factory.socket", BlindSSLSocketFactory.class.getName());
			blind = true;
		}
	}

	/**
	 * @return null if there are zero results
	 */
	private NamingEnumeration<SearchResult> search(DirContext ctx, Name base, String[] returnAttributes, Filter filter,
		boolean recurse)
	{
		SearchControls ctls = new SearchControls();
		ctls.setCountLimit(filter.getLimit());
		ctls.setReturningAttributes(returnAttributes);
		ctls.setSearchScope(recurse ? SearchControls.SUBTREE_SCOPE : SearchControls.ONELEVEL_SCOPE);

		try
		{
			// Search for objects using the filter
			String query = filter.toFilter();
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Query:" + query + " Base:" + base);
			}
			NamingEnumeration<SearchResult> ne = ctx.search(base, query, ctls);
			if( ne.hasMore() )
			{
				return ne;
			}
		}
		catch( PartialResultException pre )
		{
			LOGGER.info(pre);
		}
		catch( SizeLimitExceededException slee )
		{
			LOGGER.info(slee);
		}
		catch( Exception e )
		{
			LOGGER.warn(e);
		}

		return null;
	}

	public <T> T searchFirstResult(DirContext ctx, Name base, Filter filter, HitsCollector<T> collector,
		boolean recurse) throws NamingException
	{
		collector.setup(ctx, this);

		NamingEnumeration<SearchResult> ne = search(ctx, base, collector.getReturnAttributes(), filter, recurse);
		// The above returns null if there are no results, so assume if not null
		// that there is a first result
		if( ne != null )
		{
			try
			{
				collector.addResult(ne.next(), base);
				return collector.getResults().get(0);
			}
			finally
			{
				// Because we're not going all the way to the end of the
				// enumeration, we have to manually close it.
				ne.close();
			}
		}
		return null;
	}

	public <T> T searchFirstResultAllBases(DirContext ctx, Filter filter, HitsCollector<T> collector, boolean recurse)
	{
		try
		{
			for( Name element : getBases() )
			{
				T rv = searchFirstResult(ctx, element, filter, collector, recurse);
				if( rv != null )
				{
					return rv;
				}
			}
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		return null;
	}

	public <T> List<T> search(DirContext ctx, Name base, Filter filter, HitsCollector<T> collector, boolean recurse)
	{
		collector.setup(ctx, this);

		NamingEnumeration<SearchResult> ne = search(ctx, base, collector.getReturnAttributes(), filter, recurse);
		try
		{
			while( ne != null && ne.hasMore() )
			{
				SearchResult sr = ne.next();
				collector.addResult(sr, base);
			}
		}
		catch( PartialResultException pre )
		{
			LOGGER.info(pre);
		}
		catch( SizeLimitExceededException slee )
		{
			LOGGER.info(slee);
		}
		catch( Exception e )
		{
			LOGGER.error("Error searching", e);
		}

		return collector.getResults();
	}

	public <T> List<T> searchAllBases(DirContext ctx, Filter filter, HitsCollector<T> collector, boolean recurse)
	{
		for( Name element : getBases() )
		{
			search(ctx, element, filter, collector, recurse);
		}
		return collector.getResults();
	}

	public String searchAuthenticate(final String username, final String password)
	{
		if( username.length() == 0 || password.length() == 0 )
		{
			throw new BadCredentialsException("Error authenticating with LDAP");
		}

		LOGGER.debug("Authenticating \"" + username + "\" with searching authentication");

		return doAsAdmin(new InContext<String>()
		{
			@Override
			public String execute(DirContext ctx) throws NamingException
			{
				for( Name element : getBases() )
				{
					LOGGER.debug("Searching for user in DN: " + element);
					String name = searchFirstResult(ctx, element, getUsernameFilter(username),
						new HitsCollector<String>()
					{
						@Override
						public void addResult(SearchResult sr, Name base)
						{
							// This is much better than calling sr.getName()
							// and adding the base
							results.add(sr.getNameInNamespace());
						}

						@Override
						public String[] getReturnAttributes()
						{
							return ATTRIBUTES_NONE;
						}
					}, true);

					if( name != null )
					{
						try
						{
							LOGGER.debug("Authenticating user using DN: " + name);
							DirContext uctx = null;
							try
							{
								uctx = bind(name, password, false, false);
							}
							finally
							{
								if( uctx != null )
								{
									try
									{
										uctx.close();
									}
									catch( NamingException e )
									{
										throw new RuntimeException(e);
									}
								}
							}
							LOGGER.debug("User \"" + username + "\" was authenticated");
							return name;
						}
						catch( AuthenticationException ae )
						{
							throw new BadCredentialsException("Error authenticating with LDAP");
						}
						catch( Exception e )
						{
							LOGGER.warn("Error bind user authentication", e);
						}
					}
				}
				LOGGER.debug("User \"" + username + "\" was NOT authenticated");
				throw new BadCredentialsException("Error authenticating with LDAP");
			}
		});
	}

	public DirContext bind(String username, String password, boolean canBeAnonymous, boolean pool)
		throws NamingException
	{
		try
		{
			return _bind(username, password, canBeAnonymous, pool);
		}
		catch( BadCredentialsException ex )
		{
			if( Check.isEmpty(defaultDomain) )
			{
				throw ex;
			}

			return _bind(username + atDefaultDomain, password, canBeAnonymous, pool);
		}
	}

	private DirContext _bind(String username, String password, boolean canBeAnonymous, boolean pool)
		throws NamingException
	{
		if( !canBeAnonymous && password.length() == 0 )
		{
			throw new BadCredentialsException("Couldn't authenticate with LDAP");
		}

		// We'll ignore Sonar's advice to replace Hashtable with java.util.Map
		Hashtable<String, String> map = new Hashtable<String, String>(env); // NOSONAR
		map.put("com.sun.jndi.ldap.connect.pool", Boolean.toString(pool));
		map.put(Context.SECURITY_PRINCIPAL, username);
		map.put(Context.SECURITY_CREDENTIALS, password);
		map.put(Context.SECURITY_AUTHENTICATION, "simple");

		ClassLoader oldLoader = null;
		try
		{
			if( blind )
			{
				oldLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(LDAP.class.getClassLoader());
			}
			return new InitialDirContext(map);
		}
		catch( AuthenticationException e )
		{
			throw new BadCredentialsException("Couldn't authenticate with LDAP");
		}
		finally
		{
			if( oldLoader != null )
			{
				Thread.currentThread().setContextClassLoader(oldLoader);
			}
		}
	}

	public synchronized List<Name> getBases()
	{
		if( bases == null )
		{
			bases = new ArrayList<Name>(baseStrings.size());
			for( String baseString : baseStrings )
			{
				try
				{
					bases.add(parse(baseString));
				}
				catch( NamingException e )
				{
					LOGGER.warn("Couldn't use base:" + baseString);
				}
			}
		}
		return bases;
	}

	public List<String> getDNs()
	{
		return doAsAdmin(new InContext<List<String>>()
		{
			@Override
			public List<String> execute(DirContext ctx) throws NamingException
			{
				final String namingContextsAttr = "namingcontexts";

				Attributes attrs = ctx.getAttributes("", new String[]{namingContextsAttr});
				if( attrs != null )
				{
					for( NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMore(); )
					{
						Attribute attr = ae.next();
						if( attr.getID().equalsIgnoreCase(namingContextsAttr) )
						{
							List<String> rv = Lists.newArrayList();
							for( NamingEnumeration<?> vals = attr.getAll(); vals.hasMore(); )
							{
								rv.add((String) vals.next());
							}
							return rv;
						}
					}
				}
				return Collections.emptyList();
			}
		});
	}

	protected boolean isSearchAllQuery(String query)
	{
		query = checkQuery(query);
		return query.length() == 0 || query.equals("*");
	}

	public String checkQuery(String query)
	{
		if( !config.isWildcards() )
		{
			while( query.length() > 0 && query.startsWith("*") )
			{
				query = query.substring(1);
			}
		}
		return query;
	}

	public Filter getUserIDFilter(String userid)
	{
		SingleFilter nv1 = new SingleFilter(OBJECTCLASS, personObject);
		SingleFilter nv2 = new SingleFilter(idField, userid);
		return new AndFilter(nv1, nv2);
	}

	public Filter getGroupIDFilter(String groupid)
	{
		SingleFilter nv1 = new SingleFilter(OBJECTCLASS, groupObject);
		SingleFilter nv2 = new SingleFilter(groupIdField, groupid);
		return new AndFilter(nv1, nv2);
	}

	public Filter getGroupNameFilter(String groupName)
	{
		SingleFilter nv1 = new SingleFilter(OBJECTCLASS, groupObject);
		SingleFilter nv2 = new SingleFilter(groupNameField, groupName);
		return new AndFilter(nv1, nv2);
	}

	public Filter getUsernameFilter(String username)
	{
		Filter nv1 = new SingleFilter(OBJECTCLASS, personObject);
		Filter nv2 = new SingleFilter(usernameField, username);
		if( !Check.isEmpty(defaultDomain) )
		{
			nv2 = new OrFilter(nv2, new SingleFilter(usernameField, username + atDefaultDomain));
		}
		return new AndFilter(nv1, nv2);
	}

	public Filter getUserSearchFilter(String query)
	{
		SingleFilter personClass = new SingleFilter(OBJECTCLASS, personObject);
		personClass.setLimit(config.getSearchLimit());

		query = checkQuery(query);
		if( isSearchAllQuery(query) )
		{
			return personClass;
		}

		AndFilter and = new AndFilter(personClass);
		for( String token : Splitter.onPattern("\\s").omitEmptyStrings().trimResults(CharMatcher.is('*')).split(query) )
		{
			token = (config.isWildcards() ? "*" : "") + token + '*';

			OrFilter or = new OrFilter();
			or.addFilter(new SingleFilter(usernameField, token, false));
			or.addFilter(new SingleFilter(firstnameField, token, false));
			or.addFilter(new SingleFilter(surnameField, token, false));

			and.addFilter(or);
		}
		return and;
	}

	public Filter getGroupSearchFilter(String query)
	{
		SingleFilter groupClass = new SingleFilter(OBJECTCLASS, groupObject);
		groupClass.setLimit(config.getSearchLimit());

		query = checkQuery(query);
		if( isSearchAllQuery(query) )
		{
			return groupClass;
		}

		AndFilter and = new AndFilter(groupClass);
		for( String token : Splitter.onPattern("\\s").omitEmptyStrings().trimResults(CharMatcher.is('*')).split(query) )
		{
			token = (config.isWildcards() ? "*" : "") + token + '*';

			OrFilter or = new OrFilter();
			or.addFilter(new SingleFilter(groupNameField, token, false));
			or.addFilter(new SingleFilter(groupIdField, token, false));

			and.addFilter(or);
		}
		return and;
	}

	public static class SingleFilter extends Filter
	{
		private final String name;
		private final String value;
		private final boolean escapeWildcards;

		private int limit = MAX_LIMIT;

		public SingleFilter(String name, String value)
		{
			this(name, value, true);
		}

		public SingleFilter(String name, String value, boolean escapeWildcards)
		{
			this.name = name;
			this.value = value;
			this.escapeWildcards = escapeWildcards;
		}

		@Override
		protected void toFilter(StringBuilder sb)
		{
			if( name != null )
			{
				sb.append('(');
				sb.append(name);
				sb.append('=');
				escapeValue(sb, value);
				sb.append(')');
			}
		}

		@Override
		public int getLimit()
		{
			return limit;
		}

		public void setLimit(int limit)
		{
			this.limit = limit;
		}

		public String getName()
		{
			return name;
		}

		@Override
		public boolean isSlow()
		{
			// Always have (objectclass=something) last to speed up queries
			return OBJECTCLASS.equals(name);
		}

		private void escapeValue(StringBuilder sb, String value)
		{
			for( int i = 0; i < value.length(); i++ )
			{
				final char curChar = value.charAt(i);
				switch( curChar )
				{
					case '\\':
						sb.append("\\5c");
						break;
					case '(':
						sb.append("\\28");
						break;
					case ')':
						sb.append("\\29");
						break;
					case '\u0000':
						sb.append("\\00");
						break;
					case '*':
						sb.append(escapeWildcards ? "\\2a" : curChar);
						break;
					default:
						sb.append(curChar);
				}
			}
		}
	}

	public abstract static class MultipleFilter extends Filter
	{
		private final String operator;
		private final SortedSet<Filter> filters;

		public MultipleFilter(String operator, Filter... fs)
		{
			this.operator = operator;

			filters = new TreeSet<Filter>(new Comparator<Filter>()
			{
				@Override
				public int compare(Filter o1, Filter o2)
				{
					int compare = -1;
					if( o1.isSlow() )
					{
						compare = 1;
					}
					return compare;
				}
			});

			for( Filter f : fs )
			{
				filters.add(f);
			}
		}

		@Override
		public int getLimit()
		{
			int i = MAX_LIMIT;
			for( Filter f : filters )
			{
				i = Math.max(i, f.getLimit());
			}
			return i;
		}

		public void addFilter(Filter filter)
		{
			filters.add(filter);
		}

		public Iterator<Filter> iterate()
		{
			return filters.iterator();
		}

		@Override
		protected void toFilter(StringBuilder sb)
		{
			sb.append('(');
			sb.append(operator);
			for( Filter f : filters )
			{
				f.toFilter(sb);
			}
			sb.append(')');
		}
	}

	public static class OrFilter extends MultipleFilter
	{
		public OrFilter(Filter... fs)
		{
			super("|", fs);
		}
	}

	public static class AndFilter extends MultipleFilter
	{
		public AndFilter(Filter... fs)
		{
			super("&", fs);
		}
	}

	public abstract static class Filter
	{
		public static final int MAX_LIMIT = 0;

		protected abstract void toFilter(StringBuilder sb);

		public abstract int getLimit();

		public final String toFilter()
		{
			StringBuilder sb = new StringBuilder();
			toFilter(sb);
			return sb.toString();
		}

		@Override
		public String toString()
		{
			return toFilter();
		}

		public boolean isSlow()
		{
			return false;
		}
	}

	public Attributes getAttributes(DirContext ctx, Name name, String[] attributes)
	{
		try
		{
			return ctx.getAttributes(name, attributes);
		}
		catch( NamingException ne )
		{
			throw new RuntimeException(ne);
		}
	}

	public static Name parse(String name) throws InvalidNameException
	{
		return new LdapName(name);
	}

	public UserBean getUserBean(DirContext ctx, Name name)
	{
		return getUserBeanFromResult(getAttributes(ctx, name, userAttributes));
	}

	public GroupBean getGroupBean(DirContext ctx, Name name)
	{
		return getGroupBeanFromResult(name, getAttributes(ctx, name, groupAttributes));
	}

	private UserBean getUserBeanFromResult(Attributes result)
	{
		String id = getValue(result, idField);
		if( config.isForceLowercaseIds() )
		{
			id = id.toLowerCase();
		}

		String username = getValue(result, usernameField);
		if( !Check.isEmpty(defaultDomain) && username.endsWith(atDefaultDomain) )
		{
			username = username.substring(0, username.length() - atDefaultDomain.length());
		}
		String email = getValue(result, emailField);
		String firstname = getValue(result, firstnameField);
		String lastname = getValue(result, surnameField);
		return new DefaultUserBean(id, username, firstname, lastname, email);
	}

	public GroupBean getGroupBeanFromResult(LDAPResult result)
	{
		if( result == null )
		{
			return null;
		}
		return getGroupBeanFromResult(result.getFullName(), result.getAttributes());
	}

	public GroupBean getGroupBeanFromResult(Name name, Attributes attributes)
	{
		String id;
		if( !Check.isEmpty(groupIdField) )
		{
			id = getValue(attributes, groupIdField);
		}
		else
		{
			id = name.toString();
		}
		if( config.isForceLowercaseIds() )
		{
			id = id.toLowerCase();
		}

		String groupname = getValue(attributes, groupNameField);
		if( id.length() == 0 || groupname.length() == 0 )
		{
			return null;
		}
		return new DefaultGroupBean(id, groupname);
	}

	private String getValue(Attributes answer, String name)
	{
		Attribute attr = null;
		if( name != null )
		{
			attr = answer.get(name);
		}

		if( attr == null )
		{
			return "";
		}

		try
		{
			return (String) attr.get();
		}
		catch( NamingException ne )
		{
			return "";
		}
	}

	public GroupSearch getGroupSearch()
	{
		if( groupSearch == null )
		{
			if( Check.isEmpty(memberField) && Check.isEmpty(memberOfField) )
			{
				groupSearch = new DirectoryGroupSearch(this);
			}
			else
			{
				groupSearch = new MemberOfGroupSearch(memberField, memberOfField, memberKeyField, this);
			}
		}
		return groupSearch;
	}

	public LDAPResult getGroupResult(DirContext ctx, String groupID, String[] attrs)
	{
		if( !Check.isEmpty(groupIdField) )
		{
			SingleFilter nv1 = new SingleFilter(OBJECTCLASS, getGroupObject());
			SingleFilter nv2 = new SingleFilter(groupIdField, groupID);
			return searchFirstResultAllBases(ctx, new AndFilter(nv1, nv2), new LdapResultHitsCollector(attrs), true);
		}

		try
		{
			Name name = LDAP.parse(groupID);
			return new LDAPResult(name, getAttributes(ctx, name, attrs));
		}
		catch( InvalidNameException e )
		{
			LOGGER.debug(e, e);
			return null;
		}
	}

	public interface InContext<T>
	{
		T execute(DirContext ctx) throws NamingException;
	}

	public <T> T doInContext(DirContext ctx, InContext<T> inContext)
	{
		try
		{
			return inContext.execute(ctx);
		}
		catch( NamingException ne )
		{
			throw new RuntimeException("LDAP Error", ne);
		}
		finally
		{
			try
			{
				ctx.close();
			}
			catch( NamingException e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	public <T> T doAsAdmin(InContext<T> inContext)
	{
		DirContext ctx;
		try
		{
			ctx = bind(adminUsername, adminPassword, true, true);
		}
		catch( NamingException e )
		{
			throw new RuntimeException(e);
		}
		return doInContext(ctx, inContext);
	}

	public abstract static class HitsCollector<T>
	{
		protected DirContext ctx;
		protected LDAP ldap;

		protected List<T> results = new ArrayList<T>();

		public List<T> getResults()
		{
			return results;
		}

		protected void setup(DirContext ctx, LDAP ldap)
		{
			this.ctx = ctx;
			this.ldap = ldap;
		}

		public abstract void addResult(SearchResult sr, Name base) throws NamingException;

		public abstract String[] getReturnAttributes();
	}

	public static class FullNameHitsCollector extends HitsCollector<Name>
	{
		@Override
		public void addResult(SearchResult sr, Name base) throws NamingException
		{
			results.add(parse(sr.getNameInNamespace()));
		}

		@Override
		public String[] getReturnAttributes()
		{
			return ATTRIBUTES_NONE;
		}
	}

	/**
	 * @deprecated This is a bit unsafe since the return attributes may not
	 *             match what's required.
	 */
	@Deprecated
	public static class LdapResultHitsCollector extends HitsCollector<LDAPResult>
	{
		private final String[] returnAttributes;

		public LdapResultHitsCollector(String[] returnAttributes)
		{
			this.returnAttributes = returnAttributes;
		}

		@Override
		public void addResult(SearchResult sr, Name base) throws InvalidNameException
		{
			results.add(new LDAPResult(sr, base));
		}

		@Override
		public String[] getReturnAttributes()
		{
			return returnAttributes;
		}
	}

	public static class UserBeanHitsCollector extends HitsCollector<UserBean>
	{
		@Override
		public void addResult(SearchResult sr, Name base)
		{
			results.add(ldap.getUserBeanFromResult(sr.getAttributes()));
		}

		@Override
		public String[] getReturnAttributes()
		{
			return ldap.userAttributes;
		}
	}

	public static class GroupBeanHitsCollector extends HitsCollector<GroupBean>
	{
		@Override
		public void addResult(SearchResult sr, Name base) throws NamingException
		{
			GroupBean gb = ldap.getGroupBeanFromResult(parse(sr.getNameInNamespace()), sr.getAttributes());
			if( gb != null )
			{
				results.add(gb);
			}
		}

		@Override
		public String[] getReturnAttributes()
		{
			return ldap.groupAttributes;
		}
	}

	public static class LDAPResult
	{
		private final Name baseName;
		private final Name fullName;
		private final Attributes attributes;

		public LDAPResult(SearchResult sr, Name base) throws InvalidNameException
		{
			attributes = sr.getAttributes();

			// getNameInNamespace, otherwise you get parsing problems
			fullName = parse(sr.getNameInNamespace());

			this.baseName = base;
		}

		public LDAPResult(Name base, Attributes a)
		{
			this.baseName = base;
			this.fullName = base;
			this.attributes = a;
		}

		public Name getBaseName()
		{
			return baseName;
		}

		public Attributes getAttributes()
		{
			return attributes;
		}

		public Name getFullName()
		{
			return fullName;
		}

		@Override
		public int hashCode()
		{
			return fullName.hashCode();
		}

		@Override
		public boolean equals(Object rhs)
		{
			if( rhs != null )
			{
				if( this == rhs )
				{
					return true;
				}
				else if( rhs instanceof LDAPResult )
				{
					return fullName.equals(((LDAPResult) rhs).fullName);
				}
			}
			return false;
		}

		@Override
		public String toString()
		{
			return "(" + getFullName() + "=" + attributes + ")";
		}
	}

	protected String getGroupObject()
	{
		return groupObject;
	}

	public String getGroupIdField()
	{
		return groupIdField;
	}

	protected String getGroupNameField()
	{
		return groupNameField;
	}

	public String[] getGroupAttributes()
	{
		return groupAttributes;
	}

	public String[] getExtGroupAttributes()
	{
		return extGroupAttributes;
	}
}
