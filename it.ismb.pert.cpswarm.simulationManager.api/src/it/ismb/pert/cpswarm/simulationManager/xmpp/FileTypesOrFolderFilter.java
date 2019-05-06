package it.ismb.pert.cpswarm.simulationManager.xmpp;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

final class FileTypesOrFolderFilter implements FileFilter {
    private final Set<String> supportedExceptions;

    public FileTypesOrFolderFilter(final Set<String> supportedExceptions) {
        this.supportedExceptions = supportedExceptions;
    }

  public boolean accept(File pathname) {
    	String extension = pathname.getName().split("\\.")[1];
    	boolean result = supportedExceptions.contains(extension)  || pathname.isDirectory();
        return result;
    }
}