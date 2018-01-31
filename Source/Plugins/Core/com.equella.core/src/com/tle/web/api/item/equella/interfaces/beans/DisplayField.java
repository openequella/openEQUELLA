package com.tle.web.api.item.equella.interfaces.beans;

import com.tle.common.interfaces.I18NString;

public class DisplayField
{
    private final I18NString name;
    private final I18NString html;

    public DisplayField(I18NString name, I18NString html)
    {
        this.name = name;
        this.html = html;
    }

    public I18NString getName()
    {
        return name;
    }

    public I18NString getHtml()
    {
        return html;
    }
}
