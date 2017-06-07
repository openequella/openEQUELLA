package com.dytech.installer.commands;

import com.dytech.installer.InstallerException;

import java.io.File;

public class CMkExec extends Command {

    private final String file;

    public CMkExec(String file)
    {
        this.file = file;
    }
    @Override
    public void execute() throws InstallerException {

        new File(file).setExecutable(true);
    }

    @Override
    public String toString() {
        return "Making "+file+" executable";
    }
}
