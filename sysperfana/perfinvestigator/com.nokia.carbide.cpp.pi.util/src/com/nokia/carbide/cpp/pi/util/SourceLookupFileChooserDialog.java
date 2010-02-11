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

package com.nokia.carbide.cpp.pi.util;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.ui.CarbideUIPlugin;
import com.nokia.carbide.cpp.ui.ICarbideSharedImages;

public class SourceLookupFileChooserDialog extends TitleAreaDialog {

	IASTFileLocation[] locations = null;
	IASTFileLocation selectedLocation = null;
	
	// control
	private Composite composite = null;
	private Table table = null;

	protected SourceLookupFileChooserDialog(Shell arg0, IASTFileLocation[] locs) {
		super(arg0);
		locations = locs;
	}
	
	public Control createDialogArea(Composite parent) {
		// use image from support plugin
		setDefaultImage(CarbideUIPlugin.getSharedImages().getImageDescriptor(ICarbideSharedImages.IMG_CARBIDE_C_ICON_16_16).createImage());
		getShell().setText(Messages.getString("SourceLookupFileChooserDialog.text")); //$NON-NLS-1$
		setTitle(Messages.getString("SourceLookupFileChooserDialog.title")); //$NON-NLS-1$
		setMessage(Messages.getString("SourceLookupFileChooserDialog.message")); //$NON-NLS-1$

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(gridLayout);
				
	    table = new Table(composite, SWT.SINGLE);
	    table.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				selectedLocation = (IASTFileLocation)table.getSelection()[0].getData();
			}
	    	
	    });
	    table.addMouseListener(new MouseListener(){
	    	// allow double click instead of OK
			public void mouseDoubleClick(MouseEvent arg0) {
				selectedLocation = (IASTFileLocation)table.getSelection()[0].getData();
				okPressed();
			}

			public void mouseDown(MouseEvent arg0) {
			}

			public void mouseUp(MouseEvent arg0) {
			}
	    	
	    });
	    
	    for(IASTFileLocation location : locations) {

			IPath path = new Path(location.getFileName());
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

			String fileName = null;
			String containerName = null;
			
			if (file != null) {
				fileName = file.getName();
				containerName = "project " + file.getProject().getName();	//$NON-NLS-1$
			} else {
				fileName = path.segment(path.segmentCount() - 1);
				containerName = path.uptoSegment(path.segmentCount() - 2).toString();	//$NON-NLS-1$
			}
	    	
	    	TableItem item = new TableItem(table, SWT.NONE);
	    	item.setText(fileName + " - " + containerName);	//$NON-NLS-1$
	    	item.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));
	    	item.setData(location);
	    }
	    
	    return composite;
	}
	
	protected void okPressed() {
		if (selectedLocation != null) {
			super.okPressed();
		}
	}
	
	protected void cancelPressed() {
		selectedLocation = null;
		super.cancelPressed();
	}
	
	public IASTFileLocation getLocation() {
		return selectedLocation;
	}
}
