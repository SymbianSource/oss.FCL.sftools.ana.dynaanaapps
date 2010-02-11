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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cdt.builder.project.ISISBuilderInfo;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;

public class PkgListTreeViewer extends TreeViewer {
	PkgListTreeContentProvider contentProvider;
	
	private class PkgListProjectItem extends PkgListBaseCustomItem {

		PkgListProjectItem(PkgListTreeViewer myViewer, Object myElement,
				TreeItem myTreeItem) {
			super(myViewer, myElement, myTreeItem);
		}
	}
	
	private class PkgListBuildConfigurationItem extends PkgListBaseCustomItem {

		PkgListBuildConfigurationItem(PkgListTreeViewer myViewer,
				Object myElement, TreeItem myTreeItem) {
			super(myViewer, myElement, myTreeItem);
		}
	}
	
	private class PkgListFileItem extends PkgListBaseCustomItem {

		PkgListFileItem(PkgListTreeViewer myViewer, Object myElement,
				TreeItem myTreeItem) {
			super(myViewer, myElement, myTreeItem);
		}
	}
	
	private class PkgListTreeViewerComparer implements IElementComparer {

		public boolean equals(Object a, Object b) {
			// if they're both build configuration items, check that their packages and configurations match
			if (   PkgListTree.isBuildConfigurationItem(a)
				&& PkgListTree.isBuildConfigurationItem(b)) {
				ICarbideBuildConfiguration configA = (ICarbideBuildConfiguration)a;
				ICarbideBuildConfiguration configB = (ICarbideBuildConfiguration)b;
				return (   configA.getCarbideProject().getProject() == configB.getCarbideProject().getProject()
				        && configA.getDisplayString().equals(configB.getDisplayString()));
			}
			else if (PkgListTree.isFileItem(a) &&
				PkgListTree.isFileItem(b)) {
					IPkgEntry fileA = (IPkgEntry)a;
					IPkgEntry fileB = (IPkgEntry)b;
					if (fileA.getPkgFile().equals(fileB.getPkgFile()) &&
							fileA.getSdk().equals(fileB.getSdk())) {
						return true;
					}
					return false;
				}
			return a.equals(b);
		}

		public int hashCode(Object element) {
			return element.hashCode();
		}
		
	}

	public PkgListTreeViewer(Composite arg0, int arg1) {
		// Must use NO_RADIO_GROUP, otherwise SWT automatically handle unselect for only
		// one item, I haven't find a way to create new composite for each project node.
		// Plus we want want to handle that the not so normal interaction among 
		// project checkbox/config radio button manually
		super(arg0, arg1 | SWT.NO_RADIO_GROUP | SWT.V_SCROLL | SWT.MULTI);
		super.setComparer(new PkgListTreeViewerComparer());
	}
	
	@Override
	public void setContentProvider(IContentProvider provider) {
		super.setContentProvider(provider);
		if (provider instanceof PkgListTreeContentProvider) {
			contentProvider = (PkgListTreeContentProvider)provider;
		} else {
			GeneralMessages.showErrorMessage(Messages.getString("PkgListTreeViewer.invalid.content.provider")); //$NON-NLS-1$
		}
	}
	
	
	private Map<Object, PkgListBaseCustomItem> itemMap = new HashMap<Object, PkgListBaseCustomItem>();
	private int style = 0;

	@Override
	protected void inputChanged(Object input, Object oldInput) {
//		collapseAll();	// just highlight the selected on, setSelectedItems() takes care of expand
		super.inputChanged(input, oldInput);
	}
	
	public void initializeSelectedItems(TreePath[] paths) {
		for (TreePath path : paths) {
			reveal(path);
		}
		TreeSelection elementSelection = new TreeSelection(paths);
		setSelection(elementSelection, true);
	}

	public IPkgEntry[] getSelectedPkgs() {
		List<Object> selectionList = getSelectionFromWidget();
		ArrayList<IPkgEntry> selectedPkgItems = new ArrayList<IPkgEntry>();
		for (Object item : selectionList) {
			if (item instanceof IPkgEntry) {
				selectedPkgItems.add((IPkgEntry) item);
			}
		}
		return selectedPkgItems.toArray(new IPkgEntry[selectedPkgItems.size()]);
	}

	public IProject[] getSelectedProjects() {
		List<Object> selectionList = getSelectionFromWidget();
		ArrayList<IProject> selectedProjects = new ArrayList<IProject>();
		for (Object item : selectionList) {
			if (item instanceof IProject) {
				selectedProjects.add((IProject) item);
			}
		}
		return selectedProjects.toArray(new IProject[selectedProjects.size()]);
	}

	public ICarbideBuildConfiguration[] getSelectedConfigs() {
		List<Object> selectionList = getSelectionFromWidget();
		ArrayList<ICarbideBuildConfiguration> selectedConfigs = new ArrayList<ICarbideBuildConfiguration>();
		for (Object item : selectionList) {
			if (item instanceof ICarbideBuildConfiguration) {
				selectedConfigs.add((ICarbideBuildConfiguration) item);
			}
		}
		return selectedConfigs.toArray(new ICarbideBuildConfiguration[selectedConfigs.size()]);
	}
		
	/**
	 * Set additional style bits for radio button appearance.
	 * Most likely style added may be FLAT when in a non-3D view.
	 * @param style int
	 * @see SWT#FLAT
	 */
	public void setStyle(int style) {
		this.style  = style;
	}
		
	/**
	 * We override associate to maintain each items optional
	 * radio button information.
	 */
	@Override
	protected void associate(final Object element, Item item) {
		super.associate(element, item);
		if (!(item instanceof TreeItem)) {
			return;
		}
		
		TreeItem treeItem = (TreeItem)item;
		
		PkgListBaseCustomItem baseItem = itemMap.get(getItemMapKey(element));
		if (baseItem == null) {
			int buttonStyle = style;
			SelectionAdapter selectionAdapter;
			boolean enabled = true;
			
			if (PkgListTree.isBuildConfigurationItem(element)) {
				baseItem = new PkgListBuildConfigurationItem(this, element, treeItem);
				buttonStyle = buttonStyle | SWT.RADIO;
				selectionAdapter = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						toggleSelectionAndUpdateSelectionToViewer(element);
					}
				};
				// check for bad PKG, which may happen on regular Symbian stationary
				List<ISISBuilderInfo> sisBuilderInfoList = ((ICarbideBuildConfiguration)element).getSISBuilderInfoList();
				boolean allPkgGood = true && sisBuilderInfoList.size() > 0;
				for (ISISBuilderInfo sisBuilderInfo : sisBuilderInfoList)
				{
					if (sisBuilderInfo == null) {
						allPkgGood = false;
					} else if (sisBuilderInfo.getPKGFullPath().toFile().exists() == false) {
						allPkgGood = false;
					}
			
				}
				enabled = allPkgGood;
			} else if (PkgListTree.isProjectItem(element)) {
				baseItem = new PkgListProjectItem(this, element, treeItem);
				buttonStyle = buttonStyle | SWT.CHECK;
				selectionAdapter = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						toggleSelectionAndUpdateSelectionToViewer(element);
					}						
				};
				ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo((IProject)element);
				if (cpi != null) {
					enabled = false;
					List<ICarbideBuildConfiguration> bc = cpi.getBuildConfigurations();
					for (ICarbideBuildConfiguration config : bc) {
						// exclude configuration with (none) PKG
						List<ISISBuilderInfo> sisBuilderInfoList = ((ICarbideBuildConfiguration)config).getSISBuilderInfoList();
						for (ISISBuilderInfo sisBuilderInfo : sisBuilderInfoList)
						{
							if (sisBuilderInfo == null) {
								continue;
							} else if (sisBuilderInfo.getPKGFullPath().toFile().exists()) {
								enabled = true;
								break;
							}
					
						}					
					}
				}
			} else if (PkgListTree.isFileItem(element)) {
				baseItem = new PkgListFileItem(this, element, treeItem);
				buttonStyle = buttonStyle | SWT.CHECK;
				selectionAdapter = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						toggleSelectionAndUpdateSelectionToViewer(element);
					}						
				};
			} else {
				return;	// not a custom item
			}

			baseItem.createButton(buttonStyle, selectionAdapter);
			baseItem.setEnabled(enabled);
			
			itemMap.put(getItemMapKey(element), baseItem);
		}
	}
	
	/**
	 * We override disassociate to clean up any
	 * allocated radio button information and to 
	 * help implement force selection.
	 */
	@Override
	protected void disassociate(Item item) {
		Object element = item.getData();
		if(PkgListTree.isBuildConfigurationItem(element) ||
				PkgListTree.isFileItem(element) ||
				PkgListTree.isProjectItem(element))
		{	
			PkgListBaseCustomItem baseItem = itemMap.get(getItemMapKey(element));
			if (baseItem != null) {
				if (baseItem instanceof PkgListBuildConfigurationItem ||
						baseItem instanceof PkgListProjectItem ||
						baseItem instanceof PkgListFileItem ) 
				{
					baseItem.dispose();
					itemMap.remove(getItemMapKey(element));
				}
				else {
//					shouldn't get here
				}
			}
		}
		super.disassociate(item);
	}
	
	// We override this underlying selection to insert UI handling of our custom
	// UI associated, can't use Listeners from TreeViewer to propagate because 
	// we are not SWT.CHECKBOX
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#setSelectionToWidget(java.util.List, boolean)
	 */
	@Override
	protected void setSelectionToWidget(List l, boolean reveal) {
		super.setSelectionToWidget(l, reveal);
		// handle UI
		refreshSelectionOnUI();
	}
	
	public void refreshSelectionOnUI () {
		for (Iterator<Entry<Object, PkgListBaseCustomItem>> iter = itemMap.entrySet().iterator(); iter.hasNext();) {
			Entry<Object, PkgListBaseCustomItem> entry = iter.next();
			entry.getValue().setSelection(false);
		}
		List selectedList = getSelectionFromWidget();
		for (Object o : selectedList) {
			reveal(o);		// only expanded/visible items are available for selecting programatically
							// hidden items that never been expanded cannot
			PkgListBaseCustomItem plbci = itemMap.get(getItemMapKey(o));
			if(plbci != null) {
				plbci.setSelection(true);
			}
		}
	}
	
	private ArrayList<TreePath> buildArrayWithoutElement(TreePath[] selectedPaths, Object element) {
		ArrayList<TreePath> returnPath = new ArrayList<TreePath> ();
		for (TreePath path : selectedPaths) {
			boolean skip = false;
			for (int i = 0; i < path.getSegmentCount(); i++) {
				if (path.getSegment(i).equals(element)) {
					skip = true;
					break;
				}
			}
			if (skip == false) {
				returnPath.add(path);
			}
		}
		return returnPath;
	}
	
	public TreePath getTreePathForElement(Object element) {
		// go upward from element to root, then flip the list
		ArrayList<Object> segments = new ArrayList<Object>();
		Object current = element;
		
		do {
			segments.add(current);
			current = contentProvider.getParent(current);
		} while (current != null);
		
		Object[] reverse = segments.toArray(new Object[segments.size()]);
		Object[] inOrder = new Object[segments.size()];
		
		for (int i = 0; i < segments.size(); i++) {
			inOrder[i] = reverse[segments.size() - 1 - i ];
		}
		
		return new TreePath(inOrder);
	}
	
	/*
	 * Build and update the tree selection reflecting the element you want to toggle
	 */
	private void toggleSelectionAndUpdateSelectionToViewer(final Object element)
	{
		ArrayList<TreePath> myNewTreePath;
		ISelection iSelection = this.getSelection();
		ITreeSelection iTreeSelection;
		if (iSelection instanceof TreeSelection) {
			iTreeSelection = (TreeSelection)iSelection;
		} else {
			return;
		}
		TreePath[] selectedPaths = iTreeSelection.getPaths();

		if (PkgListTree.isFileItem(element)) {
			if (iTreeSelection.getPathsFor(element).length > 0) {
				// file already selected, clear self
				myNewTreePath = buildArrayWithoutElement(selectedPaths, element);
			} else {
				// file not yet selected, select self
				myNewTreePath = new ArrayList<TreePath>();
				for (TreePath path: selectedPaths) {
					myNewTreePath.add(path);
				}
				TreePath self = getTreePathForElement(element);
				if (self != null) {
					myNewTreePath.add(self);
				}
			}
		} else if (PkgListTree.isBuildConfigurationItem(element)) {
			Object projectSelection = contentProvider.getParent(element);
			myNewTreePath = buildArrayWithoutElement(selectedPaths, projectSelection);
			if (iTreeSelection.getPathsFor(element).length > 0) {
				// configuration already selected, clear self and parent project
				// do nothing, myNewTreePath already copied without parent project and self
			} else {
				// configuration not yet selected, clear siblings, select self
				TreePath self = getTreePathForElement(element);
				// myNewTreePath already copied without parent project and self
				if (self != null) {
					myNewTreePath.add(self);	// add self back
				}
				TreePath projectPath = getTreePathForElement(projectSelection);
				if (projectPath != null) {
					myNewTreePath.add(projectPath);	// add project back
				}
			}
		} else if (PkgListTree.isProjectItem(element)) {
			myNewTreePath = buildArrayWithoutElement(selectedPaths, element);
			if (iTreeSelection.getPathsFor(element).length > 0) {
				// project already selected, clear self and all children configurations
				// do nothing, myNewTreePath already copied without parent project and self
			} else {
				// project not yet selected, select self and a child configuration(default or next valid)
				TreePath self = getTreePathForElement(element);
				
				// so children are associated
				expandToLevel(self, 1);
				// I don't know why it didn't repaint proper without this
				collapseToLevel(self, 1);
				
				IProject project = (IProject) element;
				Object[] children = this.getRawChildren(project);
				
				ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo(project);
				ICarbideBuildConfiguration configDefault = cpi.getDefaultConfiguration();
				ICarbideBuildConfiguration configDefaultOrFirstGood = null;
				
				// enabled default or first enabled child
				for (Object child : children) {
					if (itemMap.get(getItemMapKey(child)) == null) {
						continue;
					}
					if (itemMap.get(getItemMapKey(child)).getEnabled()) {
						if (configDefaultOrFirstGood == null || 
							((ICarbideBuildConfiguration)child).getDisplayString().equals(configDefault.getDisplayString())) {
							configDefaultOrFirstGood = (ICarbideBuildConfiguration)child;
						}
					}
				}
				if (configDefaultOrFirstGood  != null) {
					// myNewTreePath already copied without self and children
					if (self != null) {
						myNewTreePath.add(self);	// add self back
					}
					TreePath child = getTreePathForElement(configDefaultOrFirstGood);
					// myNewTreePath already copied without self and children
					if (child != null) {
						myNewTreePath.add(child);	// add child back
					}
				}
			}
		} else {
			myNewTreePath = new ArrayList<TreePath> ();
			for (TreePath path : selectedPaths) {
				myNewTreePath.add(path);
			}
		}

		// fire event
		//updateSelection(getSelection());
		
		// select from viewer, viewer will fire event and eventually change UI with our
		// overrode routine
		ITreeSelection treeSelection = new TreeSelection(myNewTreePath.toArray(new TreePath[myNewTreePath.size()]));
		setSelection(treeSelection, true);
	}
	
	public boolean getElementEnabled(Object element) {
		PkgListBaseCustomItem item = itemMap.get(getItemMapKey(element));
		if (item != null) {
			return item.getEnabled();
		}
		return false;
	}
	
	private Object getItemMapKey(Object element) {
		// because ICarbideBuildConfiguration says keys are equal even when their projects
		// do not match, use the project concatenated with configuration as the key for ICarbideBuildConfiguration
		if (element instanceof ICarbideBuildConfiguration) {
			ICarbideBuildConfiguration config = (ICarbideBuildConfiguration) element;
			return config.getCarbideProject().getProject() + "/" + config.getDisplayString();
		} else {
			return element;
		}
	}
}
