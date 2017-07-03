package com.tle.common.encryption;

public interface RemoteEncryptionService
{
	String encrypt(String value);

	String decrypt(String value);
}
