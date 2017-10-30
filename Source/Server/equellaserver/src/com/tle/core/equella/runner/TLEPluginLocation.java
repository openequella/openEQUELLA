package com.tle.core.equella.runner;

import org.java.plugin.registry.ManifestInfo;

import java.net.URL;

public class TLEPluginLocation implements org.java.plugin.PluginManager.PluginLocation
{
    private final String jar;
    private final ManifestInfo manifestInfo;
    private final URL context;
    private final URL manifest;
    private int version = -1;

    @Override
    public boolean equals(Object obj)
    {
        if( this == obj )
        {
            return true;
        }

        if( !(obj instanceof TLEPluginLocation) )
        {
            return false;
        }

        return context.toString().equals(((TLEPluginLocation) obj).context.toString());
    }

    @Override
    public int hashCode()
    {
        return context.toString().hashCode();
    }

    public TLEPluginLocation(ManifestInfo info, String jar, URL context, URL manifest)
    {
        this.manifestInfo = info;
        this.jar = jar;
        this.context = context;
        this.manifest = manifest;
    }

    public int getVersion()
    {
        return version;
    }

    public String getJar()
    {
        return jar;
    }

    @Override
    public URL getContextLocation()
    {
        return context;
    }

    @Override
    public URL getManifestLocation()
    {
        return manifest;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public ManifestInfo getManifestInfo()
    {
        return manifestInfo;
    }
}
