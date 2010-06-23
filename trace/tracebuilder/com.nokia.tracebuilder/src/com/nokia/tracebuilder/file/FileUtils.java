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
* File utility functions
*
*/
package com.nokia.tracebuilder.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.runtime.IPath;

import com.nokia.tracebuilder.source.SourceConstants;

/**
 * File utility functions
 * 
 */
public final class FileUtils {

	/**
	 * File copy buffer size
	 */
	private static final int COPY_BUFFER_SIZE = 4096; // CodForChk_Dis_Magic

	/**
	 * cpp extension
	 */
	public final static String CPP_EXTENSION = "cpp"; //$NON-NLS-1$	

	/**
	 * c extension
	 */
	public final static String C_EXTENSION = "c"; //$NON-NLS-1$	

	/**
	 * Allowed files
	 */
	private final static String[] FILE_FILTERS = { ".cpp", //$NON-NLS-1$
			".c", //$NON-NLS-1$
			".inl", //$NON-NLS-1$
			".h" //$NON-NLS-1$
	};

	/**
	 * MMP file extension
	 */
	public static final String MMP = ".mmp"; //$NON-NLS-1$

	/**
	 * Creates a file output stream. This creates directories and overwriting
	 * possible read-only flag
	 * 
	 * @param file
	 *            the file
	 * @return the file output stream
	 * @throws FileNotFoundException
	 *             if file cannot be created
	 */
	public static OutputStream createOutputStream(File file)
			throws FileNotFoundException {
		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
		} catch (IOException e) {
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);
		}
		return new BufferedOutputStream(fos);
	}

	/**
	 * Creates a copy of a file
	 * 
	 * @param source
	 *            the source file
	 * @param target
	 *            the target file
	 * @return true if written successfully
	 */
	public static boolean copyFile(File source, File target) {
		boolean backup = true;
		try {
			byte[] buf = new byte[COPY_BUFFER_SIZE];
			FileInputStream fis = new FileInputStream(source);
			OutputStream fos = createOutputStream(target);
			int len;
			do {
				len = fis.read(buf);
				if (len > 0) {
					fos.write(buf, 0, len);
				}
			} while (len > 0);
			fis.close();
			fos.close();
		} catch (Exception e) {
			backup = false;
		}
		return backup;
	}

	/**
	 * Converts file separator characters
	 * 
	 * @param separator
	 *            separator to be used
	 * @param path
	 *            string to be converted
	 * @param addLast
	 *            true if the converted string should end with a separator
	 * @return the converted string
	 */
	public static String convertSeparators(char separator, String path,
			boolean addLast) {
		path = path.replace(SourceConstants.FORWARD_SLASH_CHAR, separator);
		path = path.replace(SourceConstants.BACKSLASH_CHAR, separator);
		String sepStr = String.valueOf(separator);
		if (addLast && !path.endsWith(sepStr)) {
			path += separator;
		} else if (!addLast && path.endsWith(sepStr)) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	/**
	 * Gets the relative path from source to target
	 * 
	 * @param source
	 *            the source file
	 * @param target
	 *            the target file
	 * @return the relative path without file name
	 */
	public static String getRelativePath(String source, String target) {
		try {
			source = new File(source).getCanonicalPath();
			target = new File(target).getCanonicalPath();
		} catch (IOException e) {
		}
		source = convertSeparators(SourceConstants.FORWARD_SLASH_CHAR, source,
				false);
		target = convertSeparators(SourceConstants.FORWARD_SLASH_CHAR, target,
				false);
		int len = Math.min(source.length(), target.length());
		int start = -1;
		for (int i = 0; i < len && start == -1; i++) {
			if (source.charAt(i) != target.charAt(i)) {
				start = i;
			}
		}
		String retval;
		if (start == -1) {
			retval = getRelativePathSub(source, target);
		} else {
			retval = getRelativePathUnrelated(source, target, start);
		}
		return retval;
	}

	/**
	 * Gets the relative path from source to target when either one is a
	 * sub-directory of another
	 * 
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the relative path
	 */
	private static String getRelativePathSub(String source, String target) {
		StringBuffer sb = new StringBuffer();
		if (source.length() > target.length()) {
			// Source is in sub-directory of target
			for (int i = target.length(); i < source.length(); i++) {
				if (source.charAt(i) == SourceConstants.FORWARD_SLASH_CHAR) {
					sb.append(SourceConstants.PATH_UP);
				}
			}
		} else if (target.length() > source.length()) {
			// Target is in sub-directory of source
			int lastIndex;
			if (source.endsWith(String
					.valueOf(SourceConstants.FORWARD_SLASH_CHAR))) {
				lastIndex = source.length();
			} else {
				lastIndex = source.length() + 1;
			}
			for (int i = source.length(); i < target.length(); i++) {
				if (target.charAt(i) == SourceConstants.FORWARD_SLASH_CHAR) {
					sb.append(target.substring(lastIndex, i + 1));
					lastIndex = i + 1;
				}
			}
			if (new File(target).isDirectory() && lastIndex < target.length()) {
				sb.append(target.substring(lastIndex));
			}
		} else {
			// Paths were equal
			sb.append(SourceConstants.THIS_PATH);
		}
		return sb.toString();
	}

	/**
	 * Gets the relative path between two unrelated directories
	 * 
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param start
	 *            the start index where the paths differ
	 * @return the relative path
	 */
	private static String getRelativePathUnrelated(String source,
			String target, int start) {
		String retval;
		StringBuffer sb = new StringBuffer();
		File f = new File(target);
		// If target is absolute path and there is nothing in common between the
		// paths, the target absolute path is returned as such
		if (f.isAbsolute() && start == 0) {
			int lastIndex = target
					.lastIndexOf(SourceConstants.FORWARD_SLASH_CHAR) + 1;
			sb.append(target.substring(0, lastIndex));
		} else {
			for (int i = start; i < source.length(); i++) {
				if (source.charAt(i) == SourceConstants.FORWARD_SLASH_CHAR) {
					sb.append(SourceConstants.PATH_UP);
				}
			}
			int lastIndex;
			// The previous directory separator is used as the root between the
			// two paths.
			lastIndex = target.lastIndexOf(SourceConstants.FORWARD_SLASH_CHAR,
					start - 1) + 1;
			for (int i = start; i < target.length(); i++) {
				if (target.charAt(i) == SourceConstants.FORWARD_SLASH_CHAR) {
					sb.append(target.substring(lastIndex, i + 1));
					lastIndex = i + 1;
				}
			}
		}
		retval = sb.toString();
		return retval;
	}

	/**
	 * Checks if given file is allowed to be opened into TraceBuilder
	 * 
	 * @param fileName
	 *            the file to be checked
	 * @return true if filtered, false if not
	 */
	public static boolean isFileAllowed(String fileName) {
		boolean allowed = false;
		fileName = fileName.toLowerCase();
		for (String filter : FILE_FILTERS) {
			if (fileName.endsWith(filter)) {
				allowed = true;
				break;
			}
		}
		return allowed;
	}

	/**
	 * Creates URI from given IPath instance.
	 * @param path
	 * @return URI from given IPath instance
	 */
	public static URI makeURI(IPath path) {
		File file = path.toFile();
		return file.toURI();
	}	
}
