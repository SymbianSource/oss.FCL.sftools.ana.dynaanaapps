/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
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
 * Description:
 *
 * Show Trace Information Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.nokia.traceviewer.TraceViewerHelpContextIDs;

/**
 * Show Trace Information Dialog class
 * 
 */
public final class ShowTraceInfoDialog extends BaseDialog {

	/**
	 * Font to use
	 */
	private static final String FONT = "Courier New"; //$NON-NLS-1$

	/**
	 * View font size
	 */
	public static final int FONT_SIZE = 8;

	/**
	 * Contents of the dialog
	 */
	private final String contents;

	/**
	 * List containing style ranges
	 */
	private final List<StyleRange> styleRanges;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 * @param text
	 *            contents of the dialog
	 * @param styleRanges
	 *            style ranges
	 */
	public ShowTraceInfoDialog(Shell parent, String text,
			List<StyleRange> styleRanges) {
		super(parent, SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE);
		this.contents = text;
		this.styleRanges = styleRanges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createDialogContents()
	 */
	@Override
	protected void createDialogContents() {
		// Shell
		GridLayout shellGridLayout = new GridLayout();
		getShell().setText(Messages.getString("ShowTraceInfoDialog.ShellName")); //$NON-NLS-1$
		composite.setLayout(shellGridLayout);

		// Text field
		GridData textGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		StyledText text = new StyledText(composite, SWT.MULTI | SWT.BORDER
				| SWT.WRAP | SWT.V_SCROLL);
		Font font = new Font(getShell().getDisplay(), new FontData(FONT,
				FONT_SIZE, SWT.NORMAL));
		text.setFont(font);
		text.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		text.setEditable(false);
		text.setText(contents);
		text.setLayoutData(textGridData);

		// Color header and data parts
		StyleRange[] ranges = new StyleRange[styleRanges.size()];
		for (int i = 0; i < styleRanges.size(); i++) {
			ranges[i] = styleRanges.get(i);
		}

		// Run this inside try catch to still show dialog if style ranges are
		// invalid
		try {
			text.setStyleRanges(ranges);
		} catch (Exception e) {
		}

		// Set help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				TraceViewerHelpContextIDs.TRACE_INFO_DIALOG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
	 */
	@Override
	protected void initializeBounds() {
		getShell().setSize(new Point(300, 360));
		super.initializeBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.dialog.BaseDialog#createActionListeners()
	 */
	@Override
	protected void createActionListeners() {
	}
}
