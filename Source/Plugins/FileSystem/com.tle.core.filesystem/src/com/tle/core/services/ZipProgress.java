package com.tle.core.services;

public interface ZipProgress
{
    int getTotalFiles();

    int getCurrentFile();

    boolean isFinished();
}
