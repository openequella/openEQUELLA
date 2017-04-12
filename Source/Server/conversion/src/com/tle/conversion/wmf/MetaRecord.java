/**
 * 
 */
package com.tle.conversion.wmf;

public class MetaRecord
{
	private int rdSize;
	private short rdFunction;
	private byte[] rdParm;

	public MetaRecord(int rdSize, short rdFunction, byte[] rdParm)
	{
		this.rdSize = rdSize;
		this.rdFunction = rdFunction;
		this.rdParm = rdParm; // arraycopy
	}

	public void initialize(int rdSize, short rdFunction, byte[] rdParm)
	{
		this.rdSize = rdSize;
		this.rdFunction = rdFunction;
		this.rdParm = rdParm; // arraycopy
	}

	public int getSize()
	{
		return rdSize;
	}

	public short getFunction()
	{
		return rdFunction;
	}

	public byte[] getParm()
	{
		return rdParm;
	}
}