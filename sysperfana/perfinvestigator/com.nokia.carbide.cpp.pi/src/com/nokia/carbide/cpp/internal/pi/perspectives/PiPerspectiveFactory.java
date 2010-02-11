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

package com.nokia.carbide.cpp.internal.pi.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PiPerspectiveFactory implements IPerspectiveFactory {

	private static final String PI_VIEW_ID =
		"com.nokia.carbide.cpp.pi.views.PiView1"; //$NON-NLS-1$
	
	private static final String PACKAGE_EXPLORER_VIEW_ID =
		"org.eclipse.jdt.ui.PackageExplorer"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout) {
		// get the editor area
		String editorArea = layout.getEditorArea();
		
		// put the Package Explorer view left of the editor area
		layout.addView(
				PACKAGE_EXPLORER_VIEW_ID,
				IPageLayout.LEFT,
				0.20f,
				editorArea);
		
		// put the PI view above the editor area
		IFolderLayout top =
			layout.createFolder(
					"top",  //$NON-NLS-1$
					IPageLayout.TOP,
					0.66f,
					editorArea);
		
		top.addView(PI_VIEW_ID);
	}

}
