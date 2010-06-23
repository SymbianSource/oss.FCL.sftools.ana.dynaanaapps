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
* Property file management engine
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import java.io.File;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.nokia.tracebuilder.engine.TraceBuilderEngine;
import com.nokia.tracebuilder.engine.TraceBuilderErrorMessages;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceBuilderErrorCodes.TraceBuilderErrorCode;
import com.nokia.tracebuilder.engine.project.ProjectEngine;
import com.nokia.tracebuilder.file.FileUtils;
import com.nokia.tracebuilder.model.TraceBuilderException;
import com.nokia.tracebuilder.model.TraceConstantTable;
import com.nokia.tracebuilder.model.TraceConstantTableEntry;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.model.TraceProcessingListener;
import com.nokia.tracebuilder.project.ProjectUtils;
import com.nokia.tracebuilder.source.SourceConstants;

/**
 * Property file management engine
 * 
 */
public final class PropertyFileEngine extends TraceBuilderEngine implements
		TraceModelListener, TraceProcessingListener {

	/**
	 * Trace model
	 */
	private TraceModel model;

	/**
	 * Model processing flag prevents unnecessary saves
	 */
	private boolean processing;

	/**
	 * Backup created flag. This is initially set to false and when backup is
	 * created, changes to true
	 */
	private boolean backupCreated;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the trace model
	 */
	public PropertyFileEngine(TraceModel model) {
		this.model = model;
		model.addModelListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#projectOpened()
	 */
	@Override
	public void projectOpened() {
		TracePropertyFile propertyFile = model
				.getExtension(TracePropertyFile.class);
		if (propertyFile == null) {
			String fileName = null;
			try {
				fileName = ProjectUtils.getLocationForFile(model,
						ProjectEngine.traceFolderName,
						PropertyFileConstants.PROPERTY_FILE_NAME, false);
			} catch (TraceBuilderException e) {
				// Model should always be open when traceProjectOpened is
				// called
			}
			if (fileName != null) {
				propertyFile = parsePropertyFile(fileName);
				// Backup flag is reset when model is opened
				backupCreated = false;
			}
		}
		if (propertyFile == null) {
			String msg = Messages
					.getString("PropertyFileEngine.FailedToAttachFile"); //$NON-NLS-1$
			TraceBuilderGlobals.getEvents().postErrorMessage(msg, null, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#projectClosing()
	 */
	@Override
	public void projectClosed() {
		model.removeExtensions(TracePropertyFile.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceBuilderEngine#exportProject()
	 */
	@Override
	public void exportProject() {
	}

	/**
	 * Parses the property file
	 * 
	 * @param fileName
	 *            the file path
	 * @return the property file
	 */
	private TracePropertyFile parsePropertyFile(String fileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder;
		TracePropertyFile propertyFile = null;
		try {
			builder = factory.newDocumentBuilder();
			File file = new File(fileName);
			if (file.exists()) {
				try {
					PropertyFileParser parser = new PropertyFileParser(model,
							fileName, builder);
					parser.parse();
					propertyFile = new TracePropertyFile(file.getParent(),
							parser.getDocument());
					propertyFile.setProperties(parser.getGroupProperties(),
							parser.getTraceProperties());
				} catch (TraceBuilderException e) {
					// Problem parsing document -> Backup and create new
					TraceBuilderGlobals.getEvents().postError(e);
					createBackup(fileName);
				}
			}
			if (propertyFile == null) {
				propertyFile = new TracePropertyFile(file.getParent(), builder
						.newDocument());
			}
			model.addExtension(propertyFile);
		} catch (ParserConfigurationException e) {
		}
		return propertyFile;
	}

	/**
	 * Writes the property file
	 */
	private void writePropertyFile() {
		if (!processing) {
			TracePropertyFile propertyFile = model
					.getExtension(TracePropertyFile.class);
			if (propertyFile != null) {
				// Backup flag is checked. If not set, a backup is created and
				// the flag is set. The flag is reset when project is re-opened
				String path = propertyFile.getAbsolutePath();
				if (!backupCreated) {
					createBackup(path);
					backupCreated = true;
				}
				try {
					// Uses XML API to write the property file
					OutputStream fos = FileUtils.createOutputStream(new File(
							path));
					Transformer transformer = TransformerFactory.newInstance()
							.newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
					transformer.transform(new DOMSource(propertyFile
							.getDocument()), new StreamResult(fos));
					fos.close();
					propertyFile.postFileWrittenEvent(path);
				} catch (Exception e) {
					String msg = TraceBuilderErrorMessages.getErrorMessage(
							TraceBuilderErrorCode.CANNOT_WRITE_PROJECT_FILE,
							null);
					TraceBuilderGlobals.getEvents().postErrorMessage(msg, e, true);
				}
			}
		}
	}

	/**
	 * Creates a backup of the property file
	 * 
	 * @param path
	 *            the file to be backed up
	 */
	private void createBackup(String path) {
		boolean backup = false;
		File file = new File(path);
		if (file.exists()) {
			boolean allexist = true;
			// Checks the existing backup files and renames the old file to
			// largest available number starting from 0
			File f = null;
			for (int i = 0; i < PropertyFileConstants.BACKUP_COUNT && allexist; i++) {
				f = new File(path + i + PropertyFileConstants.BACKUP_EXTENSION);
				if (!f.exists()) {
					backup = FileUtils.copyFile(file, f);
					allexist = false;
				}
			}
			if (allexist) {
				// If all backups from 0 to 9 exist, the old ones are moved
				// back 1 step. The new file is renamed to *9.h
				for (int i = 0; i < PropertyFileConstants.BACKUP_COUNT; i++) {
					f = new File(path + i
							+ PropertyFileConstants.BACKUP_EXTENSION);
					if (i == 0) {
						f.delete();
					} else {
						f.renameTo(new File(path + (i - 1)
								+ PropertyFileConstants.BACKUP_EXTENSION));
					}
				}
				f = new File(path + PropertyFileConstants.LAST_BACKUP);
				backup = FileUtils.copyFile(file, f);
			}
			if (f != null && backup) {
				String msg = Messages
						.getString("PropertyFileEngine.PropertyFileBackUpPrefix") //$NON-NLS-1$
						+ FileUtils.convertSeparators(
								SourceConstants.FORWARD_SLASH_CHAR, f
										.getAbsolutePath(), false);
				TraceBuilderGlobals.getEvents().postInfoMessage(msg, null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectAdded(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectAdded(TraceObject owner, TraceObject object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectCreationComplete(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectCreationComplete(TraceObject object) {
		if (model.isValid()) {
			if (object instanceof TraceConstantTable
					|| object instanceof TraceConstantTableEntry) {
				TracePropertyFile propertyFile = model
						.getExtension(TracePropertyFile.class);
				if (propertyFile != null) {
					propertyFile.addElement(object);
					writePropertyFile();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectRemoved(TraceObject owner, TraceObject object) {
		if (model.isValid()) {
			if (object instanceof TraceConstantTable
					|| object instanceof TraceConstantTableEntry) {
				TracePropertyFile propertyFile = model
						.getExtension(TracePropertyFile.class);
				if (propertyFile != null) {
					propertyFile.removeElement(object);
					writePropertyFile();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      propertyUpdated(com.nokia.tracebuilder.model.TraceObject, int)
	 */
	public void propertyUpdated(TraceObject object, int property) {
		if (object.isComplete()
				&& (object instanceof TraceConstantTable || object instanceof TraceConstantTableEntry)) {
			TracePropertyFile propertyFile = model
					.getExtension(TracePropertyFile.class);
			if (propertyFile != null) {
				propertyFile.updateElement(object);
				writePropertyFile();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceProcessingListener#processingComplete(boolean)
	 */
	public void processingComplete(boolean changed) {
		processing = false;
		if (changed) {
			writePropertyFile();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceProcessingListener#processingStarted()
	 */
	public void processingStarted() {
		processing = true;
	}

}
