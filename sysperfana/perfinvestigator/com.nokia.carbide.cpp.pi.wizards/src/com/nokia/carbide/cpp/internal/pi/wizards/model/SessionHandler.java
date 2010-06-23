/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
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
package com.nokia.carbide.cpp.internal.pi.wizards.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import com.nokia.carbide.cpp.pi.PiPlugin;
import com.nokia.carbide.cpp.pi.wizards.WizardsPlugin;

public final class SessionHandler {

	private static final String SESSION_DATA_FILE_NAME = "PerformanceInvestigatorSession.bin"; //$NON-NLS-1$
	private static final IPath SESSION_DATA_FILE_PATH = PiPlugin.getDefault()
			.getStateLocation().append(SESSION_DATA_FILE_NAME);
	private static SessionHandler instance;

	public static SessionHandler getInstance() {
		if (instance == null) {
			instance = new SessionHandler();
		}
		return instance;
	}

	private SessionHandler() {

	}

	/**
	 * Saves given trace files.
	 * 
	 * @param files
	 * @return <code>true</code> if trace files are saved otherwise
	 *         <code>false</code> is returned
	 */
	public boolean saveTraceFiles(List<TraceFile> files) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(SESSION_DATA_FILE_PATH.toFile());
			out = new ObjectOutputStream(fos);
			out.writeObject(files);
			out.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Load trace files from storage
	 * 
	 * @return array of the trace files
	 */
	@SuppressWarnings("unchecked")
	public TraceFile[] loadTraceFile() {
		File file = SESSION_DATA_FILE_PATH.toFile();
		if (!file.exists()) {
			return new TraceFile[0];
		}
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			List<TraceFile> traceFiles = (List<TraceFile>) in.readObject();
			return traceFiles.toArray(new TraceFile[0]);
		} catch (Exception e) {
			return new TraceFile[0];
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Removes given file.
	 * 
	 * @param traceFile
	 *            file to be removed
	 */
	public void removeTraceFile(TraceFile traceFile) {
		if (traceFile == null) {
			return;
		}
		List<TraceFile> traceFiles = new ArrayList<TraceFile>();
		traceFiles.addAll(Arrays.asList(loadTraceFile()));
		boolean removed = traceFiles.remove(traceFile);
		if (removed) {
			WizardsPlugin.getDefault().getPreferenceStore().setValue(
					traceFile.getTraceFilePath().toString(), false);
			saveTraceFiles(traceFiles);
		}
	}
	
	/**
	 * Get TraceFile with given path.
	 * 
	 * @param path
	 * @return instance of the TraceFile if instance is found with given path
	 *         otherwise null is returned
	 */
	public TraceFile getTraceFile(IPath path) {
		if (path == null) {
			return null;
		}
		List<TraceFile> traceFiles = new ArrayList<TraceFile>();
		traceFiles.addAll(Arrays.asList(loadTraceFile()));
		for (TraceFile traceFile : traceFiles) {
			if (path.equals(traceFile.getTraceFilePath())) {
				return traceFile;
			}

		}
		return null;
	}
	
	/**
	 * Add given file.
	 * 
	 * @param traceFile
	 *            file to be added
	 */
	public void addTraceFile(TraceFile traceFile) {
		List<TraceFile> traceFileList = new ArrayList<TraceFile>();
		traceFileList.addAll(Arrays.asList(loadTraceFile()));
		traceFileList.add(traceFile);
		saveTraceFiles(traceFileList);
	}
}
