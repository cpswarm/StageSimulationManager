package it.ismb.pert.cpswarm.fileLogFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

// create only one shared instance per runtime.

@Component(immediate=true,
	service = {FileLogFilter.class}
)
public class FileLogFilter implements FileFilter, FilenameFilter {

	public boolean accept(File pathName) {
		if (pathName.isDirectory()) {
			return true;
		} else {
			return pathName.getAbsolutePath().toLowerCase().endsWith("log");
		}
	}

	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith("log");
	}

	@Activate
	public void activate() {

		System.out.println("\ncreate a file log filter ...\n");

	}

	@Deactivate
	public void deactivate() {
		System.out.println("stoping a file log filter ... \n");
	}
}
