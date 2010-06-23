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
 * ProgressBar Dialog class
 *
 */
package com.nokia.traceviewer.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.nokia.traceviewer.engine.TraceViewerDialog;

/**
 * ProgressBar Dialog class
 */
public final class ProgressBarDialog extends Dialog implements
		TraceViewerDialog {

	/**
	 * Indication that shell is disposed
	 */
	private static final String DISPOSED_STR = "*Disposed*"; //$NON-NLS-1$

	/**
	 * Font size
	 */
	private static final int FONT_SIZE = 10;

	/**
	 * Maximum length of the message
	 */
	private static final int MAX_LENGTH = 50;

	/**
	 * ProgressBar
	 */
	private ProgressBar progressBar;

	/**
	 * Text to be shown inside progressBar
	 */
	private final StringBuffer processText;

	/**
	 * Process reason to show inside progressBar
	 */
	private String processReason;

	/**
	 * Indicates that progressbar is already closing
	 */
	private boolean alreadyClosing;

	/**
	 * Forces close
	 */
	private boolean forceClose;

	/**
	 * Shell
	 */
	private Shell shell;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent shell
	 */
	public ProgressBarDialog(Shell parent) {
		// Pass the default styles here
		super(parent);
		processText = new StringBuffer(MAX_LENGTH);
	}

	/**
	 * This method opens the dialog shell
	 * 
	 * @param max
	 *            maximum number of traces
	 * @param processReason
	 *            the process reason
	 */
	public void open(int max, String processReason) {
		shell = new Shell(getParentShell(), getShellStyle());
		setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		Display display = getParentShell().getDisplay();

		// Close listener
		ShellListener listener = new ShellListener() {
			public void shellActivated(ShellEvent e) {
			}

			public void shellClosed(ShellEvent e) {
				Shell parent = getParentShell();
				if (!forceClose) {
					if (!alreadyClosing) {
						alreadyClosing = true;
						ProgressBarCloseHandler handler = new ProgressBarCloseHandler();

						boolean close = handler.progressBarClosed(progressBar
								.getMaximum());
						if (!close) {
							e.doit = false;
							alreadyClosing = false;
						}
					} else {
						e.doit = false;
					}
				} else {
					e.doit = true;
					forceClose = false;
				}

				// If progressbar is already closed, set parent visible
				if (shell.isDisposed() && !parent.isDisposed()
						&& !parent.getVisible()) {
					parent.setVisible(true);
				}
			}

			public void shellDeactivated(ShellEvent e) {
			}

			public void shellDeiconified(ShellEvent e) {
			}

			public void shellIconified(ShellEvent e) {
			}
		};

		shell.addShellListener(listener);

		createShell(max);

		// Set the reason
		this.processReason = processReason;
		createActionListeners();

		// Move the dialog to the center of the top level shell.
		Rectangle shellBounds = getParentShell().getBounds();
		Point dialogSize = shell.getSize();
		int middleX = shellBounds.x + ((shellBounds.width - dialogSize.x) / 2);
		int middleY = shellBounds.y + ((shellBounds.height - dialogSize.y) / 2);
		shell.setLocation(middleX, middleY);

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell.dispose();
	}

	/**
	 * This method initializes shell
	 * 
	 * @param max
	 *            maximum number of traces
	 */
	public void createShell(int max) {
		// Shell
		GridLayout shellGridLayout = new GridLayout();
		shellGridLayout.horizontalSpacing = 0;
		shellGridLayout.marginWidth = 0;
		shellGridLayout.marginHeight = 0;
		shellGridLayout.verticalSpacing = 0;
		shell.setText(Messages.getString("ProgressBarDialog.ShellTitle")); //$NON-NLS-1$
		shell.setLayout(shellGridLayout);
		shell.setSize(new Point(297, 54));

		// Progressbar
		GridData progressBarGridData = new GridData(SWT.FILL, SWT.FILL, true,
				true);
		progressBarGridData.heightHint = 22;
		progressBarGridData.widthHint = 295;
		progressBar = new ProgressBar(shell, SWT.SMOOTH);
		progressBar.setSelection(0);
		progressBar.setMaximum(max);
		progressBar.setLayoutData(progressBarGridData);
	}

	/**
	 * Gets progressbar
	 * 
	 * @return the progressbar
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * Closes the progressBar by bypassing the shellClosed listener
	 */
	@Override
	public boolean close() {
		forceClose = true;
		if (shell != null && !shell.isDisposed()) {
			shell.close();
		}
		return true;
	}

	/**
	 * Creates action listeners
	 */
	public void createActionListeners() {
		progressBar.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				// Count percents
				double number = (progressBar.getSelection() * 1.0
						/ (progressBar.getMaximum()) * 100);

				// Round to 2 decimals
				String numberString = String.format(
						"%.2f", Double.valueOf(number)); //$NON-NLS-1$

				processText.setLength(0);
				processText.append(processReason);
				processText.append(' ');
				processText.append(numberString);
				processText.append('%');

				Point point = progressBar.getSize();
				Font font = new Font(shell.getDisplay(), "Courier", FONT_SIZE, //$NON-NLS-1$
						SWT.BOLD);
				e.gc.setFont(font);
				e.gc.setForeground(shell.getDisplay().getSystemColor(
						SWT.COLOR_DARK_BLUE));

				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int stringWidth = fontMetrics.getAverageCharWidth()
						* processText.length();
				int stringHeight = fontMetrics.getHeight();

				e.gc.drawString(processText.toString(),
						(point.x - stringWidth) / 2,
						(point.y - stringHeight) / 2, true);
				font.dispose();
			}
		});
	}

	/**
	 * Update progressBar
	 * 
	 * @param num
	 *            selection value
	 * @return true if dialog was disposed, false otherwise
	 */
	public boolean updateProgressBar(final int num) {
		boolean isDisposed = true;
		if (shell != null && !shell.toString().contains(DISPOSED_STR)
				&& shell.getDisplay() != null) {
			isDisposed = false;
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (progressBar != null && !progressBar.isDisposed()) {
						progressBar.setSelection(num);
					}
				}
			});
		}
		return isDisposed;
	}

	/**
	 * Sets new max value
	 * 
	 * @param max
	 *            new max value
	 */
	public void setMax(final int max) {
		if (shell != null && !shell.toString().contains(DISPOSED_STR)
				&& shell.getDisplay() != null) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (progressBar != null && !progressBar.isDisposed()) {
						progressBar.setMaximum(max);
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.traceviewer.engine.TraceViewerDialog#openDialog()
	 */
	public void openDialog() {
		// Empty, opened with open()
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getShell()
	 */
	@Override
	public Shell getShell() {
		return shell;
	}
}
