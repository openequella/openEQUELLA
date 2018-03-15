package com.tle.common.i18n;

import javassist.compiler.ast.StringL;

public interface StringLookup {

    String text(String key, Object... vals);

    String key(String local);

    StringLookup prefix(String prefix);

    static StringLookup prefixed(String pfx) {
        return new StringLookup() {
            @Override
            public String text(String key, Object... vals)
            {
                return CurrentLocale.get(key(key), vals);
            }

            @Override
            public String key(String local)
            {
                return pfx+"."+local;
            }

            @Override
            public StringLookup prefix(String prefix)
            {
                return StringLookup.prefixed(key(prefix));
            }
        };
    }
}
