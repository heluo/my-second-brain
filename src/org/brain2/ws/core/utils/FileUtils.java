package org.brain2.ws.core.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Work with files in file system (delete, get size, etc...).
 * 
 * @author
 * 
 */
public class FileUtils {

	public static String getRuntimeFolderPath()  {
		try {
			File dir1 = new File(".");
			return dir1.getCanonicalPath();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Calls writeToFile with createDir flag false.
	 * 
	 * @see writeToFile(String fileName, InputStream iStream, boolean createDir)
	 * 
	 * @created 2002-05-02 by Alexander Feldman
	 * 
	 */
	public static void writeToFile(String fileName, InputStream iStream)
			throws IOException {
		writeToFile(fileName, iStream, false);
	}

	/**
	 * Writes InputStream to a given <code>fileName<code>.
	 * And, if directory for this file does not exists,
	 * if createDir is true, creates it, otherwice throws OMDIOexception.
	 * 
	 * @param fileName
	 *            - filename save to.
	 * @param iStream
	 *            - InputStream with data to read from.
	 * @param createDir
	 *            (false by default)
	 * @throws IOException
	 *             in case of any error.
	 * 
	 * @refactored 2002-05-02 by Alexander Feldman - moved from OMDBlob.
	 * 
	 */
	public static void writeToFile(String fileName, InputStream iStream,
			boolean createDir) throws IOException {
		String me = "FileUtils.WriteToFile";
		if (fileName == null) {
			throw new IOException(me + ": filename is null");
		}
		if (iStream == null) {
			throw new IOException(me + ": InputStream is null");
		}

		File theFile = new File(fileName);

		// Check if a file exists.
		if (theFile.exists()) {
			String msg = theFile.isDirectory() ? "directory" : (!theFile
					.canWrite() ? "not writable" : null);
			if (msg != null) {
				throw new IOException(me + ": file '" + fileName + "' is "
						+ msg);
			}
		}

		// Create directory for the file, if requested.
		if (createDir && theFile.getParentFile() != null) {
			theFile.getParentFile().mkdirs();
		}

		// Save InputStream to the file.
		BufferedOutputStream fOut = null;
		try {
			fOut = new BufferedOutputStream(new FileOutputStream(theFile));
			byte[] buffer = new byte[32 * 1024];
			int bytesRead = 0;
			while ((bytesRead = iStream.read(buffer)) != -1) {
				fOut.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			throw new IOException(me + " failed, got: " + e.toString());
		} finally {
			close(iStream, fOut);
		}
	}
	
	public static String readFileAsString(String filePath) throws java.io.IOException {
		String fullpath = FileUtils.getRuntimeFolderPath() + filePath;		
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(fullpath));
		char[] buf = new char[2048];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}
	
	public static File[]  listFilesInForder(String folderPath) throws java.io.IOException {
		String fullpath = FileUtils.getRuntimeFolderPath() + folderPath;		
		File folder = new File(fullpath);
		if(folder.isDirectory())
			return folder.listFiles();
		return new File[0];
	}
	
	public static DataInputStream readFileAsStream(String filePath)  {
		try {
			String fullpath = FileUtils.getRuntimeFolderPath() + filePath;		
			DataInputStream  stream = new DataInputStream(new FileInputStream(fullpath));		
			return stream;
		} catch (IOException e) {
			Log.println("file " + filePath + " is not found!");
		}
		return null;
	}
	
	public static String loadFilePathToString(String fullPath) throws java.io.IOException {		
		
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(fullPath));
		char[] buf = new char[2048];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * Closes InputStream and/or OutputStream. It makes sure that both streams
	 * tried to be closed, even first throws an exception.
	 * 
	 * @throw IOException if stream (is not null and) cannot be closed.
	 * 
	 */
	protected static void close(InputStream iStream, OutputStream oStream)
			throws IOException {
		try {
			if (iStream != null) {
				iStream.close();
			}
		} finally {
			if (oStream != null) {
				oStream.close();
			}
		}
	}
	
	public static boolean writeStringToFile(String fullPath, String content){		
		try {
			Writer output = null;
			File file = new File(fullPath);
			if (!file.exists()) {
				file.createNewFile();
			}
			output = new BufferedWriter(new FileWriter(file));
			output.write(content);
			output.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;		
	}
}
