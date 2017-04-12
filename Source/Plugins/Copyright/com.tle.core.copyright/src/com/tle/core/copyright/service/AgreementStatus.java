/**
 * 
 */
package com.tle.core.copyright.service;

import com.tle.beans.filesystem.FileHandle;

public class AgreementStatus
{
	private boolean inactive;
	private boolean needsAgreement;
	private FileHandle agreementFile;

	public boolean isInactive()
	{
		return inactive;
	}

	public void setInactive(boolean inactive)
	{
		this.inactive = inactive;
	}

	public FileHandle getAgreementFile()
	{
		return agreementFile;
	}

	public void setAgreementFile(FileHandle agreementFile)
	{
		this.agreementFile = agreementFile;
	}

	public boolean isNeedsAgreement()
	{
		return needsAgreement;
	}

	public void setNeedsAgreement(boolean needsAgreement)
	{
		this.needsAgreement = needsAgreement;
	}
}