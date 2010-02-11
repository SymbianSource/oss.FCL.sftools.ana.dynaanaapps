/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/

package com.nokia.s60tools.crashanalyser.model;

import java.io.*;
import java.util.zip.*;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;

/**
 * This class provides common static methods that can be used by
 * other classes. This class provides mainly methods for file 
 * handling, such as deleteFolder, copyFile, etc.
 */
public final class FileOperations {
	private FileOperations() {
		// not meant to be implemented
	}
	
	/**
	 * If the last character of the given path is not backslash, it is added
	 * and path with backslash is returned.
	 * @param path Path to which backslash is added
	 * @return Path which last character is backslash
	 */
	public static String addSlashToEnd(String path) {
		if (path.endsWith(File.separator))
			return path;
		else
			return path + File.separator;
	}
	
	/**
	 * Deletes given folder's contents and folder itself.
	 * @param folder Folder to be deleted
	 */
	public static void deleteFolder(String folder) {
		File f = new File(folder);
		if (f.isDirectory())
			deleteAllFiles(f);
	}
	
	/**
	 * Deletes given file
	 * @param path path to file to be deleted
	 */
	public static void deleteFile(String path) {
		File f = new File(path);
		if (f.isFile())
			f.delete();
	}
	
	/**
	 * Deletes all files from given directory and directory itself
	 * @param dir Directory to be deleted
	 * @return true if successful, false if not
	 */
	private static boolean deleteAllFiles(File dir) {
		if(!dir.exists()) {
			return true;
		}
		
		boolean res = true;
		if(dir.isDirectory()) {
			File[] files = dir.listFiles();
			for(int i = 0; i < files.length; i++) {
				res &= deleteAllFiles(files[i]);
			}
			res = dir.delete();
			//Delete dir itself
		} else {
			res = dir.delete();
		}
		return res;
	}
	
	/**
	 * Copies given file to destination.
	 * @param file File to be copied
	 * @param destinationFile Where file is copied
	 * @param overwrite Defines whether destinationFile is overwritten is if exists already
	 * @return true is copy was executed, false if not
	 */
	public static boolean copyFile(File file, File destinationFile, boolean overwrite) {
		if (destinationFile.exists()) {
			if (overwrite) {
				destinationFile.delete();
			} else {
				return false;
			}
		}
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
	        fis = new FileInputStream(file);
	        fos = new FileOutputStream(destinationFile);
	        byte[] buf = new byte[1024];
	        int i = 0;
	        while((i=fis.read(buf))!=-1) {
	        	fos.write(buf, 0, i);
	        }
	        fis.close();
	        fos.close();
	    } catch (Exception e) {
	    	try {
	    		if (fis != null) fis.close();
	    	} catch (Exception E) {
	    		e.printStackTrace();
	    	}
	    	try {
	    		if (fos != null) fos.close();
	    	} catch (Exception E) {
	    		E.printStackTrace();
	    	}
	        return false;
	    }		
		return true;
	}
	
	/**
	 * Copies given file to destination.
	 * @param file File to be copied
	 * @param destinationPath Where file is copied
	 * @param overwrite Defines whether destinationPath is overwritten is if exists already
	 * @return true is copy was executed, false if not
	 */
	public static boolean copyFile(File file, String destinationPath, boolean overwrite) {
		File newFile = new File(FileOperations.addSlashToEnd(destinationPath) + file.getName());
		return copyFile(file, newFile, overwrite);
	}
	
	/**
	 * Creates a folder
	 * @param path folder to be created
	 * @return true if folder was created or existed, false if not
	 */
	public static boolean createFolder(String path) {
		try {
			File file = new File(path); 
			if (file.exists() && file.isDirectory())
				return true;
			return file.mkdir();
		} catch (Exception e) {
			return false;
		}
	}	
	
	/**
	 * Creates a folder
	 * @param path folder to be created
	 * @param override clears the folder if it exists
	 * @return true if folder was created or existed, false if not
	 */
	public static boolean createFolder(String path, boolean override) {
		try {
			File file = new File(path); 
			if (file.exists() && file.isDirectory()) {
				if (!override) {
					return true;
				}
				FileOperations.deleteAllFiles(new File(path));
			}
			return file.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	
	/**
	 * Returns file name with extension
	 * @param filePath file path e.g. c:\mypath\myfile.txt
	 * @return e.g myfile.txt when filePath is c:\mypath\myfile.txt
	 */
	public static String getFileNameWithExtension(String filePath) {
		File tmpFile = new File(filePath);
		if (tmpFile.isFile()) {
			return tmpFile.getName();
		}
		
		return "";
	}

	/**
	 * Returns file name without extesion
	 * @param fileName e.g. my_file.txt
	 * @return e.g. my_file when fileName is my_file.txt
	 */
	public static String getFileNameWithoutExtension(String fileName) {
		File tmpFile = new File(fileName);
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2 ) {
			return tmpFile.getName().substring(0, whereDot);
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns folder from full file path
	 * @param fullFilePath e.g. c:\folder\file.txt
	 * @return e.g. c:\folder\ when fullFilePath is c:\folder\file.txt
	 */
	public static String getFolder(String fullFilePath) {
		try {
			int index = fullFilePath.lastIndexOf(File.separator);
			return fullFilePath.substring(0, index);
		} catch (Exception e) {
			return "";
		}
	}
	
	
	
	/**
	 * Unzips all files from given zip file to given outputFolder.
	 * @param zipFile zip file
	 * @param outputFolder output folder
	 */
	public static void unZipFiles(File zipFile, String outputFolder) {
		try {
			String output = addSlashToEnd(outputFolder);
			final int BUFFER = 2048;
			BufferedOutputStream dest = null;
			FileInputStream fis = new FileInputStream(zipFile);
			ZipInputStream zis = new 
			ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry;
			while((entry = zis.getNextEntry()) != null) {
				int count;
				byte data[] = new byte[BUFFER];
	            // write the files to the disk
				FileOutputStream fos = new FileOutputStream(output + entry.getName());
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
			zis.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Zips given files to given zip file
	 * @param filenames files to be zipped
	 * @param outFilename zip file path
	 */
	public static void zipFiles(String[] filenames, String outFilename) {
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		try {
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
			
			// Compress the files
			for (int i=0; i < filenames.length; i++) {
				String filename = filenames[i];
				
				FileInputStream in = new FileInputStream(filename);
				File f = new File(filename);
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(f.getName()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}

			// Complete the ZIP file
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}	

	/**
	 * Shows save file dialog
	 * @param header header text for the dialog
	 * @param filter filter of the file (e.g. "*.zip")
	 * @param shell shell for the dialog
	 * @return file save path or null if cancelled
	 */
	public static String saveAsDialog(String header, String[] filter, Shell shell) {
		// We store the selected file name in fileName
		String fileName = null;

		// The user has finished when one of the
		// following happens:
		// 1) The user dismisses the dialog by pressing Cancel
		// 2) The selected file name does not exist
		// 3) The user agrees to overwrite existing file
		boolean done = false;
		
		FileDialog dlg = new FileDialog(shell, SWT.SAVE);
		dlg.setText(header);
		dlg.setFilterExtensions(filter);

		while (!done) {
			// Open the File Dialog
			fileName = dlg.open();
			if (fileName == null) {
				// User has cancelled, so quit and return
				done = true;
			} else {
				// User has selected a file; see if it already exists
				File file = new File(fileName);
				if (file.exists()) {
					// The file already exists; asks for confirmation
					MessageBox mb = new MessageBox(dlg.getParent(), SWT.ICON_WARNING
		              | SWT.YES | SWT.NO);

					mb.setMessage(fileName + " already exists. Do you want to replace it?");

					// If they click Yes, we're done and we drop out. If
					// they click No, we redisplay the File Dialog
					done = mb.open() == SWT.YES;
				} else {
					// File does not exist, so drop out
					done = true;
				}
			}
		}
		return fileName;
	}		
}
