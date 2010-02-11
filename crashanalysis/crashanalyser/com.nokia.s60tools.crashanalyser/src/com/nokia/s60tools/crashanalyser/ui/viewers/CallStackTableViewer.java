/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
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
*/

package com.nokia.s60tools.crashanalyser.ui.viewers;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.menus.*;
import sun.awt.windows.*;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.resources.*;
import com.nokia.s60tools.crashanalyser.containers.*;
import com.nokia.s60tools.crashanalyser.ui.console.CrashAnalyserEditorConsole;
import com.nokia.s60tools.util.sourcecode.*;
import java.util.*;
import java.net.URI;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

/**
 * Table viewer for call stack table. Call stack table is used in 
 * SummaryPage.java (Crash Data page in Crash Visualiser editor) 
 *
 */
public class CallStackTableViewer extends CrashAnalyserTableViewer {
	Action actionCopySelectionToClipboardAsRichText;
	Action actionCopySelectionToClipboardAsPlainText;
	Action actionCopyStackToClipboardAsRichText;
	Action actionCopyStackToClipboardAsPlainText;
	Action actionOpenSourceFile;
	Action actionDoubleClick;
	Table tableControl;
	
	public final static int COLUMN_ADDRESS = 0;
	public final static int COLUMN_SYMBOL = 1;
	public final static int COLUMN_VALUE = 2;
	public final static int COLUMN_OFFSET = 3;
	public final static int COLUMN_OBJECT = 4;
	public final static int COLUMN_TEXT = 5;
	
	MenuManager subMenuSdk;

	/**
	 * Constructor
	 * @param table table where this viewer is used
	 */
	public CallStackTableViewer(Table table) {
		super(table);
		
		tableControl = table;

		doCreateActions();
		doCreateContextMenu();
		
		TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("");

		column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Symbol");
		
		column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Address");

		column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Offset");

		column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Object");

		column = new TableViewerColumn(this, SWT.NONE);
		column.setLabelProvider(labelProvider);
		column.getColumn().setText("Data");
		
		addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				actionDoubleClick.run();
			}
		});			
	}
	
	/**
	 * Creates pop-up menu actions
	 */
	private void doCreateActions() {
		actionCopySelectionToClipboardAsRichText = new Action("as Rich Text") {
			public void run() {
				copyToClipboard(tableControl, true, true);
				}
			};

		actionCopyStackToClipboardAsRichText = new Action("as Rich Text") {
			public void run() {
				copyToClipboard(tableControl, false, true);
				}
			};

		actionCopySelectionToClipboardAsPlainText = new Action("as Plain Text") {
			public void run() {
				copyToClipboard(tableControl, true, false);
				}
			};

		actionCopyStackToClipboardAsPlainText = new Action("as Plain Text") {
			public void run() {
				copyToClipboard(tableControl, false, false);
				}
			};

		actionOpenSourceFile = new Action("Open Source File") {
			public void run() {
				openSourceFile(tableControl);
			}
		};
		
		actionDoubleClick = new Action() {
			public void run() {
				openSourceFile(tableControl);
			}
		};
	}
	
	/**
	 * Setting the focus in opened file there where the method name occurs,
	 * must call after file is opened and only if opening was successful
	 * @param location
	 * @throws CoreException
	 */
	private static void setFocusToLineWhereMethodIs(
			final SourceFileLocation location) throws CoreException {
		//Runnable to open new file
		final IWorkspaceRunnable runSetFocus = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// do the actual work in here

				try {
					//Setting focus to correct line
					IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IEditorPart activeEditor = activePage.getActiveEditor();

					if(activeEditor != null && activeEditor instanceof TextEditor){
						
						// This is actually an instance of 'org.eclipse.cdt.internal.ui.editor.CEditor' 
						// that extends org.eclipse.ui.editors.text.TextEditor
						TextEditor editor = (TextEditor) activeEditor;
						IDocument doc =  getDocument(editor);
						if(doc != null){

							String text = doc.get();
							int methodOffset = location.getMethodOffset();
							if(methodOffset == SourceFileLocation.OFFSET_NOT_FOUND){
								// Removing parameters and getting new offset.
								String methodNameWithOutParams = location.getMethodName();
								methodNameWithOutParams = methodNameWithOutParams.substring(0, methodNameWithOutParams.indexOf("(")); //$NON-NLS-1$
								methodOffset = text.indexOf(methodNameWithOutParams);

								if(methodOffset == SourceFileLocation.OFFSET_NOT_FOUND){
									// Removing possible namespace and getting new offset.
									String separator = "::"; //$NON-NLS-1$
									int separatorLocation = methodNameWithOutParams.lastIndexOf(separator);
									if(separatorLocation > 0){
										methodNameWithOutParams = methodNameWithOutParams.substring(separatorLocation + separator.length());
										methodOffset = text.indexOf(methodNameWithOutParams);
									}
								}
							}
							
							editor.setHighlightRange(methodOffset, 0, true);
						}							
					}					

				} catch (Exception e) {
					e.printStackTrace();
					Status status = new Status(IStatus.ERROR,
							"com.nokia.s60tools.crashanalyser", 0, e //$NON-NLS-1$
									.getMessage(), e);

					throw new CoreException(status);
				} 

			}
		};
		
		
		ResourcesPlugin.getWorkspace().run(runSetFocus, null, IWorkspace.AVOID_UPDATE, null);
	}

	/**
	 * Returns the document interface for the currently active document 
	 * in the given editor.
	 * @param editor Editor to ask currently active document from. 
	 * @return Document interface if found, otherwise <code>null</code>.
	 */
	private static IDocument getDocument(TextEditor editor) {
		
		TextFileDocumentProvider  documentProvider = (TextFileDocumentProvider) editor.getDocumentProvider();
		if(documentProvider != null){
			return  documentProvider.getDocument(editor.getEditorInput());
			}								
		return null;
	}		
	
	/**
	 * Shows a message box with given message
	 * @param message
	 */
	private static void showMessage(String message, Shell shell) {
		MessageDialog.openInformation(
			shell,
			"Crash Analyser",
			message);
	}
	
	/**
	 * Tries to open source file for the selected line in the call stack table
	 * @param tableControl
	 */
	static void openSourceFile(Table tableControl) {
		try {
			if (tableControl.getSelection() != null &&
				tableControl.getSelection().length == 1) {
				StackEntry stackEntry = 
					(StackEntry)tableControl.getSelection()[0].getData();
				
				// we need code segment in order to be able to open source code,
				// if we don't find code segment, notify user
				if (stackEntry == null ||
					"".equals(stackEntry.getCodeSegmentName())) {
					showMessage("CODE SEGMENT NOT FOUND\n\nThe selected row does not contain information from which Code Segment (e.g. dll, exe) its address can be found. Source file cannot be opened.", tableControl.getShell());
					return;
				}
				
				String currentSdkName = SourceSdkManager.getCurrentSkdName();
				// user must first choose an active SDK. If it has not been chosen, notify user
				if (currentSdkName == null || "".equals(currentSdkName)) {
					showMessage("ACTIVE SDK NOT SELECTED.\n\n Please select Active SDK first by right-clicking in the Call Stack table.", tableControl.getShell());
					return;
				}
				
				String epocroot = SourceSdkManager.getEpocroot(currentSdkName);
				// user might have e.g. selected long ago an sdk which is not available anymore
				if (epocroot == null) {
					showMessage("INVALID ACTIVE SDK.\n\n Please select a valid Active SDK by right-clicking in the Call Stack table.", tableControl.getShell());
					return;
				}
				File epoc = new File(epocroot);
				
				// epocroot for the selected SDK if not valid, notify user
				if (!epoc.isDirectory() || !epoc.exists()) {
					String message = "INVALID EPOCROOT.\n\nThe EPOCROOT (" +
					 epocroot +
					 ") of selected Active SDK ("+
					 currentSdkName +
					 ") is not valid. Please check your SDK settings from Window > Preferences > Carbide.c++ > SDK Preferences, or select a different Active SDK by right-clicking in the Call Stack table.";
					 showMessage(message, tableControl.getShell());
					return;
				}
				// currentSdkName is e.g. 'SDK_Name armv5 urel', we need to get out, build and variant
				int lastSpace = currentSdkName.lastIndexOf(" ");
				int secondToLastSpace = currentSdkName.lastIndexOf(" ", lastSpace-1);
				
				String build = currentSdkName.substring(lastSpace+1, currentSdkName.length()); //e.g. urel
				String variant = currentSdkName.substring(secondToLastSpace+1, lastSpace);  // e.g. armv5
				
				String methodNameAsItsInMapFile = tableControl.getSelection()[0].getText(COLUMN_SYMBOL); 
				String dllName = stackEntry.getCodeSegmentName(); 

				ISourceFinder finder = SourceFinderFactory.createMapSourceFinder(CrashAnalyserEditorConsole.getInstance());

				SourceFileLocation location = 
					finder.findSourceFileByMethodName(methodNameAsItsInMapFile, dllName, variant, build, epocroot);

				File file = new File(location.getSourceFileLocation());
				// we could not find the source file, notify user
				if(file == null || !file.exists()){
					showMessage("SOURCE FILE CANNOT BE FOUND\n\n" +
								file.getName() +
								" was not found from " +
								FileOperations.getFolder(location.getSourceFileLocation()) +
								". Please make sure you have selected a correct Active SDK by right-clicking in the Call Stack table. It is also possible that you don't have all source files for your Active SDK present. Source file cannot be opened.", 
								tableControl.getShell());
					return;
				}
				
				//Create URI to open file
				String uriStr = location.getSourceFileLocation().replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
				uriStr = "file://" + uriStr; //$NON-NLS-1$
				final URI srcURI = new URI(uriStr);
				
				//Find default editor for that file
				IEditorRegistry reg = PlatformUI.getWorkbench().getEditorRegistry();
				
				IEditorDescriptor editor = reg.getDefaultEditor(file.getName());
				//We open editor by it's ID
				final String editorId = editor.getId();
				
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IEditorPart part = IDE.openEditor(page, srcURI, editorId, true);
					if(part != null){
						//Set focus to correct line
						setFocusToLineWhereMethodIs(location);
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			} 
		}catch (CannotFoundFileException ex) {
			String newLine = System.getProperty("line.separator");
			showMessage(ex.getMessage() + newLine + newLine + "Please check that you have selected correct Active SDK, by right-clicking in the Call Stack table.", tableControl.getShell());
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("Unable to find source file. Please check your SDK properties.", tableControl.getShell());
		}
	}

	/**
	 * Copies data from table into clipboard 
	 * @param table table from which data is copied from
	 * @param selection if true, only the selected rows are copied, if false, all rows are copied
	 * @param richText if true, data is copied in rich-text format, if false, data is copied in plain text format
	 */
	static void copyToClipboard(Table table, boolean selection, boolean richText) {
		TableItem[] items = null;
		// copy only selected rows
		if (selection)
			items = table.getSelection();
		// copy all rows
		else
			items = table.getItems();
		
		if (items != null && items.length > 0) {
			// rich-text format
			if (richText) {
				try {
					String data = HtmlFormatter.formatStackForClipboard(items, table.getColumnCount());
					byte[] bytes = convertToHTMLFormat(data);
					PEBClip clip = new PEBClip();
					clip.setData( WDataTransferer.CF_HTML, bytes);
				} catch (Exception e) {
				}
			// plain text format
			} else {
				String separator = System.getProperty("line.separator");
				String data = "";
				for (int i = 0; i < items.length; i++) {
					TableItem item = items[i];
					data += String.format("%-10s  %-10s  %-6s  %-6s  %s    %s", 
											item.getText(COLUMN_ADDRESS),
											item.getText(COLUMN_VALUE),
											item.getText(COLUMN_TEXT),
											item.getText(COLUMN_OFFSET),
											item.getText(COLUMN_OBJECT),
											item.getText(COLUMN_SYMBOL)) +
											separator;
				}
				StringSelection stringSelection = new StringSelection(data);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			}
		}
	}
	
	/**
	 * surrounds the html with this envelope, ready for
	 * windows clipboard (jdk support can be made better)
	 * <pre>
	 * Version:1.0
	 * StartHTML:00000000000
	 * EndHTML:00000000000
	 * StartFragment:00000000000
	 * EndFragment:00000000000
	 * &lt;!--StartFragment--&gt;
	 * ...
	 * &lt;!-- EndFragment-- &gt;
	 * </pre>
	 * We have to return a byte array 'cause in Windows the html needs to be utf-8
	 * encoded. And because we have to calculate char-offsets, we encode it here.
	 * @param htmlText
	 * @return byte[]
	 */
	static byte[] convertToHTMLFormat(String htmlText) {
		try {
			String sep = "\r\n";
			String header = "Version:1.0"+ sep +
										 "StartHTML:00000000000"+ sep +
										 "EndHTML:00000000000"+ sep +
										 "StartFragment:00000000000"+ sep +
										 "EndFragment:00000000000" + sep;
			
			String html = "<!--StartFragment-->\r\n" + htmlText + "<!--EndFragment-->\r\n";
	
			byte[] bHtml = html.getBytes("UTF-8");// encode first 'cause it may grow
	
			int headerLen = header.length();
			int htmlLen = bHtml.length;
	
			StringBuffer buf = new  StringBuffer(header);
			setValue( buf, "StartHTML", headerLen-1);
			setValue( buf, "EndHTML", headerLen + htmlLen-1);
			setValue( buf, "StartFragment", headerLen-1);
			setValue( buf, "EndFragment", headerLen + htmlLen-1);
			byte[] bHeader = buf.toString().getBytes("UTF-8");// should stay the same (no nonASCII chars in header)
	
			byte result[] = new byte[headerLen + htmlLen ];
			System.arraycopy(bHeader, 0, result, 0, bHeader.length);
			System.arraycopy(bHtml, 0, result, bHeader.length, bHtml.length);
	
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Replaces name+":00000000000" with name+":xxxxxxxxxxx" where xxx... is the '0' padded value.
	 * Value can't be to long, since maxint can be displayed with 11 digits. If value is below zero
	 * there is enough place (10 for the digits 1 for sign).<br>
	 * If the search is not found nothing is done.
	 * @param src
	 * @param name
	 * @param value
	 */
	private static void setValue( StringBuffer src, String name, int value){
		int val = value;
		String search = name+":00000000000";
		int pos = src.indexOf(search);
		if (pos ==-1) return;// not found, do nothing

		boolean belowZero = val<0;
		if (belowZero) val = -val;

		src.replace(pos+search.length()-(val+"").length(), pos+search.length(), val+"");
		if (belowZero) src.setCharAt(pos+name.length()+1,'-'); // +1 'cause of ':' in "SearchMe:"
	}
	
	/**
	 * creates context menu accordingly to what is selected in the table.
	 */
	private void doCreateContextMenu() {
		
		// Active SDK menu
		subMenuSdk = new MenuManager("Active SDK");	
		subMenuSdk.setRemoveAllWhenShown(true);
		subMenuSdk.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Map<String, String> sdks = SourceSdkManager.getAllSdks();
				String currentSdk = SourceSdkManager.getCurrentSkdName();
				// if there are sdks
				if (sdks != null && !sdks.isEmpty()) {
					String[] sdkNames = sdks.keySet().toArray(new String[sdks.size()]);
					java.util.Arrays.sort(sdkNames);
					// go through all found sdks and and them to pop-up menu
					for (int i = 0; i < sdkNames.length; i++) {
						String sdk = sdkNames[i];
						CommandContributionItemParameter p = 
							new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
																 null,
																 "com.nokia.s60tools.crashanalyser.commands.SdkSelection",
																 CommandContributionItem.STYLE_PUSH);
						p.label = sdk;
						// if this sdk is selected as Active sdk, draw an circular image to 
						// this menu item to indicate that this is the active sdk
						if (sdk.equalsIgnoreCase(currentSdk)) {
							p.icon = ImageResourceManager.getImageDescriptor((ImageKeys.SELECTED_SDK));
						}
						CommandContributionItem item = new CommandContributionItem(p);	    
				   	    subMenuSdk.add(item);
					}
				}
			}
		});

   	    final MenuManager subMenuSelection = new MenuManager("Copy Selection to Clipboard");
		subMenuSelection.add(actionCopySelectionToClipboardAsPlainText);
		subMenuSelection.add(actionCopySelectionToClipboardAsRichText);

		final MenuManager subMenuFull = new MenuManager("Copy Whole Stack to Clipboard");
		subMenuFull.add(actionCopyStackToClipboardAsPlainText);
		subMenuFull.add(actionCopyStackToClipboardAsRichText);

		MenuManager manager = new MenuManager("#PopupMenu");
		Menu menu = manager.createContextMenu(getControl());
		getControl().setMenu(menu);
		
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Table table = (Table)getControl();
				if (table.getSelection() != null) {
					// add 'Open source file' and 'Active SDK' menu items to pop-up menu
					// only if exactly one row is selected in table and if that
					// row's symbol column is not empty
					if (table.getSelection().length == 1 && 
						!"".equals(table.getSelection()[0].getText(COLUMN_SYMBOL))) {
						manager.add(actionOpenSourceFile);
						manager.add(subMenuSdk);
						manager.add(new Separator());
					}
					manager.add(subMenuSelection);
					manager.add(subMenuFull);
				}
			}
		});
	}
}

