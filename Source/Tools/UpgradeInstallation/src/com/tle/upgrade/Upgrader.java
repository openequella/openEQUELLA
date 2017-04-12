package com.tle.upgrade;

import java.io.File;
import java.util.List;

public interface Upgrader
{
	String getId();

	boolean isRunOnInstall();

	boolean isBackwardsCompatible();

	List<UpgradeDepends> getDepends();

	void upgrade(UpgradeResult result, File tleInstallDir) throws Exception;
}
