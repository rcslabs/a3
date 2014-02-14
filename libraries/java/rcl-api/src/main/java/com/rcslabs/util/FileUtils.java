package com.rcslabs.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Utilities for file system.
 *
 */
public class FileUtils {

	private FileUtils() {}
	
	private static class SingleFileFilter implements FileFilter {
		private final String filename;

		public SingleFileFilter(String filename) {
			if(filename.startsWith("/")) { //cut leading slash
				this.filename = filename.substring(1);
			}
			else {
				this.filename = filename;
			}
		}
		
		@Override
		public boolean accept(File f) {
			return f.getName().equals(filename) && f.canRead() && f.isFile();
		}
		
		public String getFilename() {
			return filename;
		}
	}
	
	private static class SingleDirectoryFilter extends SingleFileFilter {
		public SingleDirectoryFilter(String filename) {
			super(filename);
		}
		
		@Override
		public boolean accept(File f) {
			return f.getName().equals(getFilename()) && f.canRead() && f.isDirectory();
		}
	}
	
	/**
	 * Asserts that the specified path is an existing readable directory.
	 * @param path a path to check
	 * @return a File object corresponding to this path if assertion is valid
	 * @throws IllegalStateException if assertion fails
	 */
	public static File assertReadableDirectory(String path) {
		File f = new File(path);
		if(!f.exists()) {
			throw new IllegalStateException(
					"Directory " + path + " not found."
			);
		}
		if(!f.isDirectory()) {
			throw new IllegalStateException(
					path + " is not a directory."
			);
		}
		if(!f.canRead()) {
			throw new IllegalStateException(
					"Directory is not readable."
			);
		}
		
		return f;
	}
	
	/**
	 * Returns a readable file with name filename from directory dir, if it exists.
	 * @param dir a search dir
	 * @param filename a search file name
	 * @param required if true, throws {@link IllegalStateException} in case the file does not exists
	 * @return a File object, corresponding to a target file, or null if it does not exist.
	 * @throws IllegalStateException if required == true and the file does not exist
	 */
	public static File getReadableFile(File dir, final String filename, boolean required) {
		if(required) {
			assertReadableDirectory(dir.getAbsolutePath());
		}
		return getFile(dir, new SingleFileFilter(filename), required);
	}

	/**
	 * Returns a readable directory with name filename from directory dir, if it exists.
	 * @param dir a search dir
	 * @param filename a search directory name
	 * @param required if true, throws {@link IllegalStateException} in case the directory does not exists
	 * @return a File object, corresponding to a target file, or null if it does not exist.
	 * @throws IllegalStateException if required == true and the directory does not exist
	 */
	public static File getReadableDirectory(File dir, final String filename, boolean required) {
		if(required) {
			assertReadableDirectory(dir.getAbsolutePath());
		}
		return getFile(dir, new SingleDirectoryFilter(filename), required);
	}
	
	/**
	 * Returns a file name without extension.
	 * 
	 * I.e. for file name "Readme.txt" the result will be "Readme".
	 * For file name "ChangeLog" the result will be "ChangeLog".
	 * 
	 */
	public static String getNameWithoutExtension(File file) {
		int endInd = file.getName().lastIndexOf(".");
		if(endInd != -1) {
			return file.getName().substring(0, endInd);
		}
		else return file.getName();
	}
	
	private static File getFile(File dir, SingleFileFilter filter, boolean required) {
		File[] files = dir.listFiles(filter);
		if(required && files.length == 0) 
			throw new IllegalStateException("A required item " + filter.getFilename() +  " not found in the directory " + dir.getAbsolutePath());
		
		return files.length > 0 ? files[0] : null;
	}
}
