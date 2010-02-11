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

package com.nokia.carbide.cpp.internal.pi.properties;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.zip.GZIPInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.nokia.carbide.cpp.internal.pi.manager.PluginInitialiser;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.internal.pi.test.PIAnalysisInfo;
import com.nokia.carbide.cpp.internal.pi.test.BappeaAnalysisInfo;
import com.nokia.carbide.cpp.internal.pi.utils.PluginClassLoader;


public class PIPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();

		// we will read the first object in the file and use it to
		IAdaptable resource = this.getElement();

		if (!(resource instanceof IFile))
			return null;

		IFile file = (IFile) resource;
		InputStream input = null;
		
		// make sure we can open an input stream to the trace file
		try {
			input = file.getContents();
		} catch (CoreException e) {
			System.out.println(Messages.getString("PIPropertyPage.cannotReadFile") + file.getName()); //$NON-NLS-1$
			return null;
		}
		
		if (input == null)
			return null;
		
		// the file contains Java objects that have been gziped
		GZIPInputStream ziss = null;
		try {
			ziss = new GZIPInputStream(input);
		} catch (IOException e) {
			System.out.println(Messages.getString("PIPropertyPage.cannotReadFile") + file.getName()); //$NON-NLS-1$
			return null;
		}

		BufferedInputStream bis = new BufferedInputStream(ziss);
		ObjectInputStream ois = null;
		Object ou = null;

	    try {
			ois = new ObjectInputStream(bis) {
			    @SuppressWarnings("unchecked") //$NON-NLS-1$
				protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
			    	// each object read must have a class corresponding to a plugin class
			    	String name = desc.getName();
					Class c = PluginInitialiser.getPluginClass(name);
					
					if (c == null) {
						try {
							c = Class.forName(name);
						} catch (ClassNotFoundException e) {
							// see if we have a replacement
							PluginClassLoader pcl = (PluginClassLoader)PluginInitialiser.getPluginClassLoader();
							// don't catch for class not found exception, they did it on purpose 
							// to back out of this missing plugin that exist in data file
							c = pcl.findClass(name);
						}
					}
					
			   		return c;
			    }
			};

			//read the first object
           	ou = ois.readObject();
        }
    	catch (ClassNotFoundException cnfe) {
	    }
	    catch(EOFException eof) {
	    }
	    catch (InvalidClassException ie) {
	    }
        catch (IOException e) {
        }
        catch (Exception eih) {
        };
	    
	    // close the readers
        if (ois != null)
			try {
				ois.close();
			}
        	catch (IOException e) {
			}
		
		if (bis != null)
			try {
				bis.close();
			}
			catch (IOException e) {
			}
		
		if (ziss != null)
			try {
				ziss.close();
			}
			catch (IOException e) {
			}

		AnalysisInfoHandler aih = new AnalysisInfoHandler();
		
		if (ou instanceof PIAnalysisInfo) {
			aih.analysisDataReader((PIAnalysisInfo) ou);
			aih.getAnalysisInfoLabels(parent);
		} else if (ou instanceof BappeaAnalysisInfo) {
			aih.analysisDataReader((BappeaAnalysisInfo) ou);
			aih.getAnalysisInfoLabels(parent);
		} else {
			Label label = new Label(parent, SWT.WRAP);
			label.setText(Messages.getString("PIPropertyPage.noPIinformation")); //$NON-NLS-1$
		}

		return null;
	}
}
