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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.model.WorkbenchContentProvider;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;

public class PkgListTreeContentProvider implements ITreeContentProvider {
	PkgListTree tree;
	// need to work around CarbideBuildConfiguration equal() that claims
	// configuration objects are equal even when their projects are different
	Map<IProject, ArrayList<Object>> configMap = new HashMap<IProject, ArrayList<Object>>();
	WorkbenchContentProvider cp = new WorkbenchContentProvider();
	
	public PkgListTreeContentProvider(PkgListTree myTree) {
		tree = myTree;
	}
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IPkgEntry) {
			return null;
		} else if (parentElement instanceof IViewSite) {
			return getCarbideCppProjects(false);
		} else if (parentElement instanceof IWorkspaceRoot) {
			return getCarbideCppProjects(false);
		} else if (parentElement instanceof IProject) {
			return getConfigs((IProject)parentElement).toArray();
		} else if (parentElement instanceof PkgListTree) {
			return getElements(parentElement);
		}
		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof ICarbideBuildConfiguration) {
			return ((ICarbideBuildConfiguration)element).getCarbideProject().getProject();
		}
		return cp.getParent(element);
	}

	public boolean hasChildren(Object element) {
		if (element instanceof IPkgEntry) {
			return false;
		}else if (element instanceof ICarbideBuildConfiguration) {
			return false;
		}else if (element instanceof IProject) {
			return true;
		}else if (element instanceof PkgListTree) {
			return true;
		}
		
		return cp.hasChildren(element);
	}

	public Object[] getElements(Object inputElement) {
		return tree.getRoot();
	}

	public void dispose() {
		cp.dispose();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		configMap.clear();
		cp.inputChanged(viewer, oldInput, newInput);
	}

	public Object[] getCarbideCppProjects(boolean logWarnings) {
		// lifted from com.nokia.carbide.cpp.project.ui.views.SPNViewContentProvider
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> list = new ArrayList<IProject>();
		for (int i = 0; i < projects.length; i++) {
			try {
				if (projects[i].isAccessible() && projects[i].hasNature(CarbideBuilderPlugin.CARBIDE_PROJECT_NATURE_ID)) {
					if (projects[i].isOpen()) {
						ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo(projects[i]);
						if (cpi == null)
							continue; // must have valid Carbide project info
						List<ICarbideBuildConfiguration> bc = cpi.getBuildConfigurations();
						boolean haveConfigWithPKG = false;
						// if this project have all configs with (none) PKG, don't show the project
						for (ICarbideBuildConfiguration config : bc) {
							if (config.getSISBuilderInfoList().size() > 0) {
								haveConfigWithPKG = true;
								break;
							}
						}
						if (haveConfigWithPKG == false){
							if(logWarnings){						
								GeneralMessages.PiLog(MessageFormat.format(Messages.getString("PkgListTreeContentProvider.warning.sisBuilderConfiguration.missing"),projects[i].getName()), GeneralMessages.WARNING); //$NON-NLS-1$
							}
							continue;	// must have buildconfig with good PKG to show up
						}							
						list.add(projects[i]);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return list.toArray();
	}
	
	private ArrayList<Object> getConfigs(IProject project) {
		ArrayList<Object> configs = configMap.get(project);
		if (configs == null) {
			configs = new ArrayList<Object>();
			configMap.put(project, configs);
			if (project.exists() == false || project.isOpen() == false)
				return configs;	// closed project do not have config
			ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo(project);
			if (cpi == null)
				return configs;
			List<ICarbideBuildConfiguration> bc = cpi.getBuildConfigurations();
			if (bc.size() < 1)
				return configs;
			for (ICarbideBuildConfiguration config : bc) {
				// exclude configuration with (none) PKG
				if (config.getSISBuilderInfoList().size() > 0) {
					configs.add(config);
				}
			}
		}
		return configs;
	}
}
