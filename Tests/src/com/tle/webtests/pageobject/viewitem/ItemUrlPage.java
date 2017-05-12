package com.tle.webtests.pageobject.viewitem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

/**
 * This may already exist in another form somewhere... couldn't find it.
 * 
 * @author Aaron
 *
 */
public class ItemUrlPage extends AbstractPage<ItemUrlPage>
{
	private final ItemId itemId;
	private final String folderContext;

	public ItemUrlPage(PageContext context, ItemId itemId)
	{
		this(context, itemId, null);
	}

	public ItemUrlPage(PageContext context, ItemId itemId, String folderContext)
	{
		super(context, By.id("listing"));
		this.itemId = itemId;
		this.folderContext = folderContext;
	}

	public ItemUrlPage viewRoot()
	{
		get("items/" + itemId + "/~");
		return new ItemUrlPage(context, itemId, null).get();
	}

	public ItemUrlPage viewFolder(String folderPath)
	{
		folderPath = encode(folderPath);
		if( folderPath.startsWith("/") )
		{
			get("items/" + itemId + folderPath + "/~");
			return new ItemUrlPage(context, itemId, folderPath.substring(1));
		}
		else
		{
			folderPath = (folderContext == null ? folderPath : folderContext + '/' + folderPath);
			get("items/" + itemId + '/' + folderPath + "/~");
			return new ItemUrlPage(context, itemId, folderPath);
		}
	}

	public WebElement getFolderLink(String folderName)
	{
		try
		{
			return getContext().getDriver().findElement(
				By.xpath("//a[@href=" + quoteXPath(encode(folderName) + "/~") + "]"));
		}
		catch( NoSuchElementException nse )
		{
			return null;
		}
	}

	public WebElement getFileLink(String fileName)
	{
		try
		{
			return getContext().getDriver().findElement(By.xpath("//a[@href=" + quoteXPath(encode(fileName)) + "]"));
		}
		catch( NoSuchElementException nse )
		{
			return null;
		}
	}

	private static final Set<String> ILLEGAL_FILENAMES = new HashSet<String>(Arrays.asList("com1", "com2", "com3",
		"com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8",
		"lpt9", "con", "nul", "prn"));

	private String encode(String filename)
	{
		// if the filename contains directory folders, we need encode each
		// seperately
		if( filename.contains("\\") || filename.contains("/") ) //$NON-NLS-1$ //$NON-NLS-2$
		{
			String[] parts = filename.split("\\\\|/"); //$NON-NLS-1$
			StringBuilder full = new StringBuilder();
			int i = 0;
			for( String part : parts )
			{
				if( i != 0 )
				{
					full.append('/');
				}
				full.append(encode(part));
				i++;
			}
			return full.toString();
		}

		StringBuilder szOut = new StringBuilder();
		if( filename.isEmpty() )
		{
			return filename;
		}
		int lastIndex = filename.length() - 1;
		char first = filename.charAt(0);
		char last = filename.charAt(lastIndex);
		boolean encodeFirst = ILLEGAL_FILENAMES.contains(filename.toLowerCase()) || first == '.';
		boolean encodeLast = last == '.' || last == ' ';
		for( int i = 0; i < filename.length(); i++ )
		{
			boolean encode = false;
			char ch = filename.charAt(i);
			if( ch < 0x20 || (encodeFirst && i == 0) || (encodeLast && i == lastIndex) )
			{
				encode = true;
			}
			else
			{
				switch( ch )
				{
					case ':':
					case '*':
					case '?':
					case '"':
					case '<':
					case '>':
					case '|':
					case '^':
					case '%':
					case '+':
						encode = true;
						break;
				}
			}
			if( encode )
			{
				szOut.append('%');
				int intval = ch;
				szOut.append(String.format("%02x", intval)); //$NON-NLS-1$
			}
			else
			{
				szOut.append(ch);
			}
		}
		String res = basicUrlEncode(szOut.toString());
		res = res.replaceAll("\\+", "%20");
		return res;
	}

	private static String basicUrlEncode(String url)
	{
		try
		{
			return URLEncoder.encode(url, "UTF-8");
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}
}
