/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class ATResourceListener
 *
 */

package com.nokia.s60tools.analyzetool.ui;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.nokia.s60tools.analyzetool.Activator;

/**
 * Listens projec changes
 * @author kihe
 *
 */
public class ATResourceListener implements IResourceChangeListener{

	/**
	 * Listens workspace resource changes.
	 * If resource is BuildComplete file => notifies AT UI that module build state is changed
	 *
	 * @param event Resource changed event
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		try {

			//if event type is change
			if( event.getType() == IResourceChangeEvent.POST_CHANGE ) {

				//get delta
				IResourceDelta delta = event.getDelta();

				//
				if(delta != null ) {

					//add new IResourceDeltaVisitor for delta
					//now we can thru all the changed files
					delta.accept( new IResourceDeltaVisitor() {
						boolean updateView = true;


						public boolean visit(IResourceDelta newDelta) {

							//we are only interested in new files
							if(newDelta.getKind() != IResourceDelta.ADDED ) {
								return true;
							}

							//get resource
							IResource res = newDelta.getResource();

							//resource if BuildComplete file
							if( res.getName().equalsIgnoreCase("BuildComplete")) {

								//notify AT UI that module build state is changed
								IActionListener listener = Activator.getActionListener();
								if( listener != null && updateView && res.getProject() != null ) {
									listener.buildStateChanged(res.getProject());
									updateView = false;
								}
							}

							return true;
						}
					}
					);
				}
			}
		}catch(CoreException ce) {
			ce.printStackTrace();
		}



	}


}


