package com.tle.blackboard.common.propbag;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * keeps reading until we get a '&amp;' and then buffer the rest as 'suspect'
 * until a ';' is read. Only then can we determine if the suspect buffer is
 * illegal and can be discarded, OR it's okay and we can 'read' it in. If we
 * read in an illegal unicode char, it can be discarded straight away. This code
 * is deliberately mad-hacks (eg unrolled loops and inline method code) for
 * performance reasons
 * 
 * @author aholland
 */
public class BadCharacterFilterReader extends FilterReader
{
	private static final int DEFAULT_CHUNK = 1000;
	private static final int MAX_SUSPECTION_LENGTH = 10;

	protected final char[] chunkBuff;
	protected final char[] suspect = new char[MAX_SUSPECTION_LENGTH];

	protected int suspectStart;
	protected int suspectLength;

	protected boolean didDiscard;

	public BadCharacterFilterReader(final Reader reader)
	{
		this(reader, DEFAULT_CHUNK);
	}

	public BadCharacterFilterReader(final Reader reader, final int bufferSize)
	{
		super(reader);
		if( bufferSize <= MAX_SUSPECTION_LENGTH )
		{
			throw new IllegalArgumentException("bufferSize must be at least " + MAX_SUSPECTION_LENGTH); //$NON-NLS-1$
		}
		chunkBuff = new char[bufferSize];

		// I assume the super (Reader) constructor initialises its lock Object?
		// But just in case, affirm it here.
		if( lock == null )
		{
			lock = new Object();
		}
	}

	@Override
	public int read()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(final char[] outBuffer, final int originalWriteOffset, final int len) throws IOException
	{
		synchronized( lock )
		{
			if( in == null )
			{
				throw new IOException("Stream closed"); //$NON-NLS-1$
			}

			int writeOffset = originalWriteOffset;
			final int maxWriteSize = (chunkBuff.length < len ? chunkBuff.length : len);

			// always flush the suspect buffer it needs to be.
			// suspectStart > 0 means we have already started writing it
			if( suspectStart > 0 )
			{
				final int slen = (suspectLength < maxWriteSize ? suspectLength : maxWriteSize);
				return writeSuspect(outBuffer, originalWriteOffset, slen);
			}
			else
			{
				// note that we may be writing out a suspect buffer filled from
				// a previous call
				// so we cannot always read in the full allowed amount
				final int maxWrappedReadSize = (maxWriteSize > suspectLength ? maxWriteSize - suspectLength : 0);
				final int wrappedReadSize = in.read(chunkBuff, 0, maxWrappedReadSize);

				// not end of stream?
				if( wrappedReadSize >= 0 )
				{
					for( int i = 0; i < wrappedReadSize; ++i )
					{
						final int next = chunkBuff[i];

						// invalid unicode char
						if( (next < 0x20 && next != 0x9 && next != 0xA && next != 0xD)
							|| (next > 0xD7FF && next < 0xE000) || (next > 0xFFFD && next < 0x10000) || next > 0x10FFFF )
						{
							// discard
							didDiscard = true;
						}
						else
						{
							// are we suspicious?
							if( suspectLength > 0 )
							{
								// is it a ';'?
								if( next == ';' )
								{
									suspect[suspectStart + suspectLength] = (char) next;
									++suspectLength;
									if( suspectLength == MAX_SUSPECTION_LENGTH )
									{
										// it clearly isn't a suspect buffer
										writeOffset += writeSuspect(outBuffer, writeOffset, len - writeOffset);
									}
									else
									{
										// so, is it dodgy? buffer at this point
										// could be #x999, #99, amp or anything!
										if( isDodgy() )
										{
											// it IS dodgy. clear out suspect
											// buffer
											suspectStart = 0;
											suspectLength = 0;
											didDiscard = true;
										}
										else
										{
											writeOffset += writeSuspect(outBuffer, writeOffset, len - writeOffset);
										}
									}
								}
								else
								{
									suspect[suspectStart + suspectLength] = (char) next;
									++suspectLength;
									if( suspectStart + suspectLength == MAX_SUSPECTION_LENGTH )
									{
										// it clearly isn't a suspect buffer
										writeOffset += writeSuspect(outBuffer, writeOffset, len - writeOffset);
									}
								}
							}
							else
							{
								// seems ok, is it a '&'?
								if( next == '&' )
								{
									// start pumping the suspect buffer
									suspect[suspectStart + suspectLength] = '&';
									++suspectLength;
								}
								else
								{
									outBuffer[writeOffset] = (char) next;
									++writeOffset;
								}
							}
						}
					}

					return writeOffset - originalWriteOffset;
				}

				// nothing left to read, do we have a suspect buffer to clear?
				else
				{
					if( suspectLength > 0 )
					{
						final int slen = (suspectLength < maxWriteSize ? suspectLength : maxWriteSize);
						return writeSuspect(outBuffer, originalWriteOffset, slen);
					}

					// this is it. it's all over
					return -1;
				}
			}
		}
	}

	public boolean didDiscard()
	{
		return didDiscard;
	}

	@SuppressWarnings("nls")
	protected int writeSuspect(final char[] output, final int position, final int maxLength)
	{
		final int len = (suspectLength < maxLength ? suspectLength : maxLength);
		if( suspect.length < len + suspectStart || output.length < len + position || len < 0 )
		{
			throw new ArrayIndexOutOfBoundsException("suspect buffer size: " + suspect.length
				+ ", output buffer size: " + output.length + ", suspect start: " + suspectStart + ", position: "
				+ position + ", maxLength: " + maxLength);
		}
		System.arraycopy(suspect, suspectStart, output, position, len);

		// fully written buffer
		if( (len == suspectLength) )
		{
			suspectStart = 0;
			suspectLength = 0;
		}
		else
		{
			suspectStart += len;
			suspectLength -= len;
		}

		return len;
	}

	/**
	 * A very apt name for this code methinks... Preconditions: suspectStart is
	 * ALWAYS zero here suspect[0] == '&' ALWAYS suspect[suspectLength-1] == ';'
	 * ALWAYS
	 * 
	 * @param suspect
	 * @param suspectLength
	 * @return
	 */
	protected boolean isDodgy()
	{
		// if it starts with hash -> then it's a numbered escape sequence
		if( suspect[1] == '#' )
		{
			if( suspectLength > 2 )
			{
				// is the next an x or a number?
				final char c3 = suspect[2];
				if( c3 == 'x' )
				{
					if( suspectLength > 3 )
					{
						// at this point, bugger optimisations it's looking a
						// fair chance
						// bear in mind the suspect will always end in ';' at
						// this point, hence the -4
						final String str = new String(suspect, 3, suspectLength - 4);
						// is this a hex representation of an invalid range?
						try
						{
							final int i = Integer.parseInt(str, 16);
							if( (i < 0x20 && i != 0x9 && i != 0xA && i != 0xD) || (i > 0xD7FF && i < 0xE000)
								|| (i > 0xFFFD && i < 0x10000) || i > 0x10FFFF )
							{
								return true;
							}
						}
						catch( final NumberFormatException n )
						{
							// nope
						}
					}
				}
				else if( c3 == '0' || c3 == '1' || c3 == '2' || c3 == '3' || c3 == '4' || c3 == '5' || c3 == '6'
					|| c3 == '7' || c3 == '8' || c3 == '9' )
				{
					// at this point, bugger optimisations it's looking a fair
					// chance
					// bear in mind the suspect will always end in ';' at this
					// point, hence the -3
					final String str = new String(suspect, 2, suspectLength - 3);
					// is this a decimal representation of an invalid range?
					try
					{
						final int i = Integer.parseInt(str, 10);
						if( (i < 0x20 && i != 0x9 && i != 0xA && i != 0xD) || (i > 0xD7FF && i < 0xE000)
							|| (i > 0xFFFD && i < 0x10000) || i > 0x10FFFF )
						{
							return true;
						}
					}
					catch( final NumberFormatException n )
					{
						// nope
					}
				}
			}
		}
		return false;
	}

	@Override
	public void close() throws IOException
	{
		synchronized( lock )
		{
			if( in != null )
			{
				in.close();
				in = null;
			}
		}
	}
}
