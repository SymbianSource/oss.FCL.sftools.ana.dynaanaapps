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

package com.nokia.carbide.cpp.pi.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class PIProjectNature implements IProjectNature {

	public static final String PI_NATURE_ID = PICorePlugin.PLUGIN_ID + ".pinature";  //$NON-NLS-1$
	
	private transient IProject fProject;
	
	public void configure() throws CoreException {
	}

	public void deconfigure() throws CoreException {
	}

	public PIProjectNature() {
	}
	
	public PIProjectNature(final IProject project) {
		setProject(project);
	}

	public IProject getProject() {
		return fProject;
	}

	public void setProject(final IProject project) {
		fProject = project;
	}

	public static void addPINature(final IProject project, final IProgressMonitor mon) throws CoreException {
		addNature(project, PI_NATURE_ID, mon);
	}

	public static void removePINature(final IProject project, final IProgressMonitor mon) throws CoreException {
		removeNature(project, PI_NATURE_ID, mon);
	}

	private static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		final IProjectDescription description = project.getDescription();
		final String[] prevNatures = description.getNatureIds();
		for (String prevNature : prevNatures) {
			if (natureId.equals(prevNature)) {
				return;
			}
		}
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	private static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		final IProjectDescription description = project.getDescription();
		final String[] prevNatures = description.getNatureIds();
		final List<String> newNatures = new ArrayList<String>(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds((String[]) newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

}
