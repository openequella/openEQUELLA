/*
 * Copyright 2019 Apereo
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

package com.tle.web.inplaceeditor;

import java.awt.Component;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyPermission;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.rococoa.ObjCClass;
import org.rococoa.ObjCObject;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSAutoreleasePool;
import org.rococoa.cocoa.foundation.NSString;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.tle.common.URLUtils;
import com.tle.common.gui.models.GenericListModel;
import com.tle.web.inplaceeditor.LinuxOpener.EnvPropertyGetter;

/**
 * The reference documents are available from the developer.apple.com site. For
 * Rococoa, java.net/projects/rococoa/ is its home, with some useful if minimal
 * chatter on java.net/projects/rococoa/lists/users/archive For Cocoa
 * Objective-C generally, www.cocoadev.com www.stackoverflow.com has some useful
 * tagged queries.
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public class MacOpener extends AbstractListDialogOpener
{
	// Just plain "Carbon" should suffice in theory, but otherwise it's probably
	// /System/Library/Frameworks/Carbon.framework/Carbon
	public static final String MAC_CARBON_PATH = "Carbon";

	public static final String ROCOCOA = "rococoa";

	public static final String LIBROCOCOA_JNILIB = System.mapLibraryName(ROCOCOA);

	public interface CCarbonWrapper extends Library
	{
		// (CCarbonWrapper) Native.loadLibrary(MAC_CARBON_PATH,
		// CCarbonWrapper.class);
		CCarbonWrapper INSTANCE = null;

		// declare function prototypes in a form sympathetic to Java & Carbon C
		// main function APIs
		int LSCopyKindStringForMIMEType(Object cfstrMIMETYPE, PointerByReference cfstrReturned);

		int LSCopyApplicationForMIMEType(Object cfstrMIMETYPE, int rolesMask, PointerByReference cfurlReturned);

		int LSGetApplicationForURL(Object cfURL, int rolesMask, Void fileRef, PointerByReference cfurlReturned);

		Pointer LSCopyApplicationURLsForURL(Object curlRef, int rolesMask);

		// utility and conversion APIs
		//
		// CFString
		Pointer CFStringCreateWithCharacters(Void alloc, char[] chars, int numChars);

		int CFStringGetLength(Object cfstr);

		char CFStringGetCharacterAtIndex(Object cfstr, int index);

		// CFURL
		Pointer CFURLGetString(Object cfURLRef);

		Pointer CFURLCreateWithString(Void alloc, Object cfstr, Void baseUrl);

		Pointer CFURLCreateWithFileSystemPath(Void alloc, Object cfstr, int pathStyle, boolean isDirectory);

		boolean CFURLGetFSRef(Object cCURLRef, PointerByReference fsRefReturned);

		int LSCopyDisplayNameForRef(Object fsRef, PointerByReference outDisplayName);

		// CFArray
		int CFArrayGetCount(Object cfArray);

		Pointer CFArrayGetValueAtIndex(Object cfArray, int cfi);

		// CFBundle
		Pointer CFBundleCreate(Void alloc, Object cfurl);

		// Tidiness
		void CFRelease(Pointer pointer);
	}

	public interface NSWorkspace extends ObjCObject
	{
		_Class CLASS = Rococoa.createClass("NSWorkspace", _Class.class);

		public interface _Class extends ObjCClass
		{
			// static method to get the workspace in
			NSWorkspace sharedWorkspace();
		}

		// The Mac OS Cocoa method we actually need.
		// NOTE!! the underscore notation is how Rococa translates the weird
		// Cocoa Objective-C method naming conventions, so where the Apple doc
		// says: (BOOL)openFile:(NSString *)fullPath withApplication:(NSString
		// *)appName we get
		boolean openFile_withApplication(NSString fullPath, NSString appName);
		// Parameter formal names are not relevant, but the colon-identifier
		// syntax in Objective-C must be observed, exchanging colon for
		// underscore. Otherwise we either end up with an overloaded function we
		// don't want, where the method signature is inadequate to bind to the
		// intended 2-parameter function, eg (BOOL)openFile:(NSString *)fullPath
		// or a function which doesn't exist and when a call is made to it,
		// results in an unintelligible error message like: (using
		// launchApplicationAtURL as an example) 'MSInvalidArgumentException',
		// reason: '-[NSWorkspace launchApplicationAtURL:] unrecognized selector
		// sent to instance 0xSOME_HEX_ADDRESS
	}

	private CCarbonWrapper ccarbon = CCarbonWrapper.INSTANCE;

	private static final Logger LOGGER = Logger.getLogger(MacOpener.class.getName());
	// Copies of Apple constants
	public static final int kLSRolesNone = 0x00000001;
	public static final int kLSRolesViewer = 0x00000002;
	public static final int kLSRolesEditor = 0x00000004;
	public static final int kLSRolesShell = 0x00000008;
	public static final int kLSRolesAll = 0xFFFFFFFF;

	/**
	 * To quote from http://lopica.sourceforge.net/faq.html
	 * ("Unofficial Java Web Start/JNLP FAQ") "Note, that if you load classes
	 * with your own ClassLoader, you should also install your own
	 * SecurityManager (or none) by calling System.setSecurityManager( null ),
	 * otherwise you might run into security access violations even if you
	 * signed all your jars. Web Start's built-in security manager only assigns
	 * all permissions to the classes loaded by its own JNLPClassLoader." A
	 * reference to JavaWebStartSecurityManager if such a thing exists ...? And
	 * furthermore ... "Q: How can I turn off the sandbox? If your app is
	 * signed, you can call System.setSecurityManager(null) to turn off the
	 * sandbox. If you get rid off the security manager, your app should speed
	 * up as it no longer goes through security layers. Don't expect miracles,
	 * though." Roger that: miracles not expected ... ?
	 */
	@Override
	public void openWith(final Component parent, final String filepath, final String mimetype)
	{
		LOGGER.info("OS is mac.  Invoking openWith ... " + filepath);
		attemptSetNullSecurityManager();

		String tmpdir = getSysPropertyValues("java.io.tmpdir", "read");

		final Permissions permissions = new Permissions();

		loadUpPermissions(permissions);

		if( tmpdir != null && tmpdir.length() > 0 )
		{
			// demands delete privilege to the tmpdir ...
			permissions.add(new FilePermission(tmpdir, "read,write,delete"));
			// and just in case we also explicitly need the subdirectory
			// permissions
			if( !tmpdir.endsWith("/") )
			{
				tmpdir += "/";
			}
			String tmpdirsub = tmpdir + '-';
			String tmpdirwild = tmpdir + '*';
			permissions.add(new FilePermission(tmpdirsub, "read,write,delete"));
			permissions.add(new FilePermission(tmpdirwild, "read,write,delete"));
		}

		final String jnaLibPath = System.getProperty("jna.library.path");
		final String javaLibPath = System.getProperty("java.library.path");

		final AccessControlContext context = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null,
			permissions)});
		AccessController.doPrivileged(new PrivilegedAction<Object>()
		{
			@Override
			public Object run()
			{
				try
				{

					LOGGER.info("Attempting Native.loadLibrary('" + MAC_CARBON_PATH
						+ ", ...') where 'jna.library.path' is " + jnaLibPath + ", and 'java.library.path' is "
						+ javaLibPath);
					ccarbon = (CCarbonWrapper) Native.loadLibrary(MAC_CARBON_PATH, CCarbonWrapper.class);
					LOGGER.info("Should have loaded " + MAC_CARBON_PATH + " statically");
					List<App> apps = getAppList(filepath, mimetype);
					JList list = new JList(new GenericListModel<App>(apps));
					list.setCellRenderer(new DefaultListCellRenderer()
					{
						private static final long serialVersionUID = -2876756312441665693L;

						@Override
						public Component getListCellRendererComponent(JList list, Object value, int index,
							boolean isSelected, boolean cellHasFocus)
						{
							super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
							setText(((App) value).getName());
							return this;
						}
					});

					int rv = JOptionPane.showOptionDialog(parent, new JScrollPane(list), "Open with...",
						JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Launch",}, null);
					if( rv != JOptionPane.OK_OPTION )
					{
						return null;
					}

					int i = list.getSelectedIndex();
					if( i < 0 )
					{
						return null;
					}

					LOGGER.info("Attempting loadLibrary(\"" + ROCOCOA + "\")");
					System.loadLibrary(ROCOCOA);
					LOGGER.info("Progressed past loadLibrary(\"" + ROCOCOA + "\"), about to call executeApp");
					executeApp(apps.get(i), filepath, mimetype);
				}
				catch( IOException ioe )
				{
					throw new RuntimeException(ioe);
				}
				return null;
			}
		}, context);
	}

	private void attemptSetNullSecurityManager()
	{
		final Permissions permissions = new Permissions();
		permissions.add(new RuntimePermission("setSecurityManager"));
		final AccessControlContext context = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null,
			permissions)});
		AccessController.doPrivileged(new PrivilegedAction<Object>()
		{
			@Override
			public Object run()
			{
				LOGGER.info("About to call System.setSecurityManager(null)");
				System.setSecurityManager(null);
				LOGGER.info("Progressed beyond call to System.setSecurityManager(null)");
				return null;
			}
		}, context);
	}

	/*
	 * We can reliably call APIs to get the SINGLE preferred app for a named
	 * file (named by a full url) or get the single preferred app for a mimetype
	 * (if a mimetype is supplied). These 2 calls may return different results,
	 * in which case the LSGetApplicationForURL is likely to be more appropriate
	 * hence should be placed at the top of the list.
	 * @see
	 * com.tle.web.inplaceeditor.AbstractListDialogOpener#getAppList(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	protected List<App> getAppList(String filepath, String mimetype) throws IOException
	{
		List<App> appList = new ArrayList<App>();

		Pointer curlref = null;

		// convert filepath arg to a CFURLRef
		Object cfstr = toCFString(filepath);
		// copy of Apple constant
		int kCFURLPOSIXPathStyle = 0;
		boolean isDirectory = false; // more than likely an Apple application is
										// truly a directory, but should it
										// matter?
		curlref = ccarbon.CFURLCreateWithFileSystemPath(null, cfstr, kCFURLPOSIXPathStyle, isDirectory);

		// Extract the string value of the cCURLRef for logging.
		String curlstr = curlref.toString();
		Object cfstrURL = ccarbon.CFURLGetString(curlref);
		if( cfstrURL != null )
		{
			curlstr = fromCFString(cfstrURL);
		}

		// Gets the preferred application for a given file, the intent being to
		// place
		// this first in any multi-element list.

		LOGGER.info("About to call LSGetApplicationForURL with url (" + curlstr + ") from filepath - " + filepath);
		PointerByReference cfurlFromFile = new PointerByReference();
		int ret = ccarbon.LSGetApplicationForURL(curlref, kLSRolesEditor, null, cfurlFromFile);
		String preferredAppFromFile = processLSResult(cfurlFromFile, ret, "LSGetApplicationForURL");
		if( preferredAppFromFile != null )
		{
			addToAppList(appList, preferredAppFromFile);
		}

		// If a mimetype is present, see if there's a different preferred app
		// for it.
		// Using an all-roles flag helps to retrieve the generic all-purpose
		// application
		// applicable to some mime types.
		if( mimetype != null && mimetype.length() > 0 )
		{
			// Gets the preferred application for a given mime type, the intent
			// being to place
			// this at or near the top of any multi-element list.
			LOGGER.info("About to call LSCopyApplicationForMIMEType with mimetype (" + mimetype + ")");
			Object cfstrMimetype = toCFString(mimetype);
			PointerByReference cfurlFromMimeType = new PointerByReference();
			int retFromMimeType = ccarbon.LSCopyApplicationForMIMEType(cfstrMimetype, kLSRolesAll, cfurlFromMimeType);
			String preferredAppNameFromMimeType = processLSResult(cfurlFromMimeType, retFromMimeType,
				"LSCopyApplicationForMIMEType");
			if( preferredAppNameFromMimeType != null )
			{
				// addToAppList will skip over duplicates
				addToAppList(appList, preferredAppNameFromMimeType);
			}
		}

		// Having established the primary Launch application for our file URL
		// and possibly a secondary, if a mimetype is provided and yields a
		// different result. We can ensure the most eligible application
		// goes to the top of the list. When getting a complete set we can't
		// assume the first in the returned set is the primary app.

		appList = getAllApplicationsForFilepath(curlref, appList, kLSRolesEditor);
		ccarbon.CFRelease(curlref);
		return appList;
	}

	@Override
	protected void executeApp(App app, String filepath, String mimetype) throws IOException
	{
		LOGGER.info("For app[" + app.getName() + ", " + app.getExec() + "]:\nattempting to execute on " + filepath
			+ "(" + mimetype + ")");

		final NSAutoreleasePool pool = NSAutoreleasePool.new_();
		try
		{
			final NSWorkspace nsWorkspace = NSWorkspace.CLASS.sharedWorkspace();
			boolean isRunning = nsWorkspace.openFile_withApplication(NSString.stringWithString(filepath),
				NSString.stringWithString(app.getExec()));
			LOGGER.info("Called " + app.getExec() + " to open " + filepath + " (app now running? " + isRunning + ')');
		}
		finally
		{
			pool.drain();
		}
	}

	/**
	 * Common result extraction and logging for LSGetApplicationForURL and
	 * LSCopyApplicationForMIMEType
	 * 
	 * @param cfURLreturn
	 * @param ret
	 * @param functionName
	 * @return
	 */
	private String processLSResult(PointerByReference cfURLreturn, int ret, String functionName)
	{
		String preferredApp = null;
		switch( ret )
		{
			case 0:
				Pointer retptr = cfURLreturn.getPointer();
				if( retptr != null )
				{
					Pointer refval = retptr.getPointer(0);
					Pointer cfstr = refval != null ? ccarbon.CFURLGetString(refval) : null;
					if( cfstr != null )
					{
						preferredApp = fromCFString(cfstr);
						LOGGER
							.info("From call to " + functionName + ", preferred app from filepath is " + preferredApp);
					}
					ccarbon.CFRelease(refval);
				}
				break;
			case -10810:
				LOGGER.info("Called " + functionName + " with result (" + ret + ") kLSUnknownErr");
				break;
			case -10811:
				LOGGER.info("Called " + functionName + " with result (" + ret + ") kLSNotAnApplicationErr");
				break;
			case -10813:
				LOGGER.info("Called " + functionName + " with result (" + ret + ") kLSDataUnavailableErr");
				break;
			case -10814:
				LOGGER.info("Called " + functionName + " with result (" + ret + ") kLSApplicationNotFoundErr");
				break;
			default:
				LOGGER.info("Called " + functionName + " with result (" + ret
					+ ") Some other error - refer to Apple's Launch Services Reference");
				break;
		}
		return preferredApp;
	}

	/**
	 * Calls LSCopyApplicationURLsForURL to get all LaunchServices registered
	 * applications which claim to be able to handle the named file.
	 * 
	 * @param filepath local url filepath
	 * @param appList may be null, if so it is initialised and populated. If not
	 *            null, it is appended to with such discovered app names not
	 *            already present.
	 * @return appList
	 */
	private List<App> getAllApplicationsForFilepath(Pointer curlref, List<App> appList, int rolesMask)
	{
		if( appList == null )
		{
			appList = new ArrayList<App>();
		}

		Pointer ptrArray = ccarbon.LSCopyApplicationURLsForURL(curlref, rolesMask);
		if( ptrArray != null )
		{
			int arrLength = ccarbon.CFArrayGetCount(ptrArray);
			LOGGER.info("found " + arrLength + " app elements");
			for( int cfi = 0; cfi < arrLength; ++cfi )
			{
				Pointer cfurl = ccarbon.CFArrayGetValueAtIndex(ptrArray, cfi);
				Pointer cfstr = cfurl != null ? ccarbon.CFURLGetString(cfurl) : null;
				if( cfstr != null )
				{
					String appName = fromCFString(cfstr);
					LOGGER.info("From LSCopyApplicationsURLsForURL [" + cfi + "] " + appName);
					addToAppList(appList, appName);
				}
			}
			ccarbon.CFRelease(ptrArray);
		}
		else
		{
			LOGGER.info("null elements for app search");
		}
		return appList;
	}

	/**
	 * This is here as a proof-of-concept pilot, but it may have a use
	 * eventually.
	 * 
	 * @param mimetype eg "text/xml
	 * @return what LaunchServices treats as the readable kind type, (eg
	 *         "Text document"), null if failure or empty string if no result.
	 */
	// private String getKindStringFromMIMEType(String mimetype)
	// {
	// String retval = null;
	// Object cfstrMimetype = toCFString(mimetype);
	// PointerByReference retPtrRef = new PointerByReference();
	// int sysret = ccarbon.LSCopyKindStringForMIMEType(cfstrMimetype,
	// retPtrRef);
	// if( sysret == 0 )
	// {
	// // called without error. We expect a pointer to a pointer
	// Pointer pointer = retPtrRef.getPointer();
	// if( pointer != null ) // sanity check
	// {
	// Pointer cfstrval = pointer.getPointer(0);
	// retval = fromCFString(cfstrval);
	// // Implied malloc hence actual free, just like real C
	// ccarbon.CFRelease(cfstrval);
	// }
	// }
	// return retval;
	// }

	/**
	 * The executeApp method takes application identifiers as strings, not
	 * URL's. Apple's inquiry returns file:// URLs, which we decode (so
	 * 'Microsoft%20Word' becomes 'Microsoft Word', we strip off the file://host
	 * preamble and strip any trailing spaces.
	 */
	private void addToAppList(List<App> appList, String originalAppName)
	{
		LOGGER.info("Pre decode appName is " + originalAppName);
		String processedAppName = URLUtils.basicUrlDecode(originalAppName);
		LOGGER.info("Post decode appName is " + processedAppName);
		processedAppName = trimProtocol(processedAppName);
		LOGGER.info("Post trimPro appName is " + processedAppName);
		boolean execIsPresent = false;

		for( int i = 0; !execIsPresent && i < appList.size(); ++i )
		{
			App anApp = appList.get(i);
			if( anApp.getExec() != null && anApp.getExec().equals(processedAppName) )
			{
				execIsPresent = true;
			}
			LOGGER.info(processedAppName + " - Comparing to present element[" + i + "]: " + anApp.getExec() + '('
				+ anApp.getName() + ") " + execIsPresent);
		}

		if( !execIsPresent )
		{
			String prettyName = getDisplayNameForApplication();
			if( prettyName == null )
			{
				prettyName = processedAppName;
				// we've already trimmed off any trailing '/'
				int lastSep = prettyName.lastIndexOf('/');
				if( lastSep >= 0 && lastSep < prettyName.length() - 1 )
				{
					prettyName = prettyName.substring(lastSep + 1);
					// We can improve the look a little by stripping off the
					// ".app" from "WhatEver.app", so long as "WhatEver" is at
					// least 2 characters
					String superfluousSuffix = ".app";
					if( prettyName.length() > (superfluousSuffix.length() + 1)
						&& prettyName.endsWith(superfluousSuffix) )
					{
						prettyName = prettyName.substring(0, prettyName.length() - superfluousSuffix.length());
					}
				}
				// Would spaces help to proportion the dialogue box?
				// prettyName = "  " + prettyName + "  ";
			}
			appList.add(new App(prettyName, processedAppName));
		}
	}

	/**
	 * utility string manip function Apple Applications are usually executable
	 * directories (eg /Applications/TextEdit.app/ ) and thus are quite likely
	 * to be returned as having a trailing separator. We remove it both for
	 * neatness sake and to facilitate extracting the app name from the file
	 * path, in that the last occurrence of '/' will precede the app name.
	 */
	private static String trimProtocol(String orig)
	{
		while( orig.endsWith("/") )
		{
			orig = orig.substring(0, orig.length() - 1);
		}
		// dispense with protocol://machine so
		// "file://hostname/Applications/execfile.app/
		// becomes simply /Applications/execfile.app with removed trailing '/'
		int firstSlashIndex = orig.indexOf('/');
		if( firstSlashIndex >= 1 && firstSlashIndex < orig.length() )
		{
			int secondSlashIndex = orig.substring(firstSlashIndex + 1).indexOf('/');
			if( secondSlashIndex >= 0 && secondSlashIndex < orig.length() )
			{
				secondSlashIndex += (firstSlashIndex + 1);
				int thirdSlashIndex = orig.substring(secondSlashIndex + 1).indexOf('/');
				if( thirdSlashIndex >= 0 && thirdSlashIndex < orig.length() )
				{
					thirdSlashIndex += (secondSlashIndex + 1);
					orig = orig.substring(thirdSlashIndex);
				}
			}
		}
		return orig;
	}

	/**
	 * Utility method to convert java String to Carbon CFString
	 * 
	 * @param string
	 * @return Carbon CFString as Object
	 */
	private Object toCFString(String string)
	{
		final char[] chars = string.toCharArray();
		int length = chars.length;
		return ccarbon.CFStringCreateWithCharacters(null, chars, length);
	}

	/**
	 * Utility method to convert returned Carbon CFString to java String
	 * 
	 * @param cfstr Carbon CFString as Object
	 * @return cfstr contents converted to java String. Never null, but may be
	 *         zero length
	 */
	private String fromCFString(Object cfstr)
	{
		StringBuilder sb = new StringBuilder();
		int length = ccarbon.CFStringGetLength(cfstr);
		for( int index = 0; index < length; ++index )
		{
			sb.append(ccarbon.CFStringGetCharacterAtIndex(cfstr, index));
		}
		return sb.toString();
	}

	private static String getSysPropertyValues(String propertyName, String permissionsToGet)
	{
		final Permissions permissions = new Permissions();
		permissions.add(new PropertyPermission(propertyName, permissionsToGet));
		LOGGER.info("Added " + permissionsToGet + " property permission for " + propertyName);

		final AccessControlContext context = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null,
			permissions)});

		LinuxOpener.EnvPropertyGetter proppy = new EnvPropertyGetter(propertyName, false); // false
																							// =
																							// don't
																							// call
																							// getenv
																							// (rather
																							// getProperty)

		AccessController.doPrivileged(proppy, context);

		return proppy.getPropertyValue();
	}

	/**
	 * Some of these permissions are probably superfluous, but most, including
	 * nonsensical ones, are required by jni at some point.
	 * 
	 * @param permissions
	 */
	private static void loadUpPermissions(Permissions permissions)
	{
		permissions.add(new PropertyPermission("os.arch", "read"));
		LOGGER.info("Added read PropertyPermission for 'os.arch'");

		permissions.add(new PropertyPermission("os.name", "read"));
		LOGGER.info("Added read PropertyPermission for 'os.name'");

		permissions.add(new PropertyPermission("java.vm.version", "read"));
		LOGGER.info("Added read PropertyPermission for 'java.vm.version'");

		permissions.add(new PropertyPermission("java.vm.vendor", "read"));
		LOGGER.info("Added read PropertyPermission for 'java.vm.vendor'");

		permissions.add(new PropertyPermission("java.vm.name", "read"));
		LOGGER.info("Added read PropertyPermission for 'java.vm.name'");

		permissions.add(new PropertyPermission("jna.boot.library.path", "read"));
		LOGGER.info("Added read PropertyPermission for 'jna.boot.library.path'");

		// write permission not demanded, but we need it?
		permissions.add(new PropertyPermission("jna.library.path", "read,write"));
		LOGGER.info("Added read,write PropertyPermission for 'jna.library.path'");

		// jna demands permission to write to this property.
		permissions.add(new PropertyPermission("jna.platform.library.path", "read,write"));
		LOGGER.info("Added read,write PropertyPermission for 'jna.platform.library.path'");

		// jna demands permission to write to this property.
		permissions.add(new PropertyPermission("jna.encoding", "read,write"));
		LOGGER.info("Added read,write PropertyPermission for 'jna.encoding'");

		permissions.add(new PropertyPermission("jna.protected", "read"));
		LOGGER.info("Added read PropertyPermission for 'jna.protected'");

		permissions.add(new PropertyPermission("javawebstart.version", "read"));
		LOGGER.info("Added read PropertyPermission for 'javawebstart.version'");

		// A wildcard (loadLibrary.*) syntax is available, but does it improve
		// the situation ... ?
		permissions.add(new RuntimePermission("loadLibrary.*"));
		LOGGER.info("Added read RuntimePermission for 'loadLibrary.*'");

		permissions.add(new RuntimePermission("loadLibrary.jnidispatch"));
		LOGGER.finest("Added read RuntimePermission for 'loadLibrary.jnidispatch'");

		permissions.add(new RuntimePermission("loadLibrary.Carbon"));
		LOGGER.info("Added RuntimePermission for 'loadLibrary.Carbon'");

		permissions.add(new RuntimePermission("loadLibrary." + ROCOCOA));
		LOGGER.info("Added RuntimePermission for 'loadLibrary.rococoa'");

		permissions.add(new FilePermission("/lib", "read")); // "/lib/-"
																// 'recursively'
																// seemed
																// inadequate in
																// absence of
																// /lib
		LOGGER.info("Added read FilePermission for '/lib'");

		permissions.add(new FilePermission("/usr/lib", "read")); // likewise
		LOGGER.info("Added read FilePermission for '/usr/lib'");

		permissions.add(new FilePermission("/lib/-", "read")); // ".../-"
																// recursively
		LOGGER.info("Added read FilePermission for '/lib/-'");

		permissions.add(new FilePermission("/usr/lib/-", "read")); // likewise
		LOGGER.info("Added read FilePermission for '/usr/lib/-'");

		permissions.add(new FilePermission("/System/Library/Frameworks", "read"));
		LOGGER.info("Added read FilePermission for '/System/Library/Frameworks'");

		permissions.add(new FilePermission("/System/Library/Frameworks/-", "read"));
		LOGGER.info("Added read FilePermission for '/System/Library/Frameworks/-'");

		// jna demands permission to read
		// /Library/Java/Extensions/i386/libjnidispatch.jnilib
		// untroubled by the fact that the path doesn't exist, and has no reason
		// to exist.
		permissions.add(new FilePermission("/Library/Java/Extensions", "read"));
		LOGGER.info("Added read FilePermission for '/Library/Java/Extensions'");

		// rococoa requires these permissions expressed as "./libFoundation..."
		// on the client, although it would seem the paths are nonsensical.
		permissions.add(new FilePermission("libFoundation.dylib", "read"));
		LOGGER.info("Added read file permission for libFoundation.dylib");

		permissions.add(new FilePermission("./libFoundation.dylib", "read"));
		LOGGER.info("Added read file permission for ./libFoundation.dylib");

		permissions.add(new FilePermission("/System/Library/Java/Extensions/libFoundation.dylib", "read"));
		LOGGER.info("Added read file permission for /System/Library/Java/Extensions/libFoundation.dylib");

		permissions.add(new FilePermission("libFoundation.jnilib", "read"));
		LOGGER.info("Added read file permission for libFoundation.jnilib");

		permissions.add(new FilePermission("./libFoundation.jnilib", "read"));
		LOGGER.info("Added read file permission for ./libFoundation.jnilib");

		permissions.add(new FilePermission("/System/Library/Java/Extensions/libFoundation.jnilib", "read"));
		LOGGER.info("Added read file permission for /System/Library/Java/Extensions/libFoundation.jnilib");

		permissions.add(new FilePermission("libCarbon.dylib", "read"));
		LOGGER.info("Added read file permission for libCarbon.dylib");

		permissions.add(new FilePermission("./libCarbon.dylib", "read"));
		LOGGER.info("Added read file permission for ./libCarbon.dylib");

		permissions.add(new FilePermission("/System/Library/Java/Extensions/libCarbon.dylib", "read"));
		LOGGER.info("Added read file permission for /System/Library/Java/Extensions/libCarbon.dylib");

		permissions.add(new FilePermission("libCarbon.jnilib", "read"));
		LOGGER.info("Added read file permission for libCarbon.jnilib");

		permissions.add(new FilePermission("./libCarbon.jnilib", "read"));
		LOGGER.info("Added read file permission for ./libCarbon.jnilib");

		permissions.add(new FilePermission("/System/Library/Java/Extensions/libCarbon.jnilib", "read"));
		LOGGER.info("Added read file permission for /System/Library/Java/Extensions/libCarbon.jnilib");

		permissions.add(new FilePermission("/Library/Java/Extensions/-", "read"));
		LOGGER.info("Added read FilePermission for '/Library/Java/Extensions/-'");

		// librococoa.dylib as such doesn't exist, but do we still need these
		// permissions...?
		permissions.add(new FilePermission("librococoa.dylib", "read"));
		LOGGER.info("Added read file permission for librococoa.dylib");

		permissions.add(new FilePermission("/com/tle/web/inplaceeditor/librococoa.dylib", "read"));
		LOGGER.info("Added read file permission for /com/tle/web/inplaceeditor/librococoa.dylib");

		permissions.add(new FilePermission("/System/Library/Java/Extensions/librococoa.dylib", "read"));
		LOGGER.info("Added read file permission for /System/Library/Java/Extensions/librococoa.dylib");

		// librococoa.jnilib - renamed from the original librococoa.dylib
		permissions.add(new FilePermission(LIBROCOCOA_JNILIB, "read"));
		LOGGER.info("Added read file permission for " + LIBROCOCOA_JNILIB);

		permissions.add(new FilePermission("/com/tle/web/inplaceeditor/" + LIBROCOCOA_JNILIB, "read"));
		LOGGER.info("Added read file permission for /com/tle/web/inplaceeditor/" + LIBROCOCOA_JNILIB);

		permissions.add(new FilePermission("/System/Library/Java/Extensions/" + LIBROCOCOA_JNILIB, "read"));
		LOGGER.info("Added read file permission for /System/Library/Java/Extensions/" + LIBROCOCOA_JNILIB);

		permissions.add(new PropertyPermission("java.io.tmpdir", "read"));
		LOGGER.info("Added read property permission for java.io.tmpdir");

		// Required by ExecUtils once we finally select and attempt to launch
		permissions.add(new FilePermission("<<ALL FILES>>", "execute"));
		LOGGER.info("Added execute permission for <<ALL FILES>>");

		permissions.add(new PropertyPermission("file.encoding", "read"));
		LOGGER.info("Added read PropertyPermission for 'file.encoding'");

		permissions.add(new PropertyPermission("java.library.path", "read,write"));
		LOGGER.info("Added read,write PropertyPermission for 'java.library.path'");

		// Creeping further into permissions
		permissions.add(new PropertyPermission("cglib.debugLocation", "read"));
		LOGGER.info("Added read PropertyPermission for 'cglib.debugLocation'");

		permissions.add(new ReflectPermission("suppressAccessChecks"));
		LOGGER.info("Added 'suppressAccessChecks' ReflectPermission");

		permissions.add(new RuntimePermission("accessDeclaredMembers"));
		LOGGER.info("Added 'accessDeclaredMembers' RuntimePermission");

		permissions.add(new PropertyPermission("user.name", "read"));
		LOGGER.info("Added read PropertyPermission for 'user.name'");
	}

	/**
	 * All my attempts to make use of CFURLGetFSRef result in crashing the
	 * browser, even after the function appears to have been called
	 * successfully, and has logged the fact.
	 * 
	 * @param originalAppName TODO - Get the locale display name from the
	 *            Application as Bundle
	 * @return
	 */
	private String getDisplayNameForApplication()
	{
		String prettyName = null;
		//@formatter:off
		// Use the Carbon methods to get a nice name from the file URL
		// Create a CURLREf from the plain "file://...." String
		// Pointer curlref = ccarbon.CFURLCreateWithString(null, toCFString(originalAppName), null);
		// String curlstr = curlref.toString();
		// Object cfstrURL = ccarbon.CFURLGetString(curlref);
		// if (cfstrURL != null)
		// {
		// 		curlstr = fromCFString(cfstrURL);
		// }
		// LOGGER.info("Created CFURLRef (" + curlstr + ") from originalAppName " + originalAppName);
		// PointerByReference fsRefReturned = new PointerByReference();
		// boolean opSuccess = ccarbon.CFURLGetFSRef(curlref, fsRefReturned);
		// // this causes a crash ...!? ?
		// ccarbon.CFRelease(curlref);
		// LOGGER.info("Successful call on CFRelease(curlref)");
		//
		// if (opSuccess)
		// {
		// 		LOGGER.info("Successful call on CFURLGetFSRef");
		// 		PointerByReference outDisplayName = new PointerByReference();
		// 		int osstatus =
		// 			ccarbon.LSCopyDisplayNameForRef(fsRefReturned.getPointer().getPointer(0),
		// 				outDisplayName);
		// 		if (osstatus == 0)
		// 		{
		// 			LOGGER.info("Successful call on LSCopyDisplayNameForRef");
		// 			prettyName = fromCFString(outDisplayName.getPointer().getPointer(0));
		// 			LOGGER.info("Pretty name is " + prettyName);
		// 		}
		// 		else
		// 			LOGGER.info("FAILED call on LSCopyDisplayNameForRef");
		// 		ccarbon.CFRelease(fsRefReturned.getPointer().getPointer(0));
		// 		LOGGER.info("Successful call on CFRelease(fsRefReturned.getPointer().getPointer(0))");
		// }
		// else
		// 		LOGGER.info("FAILED call on CFURLGetFSRef");
		//@formatter:on

		return prettyName;
	}

}
