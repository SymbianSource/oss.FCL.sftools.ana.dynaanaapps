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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;

public class PkgListTreeLabelProvider implements ILabelProvider {
	
	WorkbenchLabelProvider lp = new WorkbenchLabelProvider();

	public Image getImage(Object element) {
		if (element instanceof IPkgEntry || element instanceof ICarbideBuildConfiguration)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

		return lp.getImage(element);
	}

	public String getText(Object element) {
		
		if (element instanceof IPkgEntry) {
			String result = Messages.getString("PkgListTreeLabelProvider.sample.file") + ((IPkgEntry)element).getPkgFile(); //$NON-NLS-1$
			if (((IPkgEntry)element).getSdk() != null) {
				result += Messages.getString("PkgListTreeLabelProvider.sdk") + ((IPkgEntry)element).getSdk().getUniqueId(); //$NON-NLS-1$
			}
			return result;
		} else if (element instanceof ICarbideBuildConfiguration) {
			return ((ICarbideBuildConfiguration)element).getDisplayString();
		}

		return lp.getText(element);

	}

	public void addListener(ILabelProviderListener listener) {
		lp.addListener(listener);
	}

	public void dispose() {
		lp.dispose();
	}

	public boolean isLabelProperty(Object element, String property) {
		return lp.isLabelProperty(element, property);
	}

	public void removeListener(ILabelProviderListener listener) {
		lp.removeListener(listener);
	}

}
