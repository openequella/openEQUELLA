package com.tle.core.xml.service.impl;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import java.sql.Timestamp;

public class OldSqlTimestampConverter extends AbstractSingleValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(Timestamp.class);
    }

    public Object fromString(String str) {
        return Timestamp.valueOf(str);
    }

}
