package com.tle.web.manualdatafixes.fixes;

import com.tle.core.guice.BindFactory;

@BindFactory
public interface GenerateThumbnailOpFactory
{
	GenerateThumbnailOperation generateThumbnail(boolean force);

}
