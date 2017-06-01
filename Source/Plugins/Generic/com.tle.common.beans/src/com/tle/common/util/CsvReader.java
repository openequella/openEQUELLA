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

package com.tle.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * A stream based parser for parsing delimited text data from a file or a
 * stream. http://sourceforge.net/projects/javacsv/
 */
@SuppressWarnings("nls")
public class CsvReader
{
	/**
	 * Double up the text qualifier to represent an occurrence of the text
	 * qualifier.
	 */
	public static final int ESCAPE_MODE_DOUBLED = 1;

	/**
	 * Use a backslash character before the text qualifier to represent an
	 * occurrence of the text qualifier.
	 */
	public static final int ESCAPE_MODE_BACKSLASH = 2;

	private static final char LF = '\n';
	private static final char CR = '\r';
	private static final char QUOTE = '"';
	private static final char COMMA = ',';
	private static final char SPACE = ' ';
	private static final char TAB = '\t';
	private static final char POUND = '#';
	private static final char BACKSLASH = '\\';
	private static final char NULL = '\0';
	private static final char BACKSPACE = '\b';
	private static final char FORM_FEED = '\f';
	private static final char ESCAPE = '\u001B'; // ASCII/ANSI escape
	private static final char VERTICAL_TAB = '\u000B';
	private static final char ALERT = '\u0007';

	private static final int MAX_BUFFER_SIZE = 1024;
	private static final int MAX_FILE_BUFFER_SIZE = 4 * 1024;
	private static final int INITIAL_COLUMN_COUNT = 10;
	private static final int INITIAL_COLUMN_BUFFER_SIZE = 50;

	private Reader inputStream = null;
	private String fileName = null;

	// this holds all the values for switches that the user is allowed to set
	private UserSettings userSettings = new UserSettings();
	private Charset charset = null;
	private boolean useCustomRecordDelimiter = false;

	// this will be our working buffer to hold data chunks
	// read in from the data file
	private DataBuffer dataBuffer = new DataBuffer();
	private ColumnBuffer columnBuffer = new ColumnBuffer();
	private RawRecordBuffer rawBuffer = new RawRecordBuffer();
	private boolean[] isQualified = null;
	private String rawRecord = "";
	private HeadersHolder headersHolder = new HeadersHolder();

	// these are all more or less global loop variables
	// to keep from needing to pass them all into various
	// methods during parsing
	private boolean startedColumn = false;
	private boolean startedWithQualifier = false;
	private boolean hasMoreData = true;
	private char lastLetter = '\0';
	private boolean hasReadNextLine = false;
	private int columnsCount = 0;
	private long currentRecord = 0;
	private String[] values = new String[INITIAL_COLUMN_COUNT];
	private boolean initialized = false;
	private boolean closed = false;

	/**
	 * Creates a {@link com.csvreader.CsvReader CsvReader} object using a file
	 * as the data source.
	 * 
	 * @param fileName The path to the file to use as the data source.
	 * @param delimiter The character to use as the column delimiter.
	 * @param charset The {@link java.nio.charset.Charset Charset} to use while
	 *            parsing the data.
	 */
	public CsvReader(String fileName, char delimiter, Charset charset) throws FileNotFoundException
	{
		if( fileName == null )
		{
			throw new IllegalArgumentException("Parameter fileName can not be null.");
		}

		if( charset == null )
		{
			throw new IllegalArgumentException("Parameter charset can not be null.");
		}

		if( !new File(fileName).exists() )
		{
			throw new FileNotFoundException("File " + fileName + " does not exist.");
		}

		this.fileName = fileName;
		this.userSettings.delimiter = delimiter;
		this.charset = charset;

		isQualified = new boolean[values.length];
	}

	/**
	 * Creates a {@link com.csvreader.CsvReader CsvReader} object using a file
	 * as the data source.&nbsp;Uses ISO-8859-1 as the
	 * {@link java.nio.charset.Charset Charset}.
	 * 
	 * @param fileName The path to the file to use as the data source.
	 * @param delimiter The character to use as the column delimiter.
	 */
	public CsvReader(String fileName, char delimiter) throws FileNotFoundException
	{
		this(fileName, delimiter, Charset.forName("ISO-8859-1"));
	}

	/**
	 * Creates a {@link com.csvreader.CsvReader CsvReader} object using a file
	 * as the data source.&nbsp;Uses a comma as the column delimiter and
	 * ISO-8859-1 as the {@link java.nio.charset.Charset Charset}.
	 * 
	 * @param fileName The path to the file to use as the data source.
	 */
	public CsvReader(String fileName) throws FileNotFoundException
	{
		this(fileName, COMMA);
	}

	/**
	 * Constructs a {@link com.csvreader.CsvReader CsvReader} object using a
	 * {@link java.io.Reader Reader} object as the data source.
	 * 
	 * @param inputStream The stream to use as the data source.
	 * @param delimiter The character to use as the column delimiter.
	 */
	public CsvReader(Reader inputStream, char delimiter)
	{
		if( inputStream == null )
		{
			throw new IllegalArgumentException("Parameter inputStream can not be null.");
		}

		this.inputStream = inputStream;
		this.userSettings.delimiter = delimiter;
		initialized = true;

		isQualified = new boolean[values.length];
	}

	/**
	 * Constructs a {@link com.csvreader.CsvReader CsvReader} object using a
	 * {@link java.io.Reader Reader} object as the data source.&nbsp;Uses a
	 * comma as the column delimiter.
	 * 
	 * @param inputStream The stream to use as the data source.
	 */
	public CsvReader(Reader inputStream)
	{
		this(inputStream, COMMA);
	}

	/**
	 * Constructs a {@link com.csvreader.CsvReader CsvReader} object using an
	 * {@link java.io.InputStream InputStream} object as the data source.
	 * 
	 * @param inputStream The stream to use as the data source.
	 * @param delimiter The character to use as the column delimiter.
	 * @param charset The {@link java.nio.charset.Charset Charset} to use while
	 *            parsing the data.
	 */
	public CsvReader(InputStream inputStream, char delimiter, Charset charset)
	{
		this(new InputStreamReader(inputStream, charset), delimiter);
	}

	/**
	 * Constructs a {@link com.csvreader.CsvReader CsvReader} object using an
	 * {@link java.io.InputStream InputStream} object as the data
	 * source.&nbsp;Uses a comma as the column delimiter.
	 * 
	 * @param inputStream The stream to use as the data source.
	 * @param charset The {@link java.nio.charset.Charset Charset} to use while
	 *            parsing the data.
	 */
	public CsvReader(InputStream inputStream, Charset charset)
	{
		this(new InputStreamReader(inputStream, charset));
	}

	public boolean getCaptureRawRecord()
	{
		return userSettings.captureRawRecord;
	}

	public void setCaptureRawRecord(boolean captureRawRecord)
	{
		userSettings.captureRawRecord = captureRawRecord;
	}

	public String getRawRecord()
	{
		return rawRecord;
	}

	/**
	 * Gets whether leading and trailing whitespace characters are being trimmed
	 * from non-textqualified column data. Default is true.
	 * 
	 * @return Whether leading and trailing whitespace characters are being
	 *         trimmed from non-textqualified column data.
	 */
	public boolean getTrimWhitespace()
	{
		return userSettings.trimWhitespace;
	}

	/**
	 * Sets whether leading and trailing whitespace characters should be trimmed
	 * from non-textqualified column data or not. Default is true.
	 * 
	 * @param trimWhitespace Whether leading and trailing whitespace characters
	 *            should be trimmed from non-textqualified column data or not.
	 */
	public void setTrimWhitespace(boolean trimWhitespace)
	{
		userSettings.trimWhitespace = trimWhitespace;
	}

	/**
	 * Gets the character being used as the column delimiter. Default is comma,
	 * ','.
	 * 
	 * @return The character being used as the column delimiter.
	 */
	public char getDelimiter()
	{
		return userSettings.delimiter;
	}

	/**
	 * Sets the character to use as the column delimiter. Default is comma, ','.
	 * 
	 * @param delimiter The character to use as the column delimiter.
	 */
	public void setDelimiter(char delimiter)
	{
		userSettings.delimiter = delimiter;
	}

	public char getRecordDelimiter()
	{
		return userSettings.recordDelimiter;
	}

	/**
	 * Sets the character to use as the record delimiter.
	 * 
	 * @param recordDelimiter The character to use as the record delimiter.
	 *            Default is combination of standard end of line characters for
	 *            Windows, Unix, or Mac.
	 */
	public void setRecordDelimiter(char recordDelimiter)
	{
		useCustomRecordDelimiter = true;
		userSettings.recordDelimiter = recordDelimiter;
	}

	/**
	 * Gets the character to use as a text qualifier in the data.
	 * 
	 * @return The character to use as a text qualifier in the data.
	 */
	public char getTextQualifier()
	{
		return userSettings.textQualifier;
	}

	/**
	 * Sets the character to use as a text qualifier in the data.
	 * 
	 * @param textQualifier The character to use as a text qualifier in the
	 *            data.
	 */
	public void setTextQualifier(char textQualifier)
	{
		userSettings.textQualifier = textQualifier;
	}

	/**
	 * Whether text qualifiers will be used while parsing or not.
	 * 
	 * @return Whether text qualifiers will be used while parsing or not.
	 */
	public boolean getUseTextQualifier()
	{
		return userSettings.useTextQualifier;
	}

	/**
	 * Sets whether text qualifiers will be used while parsing or not.
	 * 
	 * @param useTextQualifier Whether to use a text qualifier while parsing or
	 *            not.
	 */
	public void setUseTextQualifier(boolean useTextQualifier)
	{
		userSettings.useTextQualifier = useTextQualifier;
	}

	/**
	 * Gets the character being used as a comment signal.
	 * 
	 * @return The character being used as a comment signal.
	 */
	public char getComment()
	{
		return userSettings.comment;
	}

	/**
	 * Sets the character to use as a comment signal.
	 * 
	 * @param comment The character to use as a comment signal.
	 */
	public void setComment(char comment)
	{
		userSettings.comment = comment;
	}

	/**
	 * Gets whether comments are being looked for while parsing or not.
	 * 
	 * @return Whether comments are being looked for while parsing or not.
	 */
	public boolean getUseComments()
	{
		return userSettings.useComments;
	}

	/**
	 * Sets whether comments are being looked for while parsing or not.
	 * 
	 * @param useComments Whether comments are being looked for while parsing or
	 *            not.
	 */
	public void setUseComments(boolean useComments)
	{
		userSettings.useComments = useComments;
	}

	/**
	 * Gets the current way to escape an occurrence of the text qualifier inside
	 * qualified data.
	 * 
	 * @return The current way to escape an occurrence of the text qualifier
	 *         inside qualified data.
	 */
	public int getEscapeMode()
	{
		return userSettings.escapeMode;
	}

	/**
	 * Sets the current way to escape an occurrence of the text qualifier inside
	 * qualified data.
	 * 
	 * @param escapeMode The way to escape an occurrence of the text qualifier
	 *            inside qualified data.
	 * @exception IllegalArgumentException When an illegal value is specified
	 *                for escapeMode.
	 */
	public void setEscapeMode(int escapeMode)
	{
		if( escapeMode != ESCAPE_MODE_DOUBLED && escapeMode != ESCAPE_MODE_BACKSLASH )
		{
			throw new IllegalArgumentException("Parameter escapeMode must be a valid value.");
		}

		userSettings.escapeMode = escapeMode;
	}

	public boolean getSkipEmptyRecords()
	{
		return userSettings.skipEmptyRecords;
	}

	public void setSkipEmptyRecords(boolean skipEmptyRecords)
	{
		userSettings.skipEmptyRecords = skipEmptyRecords;
	}

	/**
	 * Safety caution to prevent the parser from using large amounts of memory
	 * in the case where parsing settings like file encodings don't end up
	 * matching the actual format of a file. This switch can be turned off if
	 * the file format is known and tested. With the switch off, the max column
	 * lengths and max column count per record supported by the parser will
	 * greatly increase. Default is true.
	 * 
	 * @return
	 */
	public boolean getSafetySwitch()
	{
		return userSettings.safetySwitch;
	}

	/**
	 * Safety caution to prevent the parser from using large amounts of memory
	 * in the case where parsing settings like file encodings don't end up
	 * matching the actual format of a file. This switch can be turned off if
	 * the file format is known and tested. With the switch off, the max column
	 * lengths and max column count per record supported by the parser will
	 * greatly increase. Default is true.
	 * 
	 * @param safetySwitch
	 */
	public void setSafetySwitch(boolean safetySwitch)
	{
		userSettings.safetySwitch = safetySwitch;
	}

	/**
	 * Gets the count of columns found in this record.
	 * 
	 * @return The count of columns found in this record.
	 */
	public int getColumnCount()
	{
		return columnsCount;
	}

	/**
	 * Gets the index of the current record.
	 * 
	 * @return The index of the current record.
	 */
	public long getCurrentRecord()
	{
		return currentRecord - 1;
	}

	/**
	 * Gets the count of headers read in by a previous call to
	 * {@link com.csvreader.CsvReader#readHeaders readHeaders()}.
	 * 
	 * @return The count of headers read in by a previous call to
	 *         {@link com.csvreader.CsvReader#readHeaders readHeaders()}.
	 */
	public int getHeaderCount()
	{
		return headersHolder.length;
	}

	/**
	 * Returns the header values as a string array.
	 * 
	 * @return The header values as a String array.
	 * @exception IOException Thrown if this object has already been closed.
	 */
	public String[] getHeaders() throws IOException
	{
		checkClosed();

		if( headersHolder.headers == null )
		{
			return null;
		}

		// use clone here to prevent the outside code from
		// setting values on the array directly, which would
		// throw off the index lookup based on header name
		String[] clone = new String[headersHolder.length];
		System.arraycopy(headersHolder.headers, 0, clone, 0, headersHolder.length);
		return clone;

	}

	public void setHeaders(String[] headers)
	{
		headersHolder.headers = headers;

		headersHolder.indexByName.clear();

		if( headers != null )
		{
			headersHolder.length = headers.length;
			// use headersHolder.Length here in case headers is null
			for( int i = 0; i < headersHolder.length; i++ )
			{
				headersHolder.indexByName.put(headers[i].toLowerCase(), i);
			}
		}
		else
		{
			headersHolder.length = 0;
		}

	}

	public String[] getValues() throws IOException
	{
		checkClosed();

		// need to return a clone, and can't use clone because values.Length
		// might be greater than columnsCount
		String[] clone = new String[columnsCount];
		System.arraycopy(values, 0, clone, 0, columnsCount);
		return clone;
	}

	/**
	 * Returns the current column value for a given column index.
	 * 
	 * @param columnIndex The index of the column.
	 * @return The current column value.
	 * @exception IOException Thrown if this object has already been closed.
	 */
	public String get(int columnIndex) throws IOException
	{
		checkClosed();

		if( columnIndex > -1 && columnIndex < columnsCount )
		{
			return values[columnIndex];
		}
		return "";
	}

	/**
	 * Returns the current column value for a given column header name.
	 * 
	 * @param headerName The header name of the column.
	 * @return The current column value.
	 * @exception IOException Thrown if this object has already been closed.
	 */
	public String get(String headerName) throws IOException
	{
		checkClosed();

		return get(getIndex(headerName));
	}

	/**
	 * Creates a {@link com.csvreader.CsvReader CsvReader} object using a string
	 * of data as the source.&nbsp;Uses ISO-8859-1 as the
	 * {@link java.nio.charset.Charset Charset}.
	 * 
	 * @param data The String of data to use as the source.
	 * @return A {@link com.csvreader.CsvReader CsvReader} object using the
	 *         String of data as the source.
	 */
	public static CsvReader parse(String data)
	{
		if( data == null )
		{
			throw new IllegalArgumentException("Parameter data can not be null.");
		}

		return new CsvReader(new StringReader(data));
	}

	/**
	 * Reads another record.
	 * 
	 * @return Whether another record was successfully read or not.
	 * @exception IOException Thrown if an error occurs while reading data from
	 *                the source stream.
	 */
	public boolean readRecord() throws IOException
	{
		checkClosed();

		columnsCount = 0;
		rawBuffer.position = 0;

		dataBuffer.lineStart = dataBuffer.position;

		hasReadNextLine = false;

		// check to see if we've already found the end of data

		if( hasMoreData )
		{
			// loop over the data stream until the end of data is found
			// or the end of the record is found

			do
			{
				if( dataBuffer.position == dataBuffer.count )
				{
					checkDataLength();
				}
				else
				{
					startedWithQualifier = false;

					// grab the current letter as a char

					char currentLetter = dataBuffer.buffer[dataBuffer.position];

					if( userSettings.useTextQualifier && currentLetter == userSettings.textQualifier )
					{
						// this will be a text qualified column, so
						// we need to set startedWithQualifier to make it
						// enter the seperate branch to handle text
						// qualified columns

						lastLetter = currentLetter;

						// read qualified
						startedColumn = true;
						dataBuffer.columnStart = dataBuffer.position + 1;
						startedWithQualifier = true;
						boolean lastLetterWasQualifier = false;

						char escapeChar = QUOTE;

						if( userSettings.escapeMode == ESCAPE_MODE_BACKSLASH )
						{
							escapeChar = BACKSLASH;
						}

						boolean eatingTrailingJunk = false;
						boolean lastLetterWasEscape = false;
						boolean readingComplexEscape = false;
						ComplexEscape escape = ComplexEscape.UNICODE;
						int escapeLength = 0;
						char escapeValue = (char) 0;

						dataBuffer.position++;

						do
						{
							if( dataBuffer.position == dataBuffer.count )
							{
								checkDataLength();
							}
							else
							{
								// grab the current letter as a char

								currentLetter = dataBuffer.buffer[dataBuffer.position];

								if( eatingTrailingJunk )
								{
									dataBuffer.columnStart = dataBuffer.position + 1;

									if( currentLetter == userSettings.delimiter )
									{
										endColumn();
									}
									else if( (!useCustomRecordDelimiter && (currentLetter == CR || currentLetter == LF))
										|| (useCustomRecordDelimiter && currentLetter == userSettings.recordDelimiter) )
									{
										endColumn();

										endRecord();
									}
								}
								else if( readingComplexEscape )
								{
									escapeLength++;

									switch( escape )
									{
										case UNICODE:
											escapeValue *= (char) 16;
											escapeValue += hexToDec(currentLetter);

											if( escapeLength == 4 )
											{
												readingComplexEscape = false;
											}

											break;
										case OCTAL:
											escapeValue *= (char) 8;
											escapeValue += (char) (currentLetter - '0');

											if( escapeLength == 3 )
											{
												readingComplexEscape = false;
											}

											break;
										case DECIMAL:
											escapeValue *= (char) 10;
											escapeValue += (char) (currentLetter - '0');

											if( escapeLength == 3 )
											{
												readingComplexEscape = false;
											}

											break;
										case HEX:
											escapeValue *= (char) 16;
											escapeValue += hexToDec(currentLetter);

											if( escapeLength == 2 )
											{
												readingComplexEscape = false;
											}

											break;
									}

									if( !readingComplexEscape )
									{
										appendLetter(escapeValue);
									}
									else
									{
										dataBuffer.columnStart = dataBuffer.position + 1;
									}
								}
								else if( currentLetter == userSettings.textQualifier )
								{
									if( lastLetterWasEscape )
									{
										lastLetterWasEscape = false;
										lastLetterWasQualifier = false;
									}
									else
									{
										updateCurrentValue();

										if( userSettings.escapeMode == ESCAPE_MODE_DOUBLED )
										{
											lastLetterWasEscape = true;
										}

										lastLetterWasQualifier = true;
									}
								}
								else if( userSettings.escapeMode == ESCAPE_MODE_BACKSLASH && lastLetterWasEscape )
								{
									switch( currentLetter )
									{
										case 'n':
											appendLetter(LF);
											break;
										case 'r':
											appendLetter(CR);
											break;
										case 't':
											appendLetter(TAB);
											break;
										case 'b':
											appendLetter(BACKSPACE);
											break;
										case 'f':
											appendLetter(FORM_FEED);
											break;
										case 'e':
											appendLetter(ESCAPE);
											break;
										case 'v':
											appendLetter(VERTICAL_TAB);
											break;
										case 'a':
											appendLetter(ALERT);
											break;
										case '0':
										case '1':
										case '2':
										case '3':
										case '4':
										case '5':
										case '6':
										case '7':
											escape = ComplexEscape.OCTAL;
											readingComplexEscape = true;
											escapeLength = 1;
											escapeValue = (char) (currentLetter - '0');
											dataBuffer.columnStart = dataBuffer.position + 1;
											break;
										case 'u':
										case 'x':
										case 'o':
										case 'd':
										case 'U':
										case 'X':
										case 'O':
										case 'D':
											switch( currentLetter )
											{
												case 'u':
												case 'U':
													escape = ComplexEscape.UNICODE;
													break;
												case 'x':
												case 'X':
													escape = ComplexEscape.HEX;
													break;
												case 'o':
												case 'O':
													escape = ComplexEscape.OCTAL;
													break;
												case 'd':
												case 'D':
													escape = ComplexEscape.DECIMAL;
													break;
											}

											readingComplexEscape = true;
											escapeLength = 0;
											escapeValue = (char) 0;
											dataBuffer.columnStart = dataBuffer.position + 1;

											break;
										default:
											break;
									}

									lastLetterWasEscape = false;

									// can only happen for ESCAPE_MODE_BACKSLASH
								}
								else if( currentLetter == escapeChar )
								{
									updateCurrentValue();
									lastLetterWasEscape = true;
								}
								else
								{
									if( lastLetterWasQualifier )
									{
										if( currentLetter == userSettings.delimiter )
										{
											endColumn();
										}
										else if( (!useCustomRecordDelimiter && (currentLetter == CR || currentLetter == LF))
											|| (useCustomRecordDelimiter && currentLetter == userSettings.recordDelimiter) )
										{
											endColumn();

											endRecord();
										}
										else
										{
											dataBuffer.columnStart = dataBuffer.position + 1;

											eatingTrailingJunk = true;
										}

										// make sure to clear the flag for next
										// run of the loop

										lastLetterWasQualifier = false;
									}
								}

								// keep track of the last letter because we need
								// it for several key decisions

								lastLetter = currentLetter;

								if( startedColumn )
								{
									dataBuffer.position++;

									if( userSettings.safetySwitch
										&& dataBuffer.position - dataBuffer.columnStart + columnBuffer.position > 100000 )
									{
										close();

										throw new IOException("Maximum column length of 100,000 exceeded in column "
											+ NumberFormat.getIntegerInstance().format(columnsCount) + " in record "
											+ NumberFormat.getIntegerInstance().format(currentRecord)
											+ ". Set the SafetySwitch property to false"
											+ " if you're expecting column lengths greater than 100,000 characters to"
											+ " avoid this error.");
									}
								}
							} // end else

						}
						while( hasMoreData && startedColumn );
					}
					else if( currentLetter == userSettings.delimiter )
					{
						// we encountered a column with no data, so
						// just send the end column

						lastLetter = currentLetter;

						endColumn();
					}
					else if( useCustomRecordDelimiter && currentLetter == userSettings.recordDelimiter )
					{
						// this will skip blank lines
						if( startedColumn || columnsCount > 0 || !userSettings.skipEmptyRecords )
						{
							endColumn();

							endRecord();
						}
						else
						{
							dataBuffer.lineStart = dataBuffer.position + 1;
						}

						lastLetter = currentLetter;
					}
					else if( !useCustomRecordDelimiter && (currentLetter == CR || currentLetter == LF) )
					{
						// this will skip blank lines
						if( startedColumn || columnsCount > 0
							|| (!userSettings.skipEmptyRecords && (currentLetter == CR || lastLetter != CR)) )
						{
							endColumn();

							endRecord();
						}
						else
						{
							dataBuffer.lineStart = dataBuffer.position + 1;
						}

						lastLetter = currentLetter;
					}
					else if( userSettings.useComments && columnsCount == 0 && currentLetter == userSettings.comment )
					{
						// encountered a comment character at the beginning of
						// the line so just ignore the rest of the line

						lastLetter = currentLetter;

						skipLine();
					}
					else if( userSettings.trimWhitespace && (currentLetter == SPACE || currentLetter == TAB) )
					{
						// do nothing, this will trim leading whitespace
						// for both text qualified columns and non

						startedColumn = true;
						dataBuffer.columnStart = dataBuffer.position + 1;
					}
					else
					{
						// since the letter wasn't a special letter, this
						// will be the first letter of our current column

						startedColumn = true;
						dataBuffer.columnStart = dataBuffer.position;
						boolean lastLetterWasBackslash = false;
						boolean readingComplexEscape = false;
						ComplexEscape escape = ComplexEscape.UNICODE;
						int escapeLength = 0;
						char escapeValue = (char) 0;

						boolean firstLoop = true;

						do
						{
							if( !firstLoop && dataBuffer.position == dataBuffer.count )
							{
								checkDataLength();
							}
							else
							{
								if( !firstLoop )
								{
									// grab the current letter as a char
									currentLetter = dataBuffer.buffer[dataBuffer.position];
								}

								if( !userSettings.useTextQualifier && userSettings.escapeMode == ESCAPE_MODE_BACKSLASH
									&& currentLetter == BACKSLASH )
								{
									if( lastLetterWasBackslash )
									{
										lastLetterWasBackslash = false;
									}
									else
									{
										updateCurrentValue();
										lastLetterWasBackslash = true;
									}
								}
								else if( readingComplexEscape )
								{
									escapeLength++;

									switch( escape )
									{
										case UNICODE:
											escapeValue *= (char) 16;
											escapeValue += hexToDec(currentLetter);

											if( escapeLength == 4 )
											{
												readingComplexEscape = false;
											}

											break;
										case OCTAL:
											escapeValue *= (char) 8;
											escapeValue += (char) (currentLetter - '0');

											if( escapeLength == 3 )
											{
												readingComplexEscape = false;
											}

											break;
										case DECIMAL:
											escapeValue *= (char) 10;
											escapeValue += (char) (currentLetter - '0');

											if( escapeLength == 3 )
											{
												readingComplexEscape = false;
											}

											break;
										case HEX:
											escapeValue *= (char) 16;
											escapeValue += hexToDec(currentLetter);

											if( escapeLength == 2 )
											{
												readingComplexEscape = false;
											}

											break;
									}

									if( !readingComplexEscape )
									{
										appendLetter(escapeValue);
									}
									else
									{
										dataBuffer.columnStart = dataBuffer.position + 1;
									}
								}
								else if( userSettings.escapeMode == ESCAPE_MODE_BACKSLASH && lastLetterWasBackslash )
								{
									switch( currentLetter )
									{
										case 'n':
											appendLetter(LF);
											break;
										case 'r':
											appendLetter(CR);
											break;
										case 't':
											appendLetter(TAB);
											break;
										case 'b':
											appendLetter(BACKSPACE);
											break;
										case 'f':
											appendLetter(FORM_FEED);
											break;
										case 'e':
											appendLetter(ESCAPE);
											break;
										case 'v':
											appendLetter(VERTICAL_TAB);
											break;
										case 'a':
											appendLetter(ALERT);
											break;
										case '0':
										case '1':
										case '2':
										case '3':
										case '4':
										case '5':
										case '6':
										case '7':
											escape = ComplexEscape.OCTAL;
											readingComplexEscape = true;
											escapeLength = 1;
											escapeValue = (char) (currentLetter - '0');
											dataBuffer.columnStart = dataBuffer.position + 1;
											break;
										case 'u':
										case 'x':
										case 'o':
										case 'd':
										case 'U':
										case 'X':
										case 'O':
										case 'D':
											switch( currentLetter )
											{
												case 'u':
												case 'U':
													escape = ComplexEscape.UNICODE;
													break;
												case 'x':
												case 'X':
													escape = ComplexEscape.HEX;
													break;
												case 'o':
												case 'O':
													escape = ComplexEscape.OCTAL;
													break;
												case 'd':
												case 'D':
													escape = ComplexEscape.DECIMAL;
													break;
											}

											readingComplexEscape = true;
											escapeLength = 0;
											escapeValue = (char) 0;
											dataBuffer.columnStart = dataBuffer.position + 1;

											break;
										default:
											break;
									}

									lastLetterWasBackslash = false;
								}
								else
								{
									if( currentLetter == userSettings.delimiter )
									{
										endColumn();
									}
									else if( (!useCustomRecordDelimiter && (currentLetter == CR || currentLetter == LF))
										|| (useCustomRecordDelimiter && currentLetter == userSettings.recordDelimiter) )
									{
										endColumn();

										endRecord();
									}
								}

								// keep track of the last letter because we need
								// it for several key decisions

								lastLetter = currentLetter;
								firstLoop = false;

								if( startedColumn )
								{
									dataBuffer.position++;

									if( userSettings.safetySwitch
										&& dataBuffer.position - dataBuffer.columnStart + columnBuffer.position > 100000 )
									{
										close();

										throw new IOException("Maximum column length of 100,000 exceeded in column "
											+ NumberFormat.getIntegerInstance().format(columnsCount) + " in record "
											+ NumberFormat.getIntegerInstance().format(currentRecord)
											+ ". Set the SafetySwitch property to false"
											+ " if you're expecting column lengths greater than 100,000 characters to"
											+ " avoid this error.");
									}
								}
							} // end else
						}
						while( hasMoreData && startedColumn );
					}

					if( hasMoreData )
					{
						dataBuffer.position++;
					}
				} // end else
			}
			while( hasMoreData && !hasReadNextLine );

			// check to see if we hit the end of the file
			// without processing the current record

			if( startedColumn || lastLetter == userSettings.delimiter )
			{
				endColumn();

				endRecord();
			}
		}

		if( userSettings.captureRawRecord )
		{
			if( hasMoreData )
			{
				if( rawBuffer.position == 0 )
				{
					rawRecord = new String(dataBuffer.buffer, dataBuffer.lineStart, dataBuffer.position
						- dataBuffer.lineStart - 1);
				}
				else
				{
					rawRecord = new String(rawBuffer.buffer, 0, rawBuffer.position)
						+ new String(dataBuffer.buffer, dataBuffer.lineStart, dataBuffer.position
							- dataBuffer.lineStart - 1);
				}
			}
			else
			{
				// for hasMoreData to ever be false, all data would have had to
				// have been
				// copied to the raw buffer
				rawRecord = new String(rawBuffer.buffer, 0, rawBuffer.position);
			}
		}
		else
		{
			rawRecord = "";
		}

		return hasReadNextLine;
	}

	/**
	 * @exception IOException Thrown if an error occurs while reading data from
	 *                the source stream.
	 */
	private void checkDataLength() throws IOException
	{
		if( !initialized )
		{
			if( fileName != null )
			{
				inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset),
					MAX_FILE_BUFFER_SIZE);
			}

			charset = null;
			initialized = true;
		}

		updateCurrentValue();

		if( userSettings.captureRawRecord && dataBuffer.count > 0 )
		{
			if( rawBuffer.buffer.length - rawBuffer.position < dataBuffer.count - dataBuffer.lineStart )
			{
				int newLength = rawBuffer.buffer.length
					+ Math.max(dataBuffer.count - dataBuffer.lineStart, rawBuffer.buffer.length);

				char[] holder = new char[newLength];

				System.arraycopy(rawBuffer.buffer, 0, holder, 0, rawBuffer.position);

				rawBuffer.buffer = holder;
			}

			System.arraycopy(dataBuffer.buffer, dataBuffer.lineStart, rawBuffer.buffer, rawBuffer.position,
				dataBuffer.count - dataBuffer.lineStart);

			rawBuffer.position += dataBuffer.count - dataBuffer.lineStart;
		}

		try
		{
			dataBuffer.count = inputStream.read(dataBuffer.buffer, 0, dataBuffer.buffer.length);
		}
		catch( IOException ex )
		{
			close();

			throw ex;
		}

		// if no more data could be found, set flag stating that
		// the end of the data was found

		if( dataBuffer.count == -1 )
		{
			hasMoreData = false;
		}

		dataBuffer.position = 0;
		dataBuffer.lineStart = 0;
		dataBuffer.columnStart = 0;
	}

	/**
	 * Read the first record of data as column headers.
	 * 
	 * @return Whether the header record was successfully read or not.
	 * @exception IOException Thrown if an error occurs while reading data from
	 *                the source stream.
	 */
	public boolean readHeaders() throws IOException
	{
		boolean result = readRecord();

		// copy the header data from the column array
		// to the header string array

		headersHolder.length = columnsCount;

		headersHolder.headers = new String[columnsCount];

		for( int i = 0; i < headersHolder.length; i++ )
		{
			String columnValue = get(i);

			headersHolder.headers[i] = columnValue;

			// if there are duplicate header names, we will save the last one
			headersHolder.indexByName.put(columnValue.toLowerCase(), i);
		}

		if( result )
		{
			currentRecord--;
		}

		columnsCount = 0;

		return result;
	}

	/**
	 * Returns the column header value for a given column index.
	 * 
	 * @param columnIndex The index of the header column being requested.
	 * @return The value of the column header at the given column index.
	 * @exception IOException Thrown if this object has already been closed.
	 */
	public String getHeader(int columnIndex) throws IOException
	{
		checkClosed();

		// check to see if we have read the header record yet

		// check to see if the column index is within the bounds
		// of our header array

		if( columnIndex > -1 && columnIndex < headersHolder.length )
		{
			// return the processed header data for this column

			return headersHolder.headers[columnIndex];
		}
		return "";
	}

	public boolean isQualified(int columnIndex) throws IOException
	{
		checkClosed();

		if( columnIndex < columnsCount && columnIndex > -1 )
		{
			return isQualified[columnIndex];
		}
		return false;
	}

	/**
	 * @exception IOException Thrown if a very rare extreme exception occurs
	 *                during parsing, normally resulting from improper data
	 *                format.
	 */
	private void endColumn() throws IOException
	{
		String currentValue = "";

		// must be called before setting startedColumn = false
		if( startedColumn )
		{
			if( columnBuffer.position == 0 )
			{
				if( dataBuffer.columnStart < dataBuffer.position )
				{
					int end = dataBuffer.position - 1;

					if( userSettings.trimWhitespace && !startedWithQualifier )
					{
						while( end >= dataBuffer.columnStart
							&& (dataBuffer.buffer[end] == SPACE || dataBuffer.buffer[end] == TAB) )
						{
							end--;
						}
					}

					currentValue = new String(dataBuffer.buffer, dataBuffer.columnStart, end
						- dataBuffer.columnStart + 1);
				}
			}
			else
			{
				updateCurrentValue();

				int end = columnBuffer.position - 1;

				if( userSettings.trimWhitespace && !startedWithQualifier )
				{
					while( end >= 0 && (columnBuffer.buffer[end] == SPACE || columnBuffer.buffer[end] == TAB) )
					{
						end--;
					}
				}

				currentValue = new String(columnBuffer.buffer, 0, end + 1);
			}
		}

		columnBuffer.position = 0;

		startedColumn = false;

		if( columnsCount >= 100000 && userSettings.safetySwitch )
		{
			close();

			throw new IOException("Maximum column count of 100,000 exceeded in record "
				+ NumberFormat.getIntegerInstance().format(currentRecord) + ". Set the SafetySwitch property to false"
				+ " if you're expecting more than 100,000 columns per record to" + " avoid this error.");
		}

		// check to see if our current holder array for
		// column chunks is still big enough to handle another
		// column chunk

		if( columnsCount == values.length )
		{
			// holder array needs to grow to be able to hold another column
			int newLength = values.length * 2;

			String[] holder = new String[newLength];

			System.arraycopy(values, 0, holder, 0, values.length);

			values = holder;

			boolean[] qualifiedHolder = new boolean[newLength];

			System.arraycopy(isQualified, 0, qualifiedHolder, 0, isQualified.length);

			isQualified = qualifiedHolder;
		}

		values[columnsCount] = currentValue;

		isQualified[columnsCount] = startedWithQualifier;

		currentValue = "";

		columnsCount++;
	}

	private void appendLetter(char letter)
	{
		if( columnBuffer.position == columnBuffer.buffer.length )
		{
			int newLength = columnBuffer.buffer.length * 2;

			char[] holder = new char[newLength];

			System.arraycopy(columnBuffer.buffer, 0, holder, 0, columnBuffer.position);

			columnBuffer.buffer = holder;
		}
		columnBuffer.buffer[columnBuffer.position++] = letter;
		dataBuffer.columnStart = dataBuffer.position + 1;
	}

	private void updateCurrentValue()
	{
		if( startedColumn && dataBuffer.columnStart < dataBuffer.position )
		{
			if( columnBuffer.buffer.length - columnBuffer.position < dataBuffer.position - dataBuffer.columnStart )
			{
				int newLength = columnBuffer.buffer.length
					+ Math.max(dataBuffer.position - dataBuffer.columnStart, columnBuffer.buffer.length);

				char[] holder = new char[newLength];

				System.arraycopy(columnBuffer.buffer, 0, holder, 0, columnBuffer.position);

				columnBuffer.buffer = holder;
			}

			System.arraycopy(dataBuffer.buffer, dataBuffer.columnStart, columnBuffer.buffer, columnBuffer.position,
				dataBuffer.position - dataBuffer.columnStart);

			columnBuffer.position += dataBuffer.position - dataBuffer.columnStart;
		}

		dataBuffer.columnStart = dataBuffer.position + 1;
	}

	/**
	 * @exception IOException Thrown if an error occurs while reading data from
	 *                the source stream.
	 */
	private void endRecord() throws IOException
	{
		// this flag is used as a loop exit condition
		// during parsing

		hasReadNextLine = true;

		currentRecord++;
	}

	/**
	 * Gets the corresponding column index for a given column header name.
	 * 
	 * @param headerName The header name of the column.
	 * @return The column index for the given column header name.&nbsp;Returns
	 *         -1 if not found.
	 * @exception IOException Thrown if this object has already been closed.
	 */
	public int getIndex(String headerName) throws IOException
	{
		checkClosed();

		Object indexValue = headersHolder.indexByName.get(headerName.toLowerCase());

		if( indexValue != null )
		{
			return ((Integer) indexValue).intValue();
		}
		return -1;
	}

	/**
	 * Skips the next record of data by parsing each column.&nbsp;Does not
	 * increment {@link com.csvreader.CsvReader#getCurrentRecord
	 * getCurrentRecord()}.
	 * 
	 * @return Whether another record was successfully skipped or not.
	 * @exception IOException Thrown if an error occurs while reading data from
	 *                the source stream.
	 */
	public boolean skipRecord() throws IOException
	{
		checkClosed();

		boolean recordRead = false;

		if( hasMoreData )
		{
			recordRead = readRecord();

			if( recordRead )
			{
				currentRecord--;
			}
		}

		return recordRead;
	}

	/**
	 * Skips the next line of data using the standard end of line characters and
	 * does not do any column delimited parsing.
	 * 
	 * @return Whether a line was successfully skipped or not.
	 * @exception IOException Thrown if an error occurs while reading data from
	 *                the source stream.
	 */
	public boolean skipLine() throws IOException
	{
		checkClosed();

		// clear public column values for current line

		columnsCount = 0;

		boolean skippedLine = false;

		if( hasMoreData )
		{
			boolean foundEol = false;

			do
			{
				if( dataBuffer.position == dataBuffer.count )
				{
					checkDataLength();
				}
				else
				{
					skippedLine = true;

					// grab the current letter as a char

					char currentLetter = dataBuffer.buffer[dataBuffer.position];

					if( currentLetter == CR || currentLetter == LF )
					{
						foundEol = true;
					}

					// keep track of the last letter because we need
					// it for several key decisions

					lastLetter = currentLetter;

					if( !foundEol )
					{
						dataBuffer.position++;
					}

				} // end else
			}
			while( hasMoreData && !foundEol );

			columnBuffer.position = 0;

			dataBuffer.lineStart = dataBuffer.position + 1;
		}

		rawBuffer.position = 0;
		rawRecord = "";

		return skippedLine;
	}

	/**
	 * Closes and releases all related resources.
	 */
	public void close()
	{
		if( !closed )
		{
			close(true);

			closed = true;
		}
	}

	private void close(boolean closing)
	{
		if( !closed )
		{
			if( closing )
			{
				charset = null;
				headersHolder.headers = null;
				headersHolder.indexByName = null;
				dataBuffer.buffer = null;
				columnBuffer.buffer = null;
				rawBuffer.buffer = null;
			}

			try
			{
				if( initialized )
				{
					inputStream.close();
				}
			}
			catch( Exception e )
			{
				// just eat the exception
			}

			inputStream = null;

			closed = true;
		}
	}

	/**
	 * @exception IOException Thrown if this object has already been closed.
	 */
	private void checkClosed() throws IOException
	{
		if( closed )
		{
			throw new IOException("This instance of the CsvReader class has already been closed.");
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		close(false);
		super.finalize();
	}

	private enum ComplexEscape
	{
		UNICODE, OCTAL, DECIMAL, HEX
	}

	private static char hexToDec(char hex)
	{
		char result;

		if( hex >= 'a' )
		{
			result = (char) (hex - 'a' + 10);
		}
		else if( hex >= 'A' )
		{
			result = (char) (hex - 'A' + 10);
		}
		else
		{
			result = (char) (hex - '0');
		}

		return result;
	}

	private static final class DataBuffer
	{
		char[] buffer;
		int position;
		// / <summary>
		// / How much usable data has been read into the stream,
		// / which will not always be as long as Buffer.Length.
		// / </summary>
		int count;
		// / <summary>
		// / The position of the cursor in the buffer when the
		// / current column was started or the last time data
		// / was moved out to the column buffer.
		// / </summary>
		int columnStart;
		int lineStart;

		public DataBuffer()
		{
			buffer = new char[MAX_BUFFER_SIZE];
			position = 0;
			count = 0;
			columnStart = 0;
			lineStart = 0;
		}
	}

	private static final class ColumnBuffer
	{
		char[] buffer;
		int position;

		private ColumnBuffer()
		{
			buffer = new char[INITIAL_COLUMN_BUFFER_SIZE];
			position = 0;
		}
	}

	private static final class RawRecordBuffer
	{
		char[] buffer;
		int position;

		private RawRecordBuffer()
		{
			buffer = new char[INITIAL_COLUMN_BUFFER_SIZE * INITIAL_COLUMN_COUNT];
			position = 0;
		}
	}

	private static final class UserSettings
	{
		char textQualifier;
		boolean trimWhitespace;
		boolean useTextQualifier;
		char delimiter;
		char recordDelimiter;
		char comment;
		boolean useComments;
		int escapeMode;
		boolean safetySwitch;
		boolean skipEmptyRecords;
		boolean captureRawRecord;

		private UserSettings()
		{
			textQualifier = QUOTE;
			trimWhitespace = true;
			useTextQualifier = true;
			delimiter = COMMA;
			recordDelimiter = NULL;
			comment = POUND;
			useComments = false;
			escapeMode = CsvReader.ESCAPE_MODE_DOUBLED;
			safetySwitch = true;
			skipEmptyRecords = true;
			captureRawRecord = true;
		}
	}

	private static final class HeadersHolder
	{
		String[] headers;
		int length;
		Map<String, Integer> indexByName;

		private HeadersHolder()
		{
			headers = null;
			length = 0;
			indexByName = new HashMap<String, Integer>();
		}
	}
}