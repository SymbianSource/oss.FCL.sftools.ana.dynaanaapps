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

package com.nokia.carbide.cpp.internal.pi.button.ui;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;


public class BupProfileTreeViewer extends TreeViewer {
	private static final String WORKSPACE_PREF = Messages.getString("BupProfileTreeViewer.0"); //$NON-NLS-1$
	private static final String CARBIDE_BUILTIN = Messages.getString("BupProfileTreeViewer.1"); //$NON-NLS-1$

	private ISymbianSDK romKit;
	private Set<ISymbianSDK> appKits;
	private ISelection lastValidSelection;
	
	public class BupProfileTreeData {
		private ISymbianSDK romSdk;
		private Set<ISymbianSDK> appSdks;
		
		public BupProfileTreeData(ISymbianSDK rom, Set<ISymbianSDK> app) {
			romSdk = rom;
			appSdks = app;
		}
		
		public ISymbianSDK getRomSdk() {
			return romSdk;
		}
		
		public Set<ISymbianSDK> getAppSdk() {
			return appSdks;
		}
	}
		
	public class BupProfileTreeContentProvider implements ITreeContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object arg0) {
			if (arg0 instanceof String) {
				if (((String)arg0).equals(WORKSPACE_PREF)) {
					ArrayList<IBupEventMapProfile> workspaceProfiles = BupEventMapManager.getInstance().getProfilesFromWorkspacePref();
					return workspaceProfiles.toArray();
				}
				if (((String)arg0).equals(CARBIDE_BUILTIN)) {
					ArrayList<IBupEventMapProfile> builtinProfiles = BupEventMapManager.getInstance().getProfilesFromBuiltin();
					return builtinProfiles.toArray();
				}
			}
			if (arg0 instanceof ISymbianSDK) {
				ArrayList<IBupEventMapProfile> sdkProfile = BupEventMapManager.getInstance().getProfilesFromSDK((ISymbianSDK)arg0);
				return sdkProfile.toArray();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object arg0) {
			// not implemented
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object arg0) {
			if (arg0 instanceof String) {
				if (((String)arg0).equals(WORKSPACE_PREF) || ((String)arg0).equals(CARBIDE_BUILTIN)) {
					return true;
				}
			}
			if (arg0 instanceof ISymbianSDK) {
				return true;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object arg0) {
			ArrayList<Object> elements = new ArrayList<Object>();
			if (romKit != null) {
				if (getChildren(romKit).length > 0) {
					elements.add(romKit);
				}
			}
			
			ISymbianSDK [] appSdks = appKits.toArray(new ISymbianSDK[appKits.size()]);
			for (ISymbianSDK sdk : appSdks) {
				if (getChildren(sdk).length > 0) {
					elements.add(sdk);
				}
			}
			elements.add(WORKSPACE_PREF);
			elements.add(CARBIDE_BUILTIN);
			
			return elements.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof BupProfileTreeData) {
				BupProfileTreeData data = (BupProfileTreeData)newInput;
				romKit = data.romSdk;
				appKits = data.appSdks;
			}
		}
		
	}
	
	private class BupProfileTreeLabelProvider extends LabelProvider implements IBaseLabelProvider{
		public String getText(Object element) {
			if (element instanceof IBupEventMapProfile) {
				return ((IBupEventMapProfile)element).getProfileId();
			}
			if (element instanceof String) {
				if (((String)element).equals(WORKSPACE_PREF)) {
					return (String)element;
				}
				if (((String)element).equals(CARBIDE_BUILTIN)) {
					return (String)element;
				}
			}
			if (element instanceof ISymbianSDK) {
				return Messages.getString("BupProfileTreeViewer.2") + ((ISymbianSDK)element).getUniqueId(); //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * @param parent
	 * @param style
	 */
	public BupProfileTreeViewer(Composite parent) {
		super(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		setContentProvider(new BupProfileTreeContentProvider());
		setLabelProvider(new BupProfileTreeLabelProvider());
		final BupProfileTreeViewer viewerSelf = this;
		this.getTree().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				Object data = arg0.item.getData();
				if (!(data instanceof IBupEventMapProfile)) {
					// we are a SWT.SINGLE, so we select last good selection canceling this one,
					// it behaves like last valid selection stick 
					viewerSelf.setSelection(lastValidSelection);
				} else {
					// remember last valid selection
					lastValidSelection = viewerSelf.getSelection();
				}
			}
		});
	}

	/**
	 * Handy routine for reading selection
	 * 
	 * @return
	 */
	public IBupEventMapProfile getSelectedProfile() {
		if (getSelection().isEmpty()) {
			return null;
		} else {
			ISelection selection = getSelection();
			if (selection instanceof ITreeSelection) {
				Object firstElement = ((ITreeSelection)selection).getFirstElement();
				if (firstElement instanceof IBupEventMapProfile) {
					return (IBupEventMapProfile)firstElement;
				}
			}
		}
		return null;
	}
}
