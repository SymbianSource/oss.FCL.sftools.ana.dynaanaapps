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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.nokia.cpp.internal.api.utils.core.Logging;

/* This GeneralMessage displays message box using synchronized run
 * from the UI thread, so it is safe to be called by non-UI thread 
 * */

public class GeneralMessages 
{
	public static final int OK = IStatus.OK;
	public static final int INFO = IStatus.INFO;
	public static final int WARNING = IStatus.WARNING;
	public static final int ERROR = IStatus.ERROR;
	public static final int CANCEL = IStatus.CANCEL;
	public static final int USERDUMP = 0xff;
	
	static boolean result = false;
	
	public static void showErrorMessage(final String error) {
		Display.getDefault().syncExec( new Runnable() {
			public void run () {
				String displayMessage;
				MessageBox messageBox = new MessageBox(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.ICON_ERROR | SWT.OK | SWT.SYSTEM_MODAL);
				if (error == null){
					displayMessage = Messages.getString("GeneralMessages.unknownError"); //$NON-NLS-1$
				}else{
					displayMessage = error;
				}
				messageBox.setMessage(displayMessage);
				messageBox.setText(Messages.getString("GeneralMessages.piError")); //$NON-NLS-1$
				messageBox.open();				
			}
		});
	}

	public static void showNotificationMessage(final String notification) {
		Display.getDefault().syncExec( new Runnable() {
			public void run () {
				MessageBox messageBox = new MessageBox(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.ICON_INFORMATION | SWT.OK | SWT.SYSTEM_MODAL);
				messageBox.setMessage(notification);
				messageBox.setText(Messages.getString("GeneralMessages.piMessage")); //$NON-NLS-1$
				messageBox.open();
			}
		});
	}

	public static void showWarningMessage(final String notification) {
		Display.getDefault().syncExec( new Runnable() {
			public void run () {
				MessageBox messageBox = new MessageBox(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.ICON_WARNING | SWT.OK | SWT.SYSTEM_MODAL);
				messageBox.setMessage(notification);
				messageBox.setText(Messages.getString("GeneralMessages.piWarning")); //$NON-NLS-1$
				messageBox.open();
			}
		});
	}

	public static boolean showProblemMessage(final String problem) {
		Display.getDefault().syncExec( new Runnable() {
			public void run () {
				MessageBox messageBox = new MessageBox(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.ICON_WARNING | SWT.OK | SWT.CANCEL | SWT.SYSTEM_MODAL);
				messageBox.setMessage(problem);
				messageBox.setText(Messages.getString("GeneralMessages.piWarning")); //$NON-NLS-1$
				int returnCode = messageBox.open();

				if (returnCode == SWT.CANCEL) {
					System.out.println(Messages.getString("GeneralMessages.cancel")); //$NON-NLS-1$
					result = false;
				} else {
					System.out.println(Messages.getString("GeneralMessages.ok")); //$NON-NLS-1$
					result = true;
				}
			}
		});
		return result;
	}

	public static boolean showQuestionMessage(final String question)
	{
		Display.getDefault().syncExec( new Runnable() {
			public void run () {
				MessageBox messageBox = new MessageBox(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.SYSTEM_MODAL);
				messageBox.setMessage(question);
				messageBox.setText(Messages.getString("GeneralMessages.piMessage")); //$NON-NLS-1$
				int returnCode = messageBox.open();

				if (returnCode == SWT.NO) {
					System.out.println(Messages.getString("GeneralMessages.no")); //$NON-NLS-1$
					result = false;					
				} else {
					System.out.println(Messages.getString("GeneralMessages.yes")); //$NON-NLS-1$
					result = true;
				}
			}
		});
		return result;
	}
	
	public static void piLog(String message, int severity) {
		piLog(message, severity, new Throwable());
	}
	
	public static void piLog(String message, int severity, Throwable throwable) {

		switch (severity) {
			case IStatus.OK:
			case IStatus.INFO:
			case IStatus.WARNING:
			case IStatus.ERROR:
			case IStatus.CANCEL:
				break;
			case GeneralMessages.USERDUMP:
				severity = IStatus.INFO;
			default:
				severity = IStatus.ERROR;
				break;
		}
	
		IStatus status = Logging.newSimpleStatus(1 /* our caller */, 
				severity, 
                Messages.getString("GeneralMessages.pi") + message, //$NON-NLS-1$
                throwable);
		Logging.log(PIUtilPlugin.getDefault(), status);
	}
}
