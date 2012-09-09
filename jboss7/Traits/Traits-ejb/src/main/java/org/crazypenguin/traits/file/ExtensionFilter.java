package org.crazypenguin.traits.file;

import java.io.File;
import java.io.FilenameFilter;

public class ExtensionFilter implements FilenameFilter {

	private final String ext; 
	
	public ExtensionFilter(final String ext) {
		this.ext = ext.startsWith(".") ? ext : "." + ext;
	}
	
	@Override
	public boolean accept(final File dir, final String name) {
		return name.endsWith(ext);
	}

}
