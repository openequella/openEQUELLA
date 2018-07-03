package com.tle.core.migration;

import com.tle.core.migration.Migration;
import com.tle.core.migration.MigrationExt;
import com.tle.core.migration.log.MigrationLog;
import org.java.plugin.registry.Extension;

public class MigrationState
{
    private final MigrationExt extension;
    private final MigrationLog logEntry;
    private boolean skip;
    private boolean execute;
    private boolean obsoleted;

    public MigrationState(MigrationExt extension, MigrationLog logEntry)
    {
        this.extension = extension;
        this.logEntry = logEntry;
    }

    public boolean isPlaceHolder()
    {
        return extension.placeholder();
    }

    public boolean needsProcessing()
    {
        return logEntry == null || logEntry.getStatus() == MigrationLog.LogStatus.ERRORED;
    }

    public boolean wasSkippedAlready()
    {
        return logEntry != null && logEntry.getStatus() == MigrationLog.LogStatus.SKIPPED;
    }

    public boolean wasExecutedAlready()
    {
        return logEntry != null && logEntry.getStatus() == MigrationLog.LogStatus.EXECUTED;
    }

    public String getId()
    {
        return extension.id();
    }

    public boolean isCanRetry()
    {
        return logEntry != null && logEntry.isCanRetry();
    }

    public MigrationLog.LogStatus getStatus()
    {
        if( logEntry == null )
        {
            return null;
        }
        return logEntry.getStatus();
    }

    public MigrationLog getLogEntry()
    {
        return logEntry;
    }

    public boolean isSkip()
    {
        return skip;
    }

    public void setSkip(boolean skip)
    {
        this.skip = skip;
    }

    public boolean isObsoleted()
    {
        return obsoleted;
    }

    public void setObsoleted(boolean obsoleted)
    {
        this.obsoleted = obsoleted;
    }

    public boolean isExecute()
    {
        return execute;
    }

    public void setExecute(boolean execute)
    {
        this.execute = execute;
    }

    public Migration getMigration()
    {
        return extension.migration();
    }
}
