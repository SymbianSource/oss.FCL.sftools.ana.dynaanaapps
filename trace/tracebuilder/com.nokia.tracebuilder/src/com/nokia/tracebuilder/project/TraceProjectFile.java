/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Generic trace project file properties
*
*/
package com.nokia.tracebuilder.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.nokia.tracebuilder.engine.TraceBuilderConfiguration;
import com.nokia.tracebuilder.engine.TraceBuilderEvents;
import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.TraceViewExtension;
import com.nokia.tracebuilder.file.FileUtils;
import com.nokia.tracebuilder.model.TraceModel;
import com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener;
import com.nokia.tracebuilder.model.TraceModelListener;
import com.nokia.tracebuilder.model.TraceModelUpdatableExtension;
import com.nokia.tracebuilder.model.TraceObject;
import com.nokia.tracebuilder.source.SourceConstants;

/**
 * Generic trace project file properties
 * 
 */
public abstract class TraceProjectFile implements TraceModelUpdatableExtension,
		TraceViewExtension, TraceModelListener {

	/**
	 * The trace model
	 */
	private TraceModel owner;

	/**
	 * Name of the project file
	 */
	private String name;

	/**
	 * Project file path
	 */
	private String path;

	/**
	 * Update listeners
	 */
	private ArrayList<TraceModelExtensionUpdateListener> listeners;

	/**
	 * View reference
	 */
	private Object viewReference;

	/**
	 * Project file name is based on model name and updated when model changes
	 */
	protected boolean hasModelName;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            the path to the file
	 * @param name
	 *            the name of the project or empty if this file does not use the
	 *            project name
	 */
	protected TraceProjectFile(String path, String name) {
		this.path = path;
		this.name = name;
		if (name == null || name.length() == 0) {
			this.hasModelName = false;
		} else {
			this.hasModelName = true;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param absolutePath
	 *            the absolute path to the file
	 * @param hasModelName
	 *            flag, which tells to update the project file name if model
	 *            name changes
	 */
	protected TraceProjectFile(String absolutePath, boolean hasModelName) {
		this.hasModelName = hasModelName;
		updatePath(absolutePath);
	}

	/**
	 * Gets the file extension of this project file
	 * 
	 * @return the extension
	 */
	protected abstract String getFileExtension();

	/**
	 * Gets the title to be shown in UI
	 * 
	 * @return the title
	 */
	public abstract String getTitle();

	/**
	 * Gets the name of the project
	 * 
	 * @return the project name
	 */
	public final String getProjectName() {
		return name;
	}

	/**
	 * Gets the path of this project file
	 * 
	 * @return the project file path
	 */
	public final String getPath() {
		return path;
	}

	/**
	 * Gets the name of this project file
	 * 
	 * @return the file name
	 */
	public final String getFileName() {
		StringBuffer sb = new StringBuffer();
		addFileName(sb, false);
		return sb.toString();
	}

	/**
	 * Checks if this file is valid
	 * 
	 * @return true if valid, false if not
	 */
	public boolean isValid() {
		return path != null && name != null;
	}

	/**
	 * Model update notification. This renames the project file according to the
	 * new model name
	 */
	protected void modelChanged() {
		if (hasModelName && !getOwner().getName().equals(this.name)) {
			File oldFile = new File(getAbsolutePath());
			this.name = getOwner().getName();
			File newFile = new File(getAbsolutePath());
			boolean renamed = oldFile.renameTo(newFile);
			if (renamed) {
				String prefix = Messages
						.getString("TraceProjectFile.ProjectFileRenamedPrefix"); //$NON-NLS-1$
				String middle = Messages
						.getString("TraceProjectFile.ProjectFileRenamedMiddle"); //$NON-NLS-1$
				TraceBuilderGlobals.getEvents().postInfoMessage(
						getTitle() + prefix + oldFile.getName() + middle
								+ newFile.getName(), null);
			}
			notifyUpdate();
		}
	}

	/**
	 * Notifies the listeners that this file has changed
	 */
	private void notifyUpdate() {
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).extensionUpdated();
			}
		}
	}

	/**
	 * Posts a project file written event
	 * 
	 * @param path
	 *            the path where file was written
	 */
	public void postFileWrittenEvent(String path) {
		TraceBuilderEvents events = TraceBuilderGlobals.getEvents();
		String oldCategory = events
				.setEventCategory(TraceBuilderEvents.VERBOSE_INFO);
		String msg = Messages
				.getString("TraceProjectFile.ProjectFileWrittenMiddle"); //$NON-NLS-1$
		TraceBuilderGlobals.getEvents().postInfoMessage(
				getTitle() + msg + path, null);
		events.setEventCategory(oldCategory);
	}

	/**
	 * Updates the project file name and path
	 * 
	 * @param absolutePath
	 *            the new path including the file name
	 */
	public void updatePath(String absolutePath) {
		File file = new File(absolutePath);
		path = file.getParent();
		name = file.getName();
		String ext = getFileExtension();
		if (name.endsWith(ext)) {
			name = name.substring(0, name.length() - ext.length());
		}
		notifyUpdate();
	}

	/**
	 * Gets the path including file name
	 * 
	 * @return the path
	 */
	public final String getAbsolutePath() {
		String retval;
		if (isValid()) {
			StringBuffer sb = new StringBuffer();
			sb.append(FileUtils.convertSeparators(
					SourceConstants.FORWARD_SLASH_CHAR, path, true));
			addFileName(sb, false);
			retval = sb.toString();
		} else {
			retval = null;
		}
		return retval;
	}

	/**
	 * Gets the absolute path to this file, including the model ID in file name
	 * 
	 * @return the path
	 */
	public final String getAbsolutePathWithID() {
		String retval;
		if (isValid()) {
			StringBuffer sb = new StringBuffer();
			sb.append(FileUtils.convertSeparators(
					SourceConstants.FORWARD_SLASH_CHAR, path, true));
			addFileName(sb, true);
			retval = sb.toString();
		} else {
			retval = null;
		}
		return retval;
	}

	/**
	 * Adds the file name to the given buffer
	 * 
	 * @param sb
	 *            the buffer
	 * @param addID
	 *            true if ID needs to be added to name
	 */
	private void addFileName(StringBuffer sb, boolean addID) {
		sb.append(name);
		if (addID) {
			sb.append("_0x"); //$NON-NLS-1$
			sb.append(Integer.toHexString(getOwner().getModel().getID()));
			sb.append("_"); //$NON-NLS-1$
		}
		sb.append(getFileExtension());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#getOwner()
	 */
	public TraceObject getOwner() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelExtension#
	 *      setOwner(com.nokia.tracebuilder.model.TraceObject)
	 */
	public void setOwner(TraceObject owner) {
		if (this.owner != null) {
			this.owner.removeModelListener(this);
		}
		if (owner instanceof TraceModel) {
			this.owner = (TraceModel) owner;
			this.owner.addModelListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#getChildren()
	 */
	public Iterator<?> getChildren() {
		List<Object> list = Collections.emptyList();
		return list.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#hasChildren()
	 */
	public boolean hasChildren() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#hideWhenEmpty()
	 */
	public boolean hideWhenEmpty() {
		return !TraceBuilderConfiguration.SHOW_PROJECT_FILES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelUpdatableExtension#
	 *      addUpdateListener(com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener)
	 */
	public void addUpdateListener(TraceModelExtensionUpdateListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<TraceModelExtensionUpdateListener>();
		}
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelUpdatableExtension#
	 *      removeUpdateListener(com.nokia.tracebuilder.model.TraceModelExtensionUpdateListener)
	 */
	public void removeUpdateListener(TraceModelExtensionUpdateListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#getViewReference()
	 */
	public Object getViewReference() {
		return viewReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.TraceViewExtension#
	 *      setViewReference(java.lang.Object)
	 */
	public void setViewReference(Object reference) {
		this.viewReference = reference;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      objectRemoved(com.nokia.tracebuilder.model.TraceObject,
	 *      com.nokia.tracebuilder.model.TraceObject)
	 */
	public void objectRemoved(TraceObject owner, TraceObject object) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.model.TraceModelListener#
	 *      propertyUpdated(com.nokia.tracebuilder.model.TraceObject, int)
	 */
	public void propertyUpdated(TraceObject object, int property) {
		// Model change notifications are processed
		if (object instanceof TraceModel) {
			modelChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer retval = new StringBuffer();
		retval.append(getTitle());
		retval.append(": "); //$NON-NLS-1$
		if (isValid()) {
			retval.append(name);
			retval.append(getFileExtension());
			retval.append(" ("); //$NON-NLS-1$
			retval.append(path);
			retval.append(")"); //$NON-NLS-1$
		} else {
			retval.append(Messages.getString("TraceProjectFile.NotValid")); //$NON-NLS-1$
		}
		return retval.toString();
	}

}
