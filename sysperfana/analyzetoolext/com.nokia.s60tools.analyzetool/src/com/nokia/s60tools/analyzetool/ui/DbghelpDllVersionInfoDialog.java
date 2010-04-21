/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class DbghelpDllVersionInfoDialog
 *
 */

package com.nokia.s60tools.analyzetool.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.nokia.s60tools.analyzetool.Activator;
import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Custom dialog with clickable link and Ok and Cancel buttons for
 * displaying of dbghelp.dll version information.
 * 
 * @author lukamil
 * 
 */
public class DbghelpDllVersionInfoDialog extends Dialog {

	private String dbghelpDllVersionInfo;

	public DbghelpDllVersionInfoDialog(Shell shell, String dbghelpDllVersionInfo) {
		super(shell);
		this.dbghelpDllVersionInfo = dbghelpDllVersionInfo;
	}

	@Override
	protected void configureShell(Shell shell) {
		shell.setText(Constants.ANALYZE_TOOL_TITLE);
		super.configureShell(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		Composite imageComposite = new Composite(container, SWT.NONE);
		imageComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));
		imageComposite.setLayout(new GridLayout());

		Label imageLabel = new Label(imageComposite, SWT.NONE);
		imageLabel.setImage(getShell().getDisplay().getSystemImage(
				SWT.ICON_INFORMATION));

		Composite infoComposite = new Composite(container, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		infoComposite.setLayout(gridLayout);

		String[] array = dbghelpDllVersionInfo.split("\n");
		String info = "";

		for (int i = 0; i < array.length - 1; i++) {
			info = info + array[i] + "\n";
		}

		String link = array[array.length - 1];

		Label infoLabel = new Label(infoComposite, SWT.NONE);
		infoLabel.setText(info);

		FormToolkit toolkit = new FormToolkit(getShell().getDisplay());
		Hyperlink hyperlink = toolkit.createHyperlink(infoComposite, link,
				SWT.NONE);
		hyperlink.setBackground(getShell().getBackground());
		hyperlink.setUnderlined(false);
		hyperlink.setHref(link);

		hyperlink.addHyperlinkListener(new IHyperlinkListener() {
			public void linkEntered(HyperlinkEvent event) {
			}

			public void linkExited(HyperlinkEvent event) {
			}

			public void linkActivated(HyperlinkEvent event) {
				IWorkbenchBrowserSupport browserSupport = Activator
						.getDefault().getWorkbench().getBrowserSupport();

				try {
					IWebBrowser browser = browserSupport.getExternalBrowser();
					browser.openURL(new URL(event.getHref().toString()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});

		Label questionLabel = new Label(infoComposite, SWT.NONE);
		questionLabel.setText("Would you like to continue?");

		return container;
	}
}
