package com.tle.web.inplaceeditor;

import java.awt.Component;
import java.io.IOException;

public interface Opener
{
	void openWith(Component parent, String filepath, String mimetype) throws IOException;
}
