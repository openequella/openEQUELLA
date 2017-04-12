/*
 * Created on Oct 26, 2004 For "The Learning Edge"
 */
package com.dytech.edge.common.valuebean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.dytech.devlib.Base64;
import com.dytech.devlib.Md5;
import com.dytech.edge.exceptions.LicenseException;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class License implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final byte[] SHAREPASS = new byte[]{45, 123, -112, 2, 89, 124, 19, 74, 0, 24, -118, 98, 5, 100, 92,
			7,};
	private static final IvParameterSpec INITVEC = new IvParameterSpec("thisis16byteslog".getBytes());
	private static final int PROPERTY_BASE = 16;
	private static final int TRIAL_LICENSE_INSTITUTIONS = 3;

	private static final String SUPPORT_KEY_KEY = "supportkey";
	private static final String FEATURES_KEY = "features";
	public static final String DEVELOPMENT_BUILD = "dev";

	private boolean freeHostname = false;
	private Set<String> hostnames = Collections.emptySet();
	private Set<String> features = Collections.emptySet();
	private int flags;
	private Date expiry = new Date();
	private Date warning = new Date();
	private int users;
	private int institutions;
	private String supportKey = "";
	private String version = "";

	public License()
	{
		super();
	}

	public License(String base64)
	{
		this();
		decrypt(base64);
	}

	public void load(Properties props)
	{
		hostnames = Sets.newHashSet(Splitter.on(',').split(props.getProperty("hostname")));
		freeHostname = Boolean.parseBoolean(props.getProperty("freeHostname"));
		flags = Integer.parseInt(props.getProperty("flags"), PROPERTY_BASE);
		expiry = new Date(Long.parseLong(props.getProperty("expiry"), PROPERTY_BASE));
		warning = new Date(Long.parseLong(props.getProperty("warning"), PROPERTY_BASE));
		users = Integer.parseInt(props.getProperty("users"));
		institutions = parse(props.getProperty("institutions"), TRIAL_LICENSE_INSTITUTIONS);
		supportKey = props.getProperty(SUPPORT_KEY_KEY);
		version = props.getProperty("version");
		String featuresProp = props.getProperty(FEATURES_KEY);
		if( featuresProp != null )
		{
			features = Sets.newHashSet(Splitter.on(',').split(featuresProp));
		}
	}

	private int parse(String string, int def)
	{
		try
		{
			return Integer.parseInt(string);
		}
		catch( NumberFormatException e )
		{
			return def;
		}
	}

	public void save(Properties props)
	{
		props.setProperty("hostname", getJoinedHostnames());
		props.setProperty("freeHostname", Boolean.toString(freeHostname));
		props.setProperty("flags", Integer.toHexString(flags));
		props.setProperty("expiry", Long.toHexString(expiry.getTime()));
		props.setProperty("warning", Long.toHexString(warning.getTime()));
		props.setProperty("users", Integer.toString(users));
		props.setProperty("institutions", Integer.toString(institutions));
		props.setProperty("version", version);
		props.setProperty(SUPPORT_KEY_KEY, supportKey);
		props.setProperty(FEATURES_KEY, getJoinedFeatures());
	}

	public void decrypt(String base64)
	{
		try
		{
			byte[] bytes = new Base64().decode(base64);
			SecretKey key = new SecretKeySpec(SHAREPASS, "AES"); //$NON-NLS-1$
			Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //$NON-NLS-1$
			ecipher.init(Cipher.DECRYPT_MODE, key, INITVEC);
			byte[] enc = ecipher.doFinal(bytes);
			ByteArrayInputStream bais = new ByteArrayInputStream(enc);
			Properties props = new Properties();
			props.load(bais);
			load(props);
		}
		catch( Exception e )
		{
			throw new LicenseException("Error decrypting", e);
		}
	}

	public String encrypt()
	{
		try
		{
			Properties props = new Properties();
			save(props);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			props.store(baos, new Md5().toString());
			SecretKey key = new SecretKeySpec(SHAREPASS, "AES"); //$NON-NLS-1$
			Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //$NON-NLS-1$

			ecipher.init(Cipher.ENCRYPT_MODE, key, INITVEC);
			byte[] bytes = baos.toByteArray();

			// Encrypt
			byte[] enc = ecipher.doFinal(bytes);
			return new Base64().encode(enc);

		}
		catch( Exception e )
		{
			throw new LicenseException("Error encrypting", e);
		}
	}

	public boolean isExpired()
	{
		return expiry.getTime() <= System.currentTimeMillis();
	}

	public boolean isWarned()
	{
		return warning.getTime() <= System.currentTimeMillis();
	}

	public Date getExpiry()
	{
		return expiry;
	}

	public void setExpiry(Date expiry)
	{
		this.expiry = expiry;
	}

	public int getFlags()
	{
		return flags;
	}

	public void setFlags(int flags)
	{
		this.flags = flags;
	}

	public Set<String> getHostnames()
	{
		return hostnames;
	}

	public void setHostnames(Set<String> hostnames)
	{
		this.hostnames = hostnames;
	}

	public String getJoinedHostnames()
	{
		return Joiner.on(',').join(hostnames);
	}

	public Set<String> getFeatures()
	{
		return features;
	}

	public void setFeatures(Set<String> features)
	{
		this.features = features;
	}

	public String getJoinedFeatures()
	{
		return Joiner.on(',').join(features);
	}

	public int getUsers()
	{
		return users;
	}

	public void setUsers(int users)
	{
		this.users = users;
	}

	public void setFreeHostname(boolean freeHostname)
	{
		this.freeHostname = freeHostname;
	}

	public boolean isFreeHostname()
	{
		return freeHostname;
	}

	public String getSupportKey()
	{
		return supportKey;
	}

	public void setSupportKey(String supportKey)
	{
		this.supportKey = supportKey;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public Date getWarning()
	{
		return warning;
	}

	public void setWarning(Date warning)
	{
		this.warning = warning;
	}

	public int getInstitutions()
	{
		return institutions;
	}

	public void setInstitutions(int institutions)
	{
		this.institutions = institutions;
	}
}
