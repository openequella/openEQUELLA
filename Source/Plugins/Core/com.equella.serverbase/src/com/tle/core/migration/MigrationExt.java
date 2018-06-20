package com.tle.core.migration;

import java.util.Date;
import java.util.Set;

public interface MigrationExt {

    Date date();

    String id();

    boolean placeholder();

    Migration migration();

    boolean initial();

    boolean system();

    Set<String> getObsoletedBy();

    Set<String> getFixes();

    Set<String> getIfSkipped();

    Set<String> getDepends();
}
