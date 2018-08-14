package com.tle.web.sections.equella.ajaxupload;

public class AjaxCallbackResponse {

    private int code;
    private String error;

    public int getCode()
    {
        return code;
    }

    public String getError()
    {
        return error;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public void setError(String error)
    {
        this.error = error;
    }
}
