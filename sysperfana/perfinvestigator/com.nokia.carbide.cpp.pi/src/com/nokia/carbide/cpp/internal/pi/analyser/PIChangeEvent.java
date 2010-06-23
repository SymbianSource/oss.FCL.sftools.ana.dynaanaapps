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

package com.nokia.carbide.cpp.internal.pi.analyser;

import java.util.ArrayList;

import com.nokia.carbide.cpp.pi.editors.PIPageEditor;

public final class PIChangeEvent {

	private PIChangeEvent() {
		/* this class can only have static method */
	}
	
	public static void action(String actionString) {
		processEvent(actionString);
	}
	
	public static void processEvent(String actionString) {
		int uid = NpiInstanceRepository.getInstance().activeUid();
		if (   actionString.equals("+") //$NON-NLS-1$
			 || actionString.equals("-") //$NON-NLS-1$
			 || actionString.equals("++") //$NON-NLS-1$
			 || actionString.equals("--")) //$NON-NLS-1$
		{
			ArrayList<ProfileVisualiser> list = NpiInstanceRepository.getInstance().activeUidGetProfilePages();
			
			// for zoom to selected time interval, there must be a selected time interval
			if (   actionString.equals("--") //$NON-NLS-1$
				&& !NpiInstanceRepository.getInstance().getProfilePage(uid, 0).getTopComposite().validInterval()) {
				// let the first page supply the error message
				NpiInstanceRepository.getInstance().getProfilePage(uid, 0).action(actionString);
				return;
			}
			
			for (int i = 0; i < list.size(); i++)
				NpiInstanceRepository.getInstance().getProfilePage(uid, i).action(actionString);
		}
		else if (   actionString.equals("changeInterval") //$NON-NLS-1$
				 || actionString.equals("changeSelection") //$NON-NLS-1$
				 || actionString.equals("changeThresholdThread") //$NON-NLS-1$
				 || actionString.equals("changeThresholdBinary") //$NON-NLS-1$
				 || actionString.equals("changeThresholdFunction")) //$NON-NLS-1$
		{
			ArrayList<ProfileVisualiser> list = NpiInstanceRepository.getInstance().activeUidGetProfilePages();
			
			for (int i = 0; i < list.size(); i++)
				NpiInstanceRepository.getInstance().getProfilePage(uid, i).action(actionString);
		}
		else
		{
			NpiInstanceRepository.getInstance().getProfilePage(uid, PIPageEditor.currentPageIndex()).action(actionString);
		}
	}
}
