package com.tle.web.workflow.guice;

import com.google.inject.AbstractModule;
import com.tle.core.institution.InstitutionService;

import javax.inject.Inject;

public class StaticServices extends AbstractModule
{
    @Inject
    public static InstitutionService institutionService;

    @Override
    protected void configure()
    {
        requestStaticInjection(getClass());
    }
}
