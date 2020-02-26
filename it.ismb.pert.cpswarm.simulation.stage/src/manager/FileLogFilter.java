package manager;

import java.io.File;
import java.io.FilenameFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

// create only one shared instance per runtime.

@Component(immediate=true,
	service = {FileLogFilter.class}
)
public class FileLogFilter implements FilenameFilter {

	public boolean accept(File dir, String name) {
		
		if(name.toLowerCase().endsWith("bag")) {
			String[] fields = name.split("_");
			if(fields[3].equals("worker"))
				return true;
		}
		return false;
	}

	@Deactivate
	public void deactivate() {
		System.out.println("stoping a file log filter ... \n");
	}
}

