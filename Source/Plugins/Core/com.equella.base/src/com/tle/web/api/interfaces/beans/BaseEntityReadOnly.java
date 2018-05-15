package com.tle.web.api.interfaces.beans;

import java.util.Collection;

public class BaseEntityReadOnly {

    private final Collection<String> granted;

    public BaseEntityReadOnly(Collection<String> granted)
    {
        this.granted = granted;
    }

    public Collection<String> getGranted()
    {
        return granted;
    }
}
