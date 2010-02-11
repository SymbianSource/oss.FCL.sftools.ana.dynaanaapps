/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.internal.pi.wizards.ui.util;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;

// keep the tree like a content provider because we have a branch of specialized node
// of workspace tree , tree access using content provider give us an edge of constitent 
// interface for walking the tree
public class PkgListTree {
	private ArrayList<Object> root = new ArrayList<Object>();
	
	public PkgListTree() {
		setupWorkspaceRoot();
	}

	private void setupWorkspaceRoot() {
		root.add(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	public Object[] getRoot() {
		return root.toArray();
	}
	
	public void removeAllPkgEntries() {
		root.clear();
		setupWorkspaceRoot();
	}
	
	public void addPkgEntry (IPkgEntry entry) {
		root.add(entry);
	}
	
	public void removePkgEntry (IPkgEntry entry) {
		root.remove(entry);
	}
	
	public IPkgEntry[] getPkgEntries () {
		ArrayList<IPkgEntry> pkgEntryArrayList = new ArrayList<IPkgEntry>();
		Iterator<Object> rootItr = root.iterator();
		while(rootItr.hasNext()) {
			Object current = rootItr.next();
			if (current instanceof IPkgEntry) {
				pkgEntryArrayList.add((IPkgEntry) current);
			}
		}
		return pkgEntryArrayList.toArray(new IPkgEntry[pkgEntryArrayList.size()]);
	}

	public void dispose() {
		root.clear();
	}
	
	public static boolean isBuildConfigurationItem(Object element) {
		if (element instanceof ICarbideBuildConfiguration)
			return true;
		return false;
	}

	public static boolean isFileItem(Object element) {
		if (element instanceof IPkgEntry)
			return true;
		return false;
	}

	public static boolean isProjectItem(Object element) {
		if (element instanceof IProject)
			return true;
		return false;
	}
}
