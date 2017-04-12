package com.tle.common.applet.client;

import java.awt.Component;
import java.io.File;

public interface FileWorker
{
	void setFile(File file);

	void setComponent(Component component);

	void start();
}