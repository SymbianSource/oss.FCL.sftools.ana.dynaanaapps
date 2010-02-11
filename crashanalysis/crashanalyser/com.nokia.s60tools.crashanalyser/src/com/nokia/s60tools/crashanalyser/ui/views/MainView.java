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

package com.nokia.s60tools.crashanalyser.ui.views;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.swt.dnd.*;

import com.nokia.s60tools.ui.*;
import com.nokia.s60tools.util.resource.*;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.plugin.*;
import com.nokia.s60tools.crashanalyser.resources.*;
import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.ui.wizards.*;
import com.nokia.s60tools.crashanalyser.ui.dialogs.*;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.interfaces.*;
import java.io.*;
import java.util.*;

/**
 * Crash Analyser's main view class. Shows Crash files in a table. 
 * 
 * 
 */
public class MainView extends ViewPart implements IDecodingObserver, 
												  IErrorLibraryObserver,
												  INewCrashFilesObserver,
												  ISelectionChangedListener,
												  DropTargetListener {
	
	/**
	 * We can get view ID at runtime once the view is instantiated, but we
	 * also need static access to ID in order to be able to invoke the view.
	 */
	public static final String ID = "com.nokia.s60tools.crashanalyser.ui.views.MainView"; //$NON-NLS-1$
	private TableViewer tableViewerCrashFiles;
	private Action actionDoubleClick;
	private Action actionOpenWizard;
	private Action actionDecode;
	private Action actionDeleteFiles;
	private Action actionPanicLibrary;
	private Action actionExportToHtml;
	private Action actionExportToXml;
	private Action actionExportAsHtml;
	private Action actionExportAsXml;
	private Action actionExportAll;
	private String errorMessage = "";
	private Browser browserPanicDescription;
	private MainViewContentProvider contentProvider = null;
	private ErrorLibrary errorLibrary = null;
	private boolean mainViewLoaded = false;
	private boolean showWizard = false;
	private boolean wizardRunning = false;
	private static SummaryFile summaryFileFromTrace = null;
	private CrashAnalyserFile fileToBeShown = null;
	private CrashAnalyserWizard wizard = null;

	
	public void selectionChanged(SelectionChangedEvent arg0) {
		ISelection selection = tableViewerCrashFiles.getSelection();
		
		// no files selected, don't show description
		if (selection == null || selection.isEmpty()) {
			browserPanicDescription.setText("");
			return;
		}
		
		@SuppressWarnings("unchecked")
		Iterator<CrashFileBundle> i = ((IStructuredSelection)selection).iterator();
		while (i.hasNext()) {
			CrashFileBundle cFileBundle = i.next();
			// multiple files selected, don't show description
			if (i.hasNext()) {
				browserPanicDescription.setText("");
				break;
			// only one selected file, show description
			} else {
				browserPanicDescription.setText(HtmlFormatter.formatHtmlStyle(browserPanicDescription.getFont(), 
												cFileBundle.getDescription(true)));
			}
		}
	}

	/**
	 * Error library calls this method when it's ready to be used (i.e.
	 * it has finished reading in all errors & panics from xml files)
	 */
	public void errorLibraryReady() {
		contentProvider.setErrorLibrary(errorLibrary);
		actionPanicLibrary.setEnabled(true);
		refreshView();
	}
	
	/**
	 * MainView gets notified by this method to refresh the crash files table
	 */
	public void crashFilesUpdated() {
		mainViewLoaded = true;
		actionOpenWizard.setEnabled(true);		
		actionDecode.setEnabled(true);
		refreshView();
		if (showWizard) {
			showWizard = false;
			showWizardIfNoFiles();
		}
	}

	/**
	 * The constructor.
	 */
	public MainView() {
		// no implementation needed
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		SashForm sashFormMain = new SashForm(parent, SWT.HORIZONTAL);
		createCrashFilesListViewTableViewer(sashFormMain);
		createPanicsViewer(sashFormMain);
		sashFormMain.setWeights(new int[] {2,1});
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		actionOpenWizard.setEnabled(false);
		actionDecode.setEnabled(false);
		actionPanicLibrary.setEnabled(false);
		errorLibrary = ErrorLibrary.getInstance(this);
		
		try {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(tableViewerCrashFiles.getControl(),
				HelpContextIDs.CRASH_ANALYSER_HELP_MAIN_VIEW);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts Crash Analyser wizard if there are no decoded Crash 
	 * files in the MainView table.
	 *
	 */
	public void showWizardIfNoFiles() {
		Runnable showWizardRunnable = new Runnable(){
			public void run(){
				try {
					if (mainViewLoaded) {
						CrashFileBundle cFile = (CrashFileBundle)tableViewerCrashFiles.getElementAt(0);
						if (cFile.isEmpty()) {
							showWizard();
						}
					} else {
						showWizard = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		Display.getDefault().asyncExec(showWizardRunnable);   		
		
	}
	
	/**
	 * Starts Crash Analyser wizard asynchronously.
	 *
	 */
	public void showWizard() {
		showWizard(null);
	}

	/**
	 * Starts Crash Analyser wizard asynchronously.
	 * @param filesToBeDecoded list of files which needs to be decoded with wizard, null if wizard should
	 * be started from the beginning.
	 *
	 */
	public void showWizard(List<CrashFileBundle> filesToBeDecoded) {
		wizard = new CrashAnalyserWizard(filesToBeDecoded, errorLibrary);
		openWizard();
	}
	
	public void showWizard(String prefilledFileOrFolder, String[] filesToBeShown) {
		wizard = new CrashAnalyserWizard(prefilledFileOrFolder, filesToBeShown, errorLibrary);
		openWizard();
	}
	
	void openWizard() {
		Runnable showWizardRunnable = new Runnable(){
			public void run(){
				WizardDialog wizDialog;
				wizDialog = new WizardDialog(getViewSite().getShell(), wizard);
				wizDialog.create();		
				wizDialog.getShell().setSize(500, 650);		
				wizDialog.addPageChangedListener(wizard);				
				wizDialog.open();		
			}
		};
		
		Display.getDefault().asyncExec(showWizardRunnable);   		
	}
	
	/**
	 * Creates the MainView table which is used for showing crash files 
	 */
	private void createCrashFilesListViewTableViewer(Composite parent) {
		SashForm sashFormCrashFiles = new SashForm(parent, SWT.VERTICAL);
		
		List<S60ToolsTableColumnData> columnDataArr = new ArrayList<S60ToolsTableColumnData>();
		
		columnDataArr.add(new S60ToolsTableColumnData("Crash File", 690, 0));
		columnDataArr.add(new S60ToolsTableColumnData("Panic Code", 70, 0)); 
		columnDataArr.add(new S60ToolsTableColumnData("Panic Category", 90, 0)); 
		columnDataArr.add(new S60ToolsTableColumnData("Thread", 250, 0));
		columnDataArr.add(new S60ToolsTableColumnData("Time", 130, 0));
		
		S60ToolsTableColumnData[] arr 
				= columnDataArr.toArray(new S60ToolsTableColumnData[columnDataArr.size()]);
		
		S60ToolsTable tbl = S60ToolsTableFactory.create(sashFormCrashFiles, arr);
		
		TableViewer tblViewer = new TableViewer(tbl.getTableInstance());
		tbl.setHostingViewer(tblViewer);
		tblViewer.addDropSupport(DND.DROP_COPY, new Transfer[] {FileTransfer.getInstance()}, this);

		contentProvider = new MainViewContentProvider(this);
		tblViewer.setContentProvider(contentProvider);
		tblViewer.setLabelProvider(new MainViewLabelProvider());
		tblViewer.setSorter(new ViewerSorter());
		tblViewer.setInput(getViewSite());
		tblViewer.addSelectionChangedListener(this);
		
		tableViewerCrashFiles = tblViewer;
	}
	
	/**
	 * Creates the right side view of the MainView. Contains a browser which
	 * is used for showing information about a selected file in MainView table. 
	 */
	private void createPanicsViewer(Composite parent) {
		SashForm sashFormPanics = new SashForm(parent, SWT.HORIZONTAL);
		
		SashForm sashFormPanicLookup = new SashForm(sashFormPanics, SWT.VERTICAL);

		Composite composite = new Composite(sashFormPanicLookup, SWT.NONE);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);		

		browserPanicDescription = new Browser(composite, SWT.BORDER);
		browserPanicDescription.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	/**
	 * Initialize double-click action
	 */
	private void hookDoubleClickAction() {
		tableViewerCrashFiles.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				actionDoubleClick.run();
			}
		});
	}	

	/**
	 * Initialize context menu
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MainView.this.fillContextMenu(manager);
				
			}
		});
		Menu menu = menuMgr.createContextMenu(tableViewerCrashFiles.getControl());
		tableViewerCrashFiles.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewerCrashFiles);
	}

	/**
	 * Action bars initializer
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fill pull down menu
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionOpenWizard);
		manager.add(actionDecode);
		manager.add(actionDeleteFiles);
		manager.add(actionPanicLibrary);
		MenuManager exportMenu = getExportMenu(true);
		if (exportMenu != null)
			manager.add(exportMenu);
	}

	/**
	 * Fills context menu
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionDecode);
		manager.add(actionDeleteFiles);
		MenuManager exportMenu = getExportMenu(false);
		if (exportMenu != null)
			manager.add(exportMenu);
	}
	
	/**
	 * Fills tool bar
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionOpenWizard);
		manager.add(actionDecode);
		manager.add(actionDeleteFiles);
		manager.add(actionPanicLibrary);
	}
	
	public void dispose() {
		EditorHandler.closeAllEditors();
	}
	
	/**
	 * Checks based on which files are selected in MainView table, that
	 * can Export sub-menu be shown.
	 * @return Export menu if it can be show, null if it can't
	 */
	private MenuManager getExportMenu(boolean showAlways) {
		MenuManager subMenuExport = new MenuManager("Export");
		
		ISelection selection = tableViewerCrashFiles.getSelection();
		// if no files are selected, don't show export menu
		if (!showAlways && (selection == null || selection.isEmpty()))
			return null;
		
		boolean allFilesContainsXml = true;
		boolean itemsAdded = false;
		
        // go through all selected files
    	@SuppressWarnings("unchecked")
		Iterator<CrashFileBundle> i = ((IStructuredSelection)selection).iterator();
		while (i.hasNext()) {
			CrashFileBundle cFileBundle = i.next();

			// if only one item selected, allow export to html and xml if it contains xml file
			if (((IStructuredSelection)selection).size() == 1 && cFileBundle.hasXml()) {
				subMenuExport.add(actionExportToHtml);
				subMenuExport.add(actionExportToXml);
				itemsAdded = true;
			}
			
			if (!cFileBundle.hasFiles())
				allFilesContainsXml = false;
			
			// do not show Export menu if "empty file" or emulator panic is selected
			if (cFileBundle.isEmpty() || cFileBundle.isEmulatorPanic())
				return null;
		}
		
		if (showAlways && !itemsAdded) {
			subMenuExport.add(actionExportToHtml);
			subMenuExport.add(actionExportToXml);
		}
		
		// if all selected files contains xml file, these actions can be added
		if (allFilesContainsXml || showAlways) {
			subMenuExport.add(actionExportAsHtml);
			subMenuExport.add(actionExportAsXml);
		}
		
		// export all can always be added, since they will export
		// all files which are available.
		subMenuExport.add(actionExportAll);
		
		return subMenuExport;
	}

	/**
	 * Make all actions (buttons, double-click)
	 */
	private void makeActions() {
		makeDeleteFilesAction();
		makeOpenWizardAction();
		makeOpenPanicLibraryAction();	
		makeExportToHtml();
		makeExportToXml();
		makeExportAllAction();
		makeExportAsHtmlAction();
		makeExportAsXmlAction();
		makeDoubleClickAction();
		makeDecodeFilesAction();
	}
	
	/**
	 * Double-click action
	 */
	private void makeDoubleClickAction() {
		// when table item is double-clicked
		actionDoubleClick = new Action() {
			public void run() {
				try {
					if (wizardRunning)
						return;
					ISelection selection = tableViewerCrashFiles.getSelection();
					Object obj = ((IStructuredSelection)selection).getFirstElement();
					CrashFileBundle cFile = (CrashFileBundle)obj;
					// if empty file is double-clicked, open wizard
					if (cFile.isEmpty()) {
						showWizard();
					// Crash file (not empty file) is double-clicked
					} else {
						// fully decoded
						if (cFile.isFullyDecoded())
							EditorHandler.openCrashAnalyserEditor(cFile.getCrashFile());
						// partially decoded
						else if (cFile.isPartiallyDecoded())
							EditorHandler.openCrashAnalyserEditor(cFile.getSummaryFile());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * Export selected file to html file
	 */
	private void makeExportToHtml() {
		actionExportToHtml = new Action() {
			public void run() {
				ISelection selection = tableViewerCrashFiles.getSelection();
				FileExportManager.ExportSelectedFileToHtml(selection, getShell());
			}
		};
		actionExportToHtml.setText("to HTML File");
	}

	/**
	 * Export selected file to .xml or .crashxml
	 */
	private void makeExportToXml() {
		actionExportToXml = new Action() {
			public void run() {
				ISelection selection = tableViewerCrashFiles.getSelection();
				FileExportManager.ExportSelectedFileToXml(selection, getShell());
			}
		};
		actionExportToXml.setText("to XML File");
	}

	/**
	 * Export to Zip as Xml
	 */
	private void makeExportAsXmlAction() {
		actionExportAsXml = new Action() {
			public void run() {
				ISelection selection = tableViewerCrashFiles.getSelection();
				FileExportManager.ExportSelectedFilesAsXmlToZip(selection, getShell());
			}
		};
		actionExportAsXml.setText("to Zip as XML");
	}

	/**
	 * Export to Zip as Html
	 */
	private void makeExportAsHtmlAction() {
		actionExportAsHtml = new Action() {
			public void run() {
				ISelection selection = tableViewerCrashFiles.getSelection();
				FileExportManager.ExportSelectedFilesAsHtmlToZip(selection, getShell());
			}
		};
		actionExportAsHtml.setText("to Zip as HTML");
	}

	/**
	 * Export all formats to zip
	 */
	private void makeExportAllAction() {
		actionExportAll = new Action() {
			public void run() {
				ISelection selection = tableViewerCrashFiles.getSelection();
				FileExportManager.ExportSelectedFilesToZipInAllFormats(selection, getShell());
			}
		};
		actionExportAll.setText("All Formats to Zip");
	}

	/**
	 * Open Error Library button
	 */
	private void makeOpenPanicLibraryAction() {
		actionPanicLibrary = new Action() {
			public void run() {
				ErrorLibraryDialog dlg = new ErrorLibraryDialog(tableViewerCrashFiles.getControl().getShell(), errorLibrary);
				dlg.open();
			}
		};
		actionPanicLibrary.setText("Open Error Library...");
		actionPanicLibrary.setToolTipText("Open Error Library Window");
		actionPanicLibrary.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.ERROR_LIBRARY));
	}

	/**
	 * Import Files button
	 */
	private void makeOpenWizardAction() {
		// Open wizard button
		actionOpenWizard = new Action() {
			public void run() {
				showWizard();
			}
		};
		actionOpenWizard.setText("Import Files...");
		actionOpenWizard.setToolTipText("Open File Import Wizard");
		actionOpenWizard.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.IMG_APP_ICON));		
	}
	
	/**
	 * Starts decoding process for the given file by opening the wizard for given file.
	 * @param f file to be decoded
	 */
	public void decodeFile(SummaryFile f) {
		int sep = f.getFilePath().lastIndexOf(File.separator);
		CrashFileBundle cfb = new CrashFileBundle(f.getFilePath().substring(0, sep), errorLibrary);
		List<CrashFileBundle> files = new ArrayList<CrashFileBundle>();
		files.add(cfb);
		EditorHandler.closeEditors(files);
		showWizard(files);
	}
	
	/**
	 * Decode files button
	 */
	private void makeDecodeFilesAction() {
		actionDecode = new Action() {
			public void run() {
				ISelection selection = tableViewerCrashFiles.getSelection();
				// if nothing is selected, just ignore button press
				if (selection == null || selection.isEmpty())
					return;

				String romId = "";
				// go through all files and check that they can be re-decoded
				List<CrashFileBundle> files = new ArrayList<CrashFileBundle>();
				@SuppressWarnings("unchecked")
				Iterator i = ((IStructuredSelection)selection).iterator();
				while (i.hasNext()) {
					CrashFileBundle cFileBundle = (CrashFileBundle)i.next();
					UndecodedFile udf = cFileBundle.getUndecodedFile();
					// must have original binaries to be able to re-decode
					if (udf == null) {
						errorMessage = "All selected files do not have original binaries available. Cannot Decode.";
						showErrorMessage();
						return;
					}
					
					// only mobilecrash files can be re-decoded (e.g. D_EXC files can't)
					if (!udf.getFileName().toLowerCase().endsWith("."+CrashAnalyserFile.MOBILECRASH_FILE_EXTENSION) &&
						!udf.getFileName().toLowerCase().endsWith("."+CrashAnalyserFile.TRACE_EXTENSION)) {
						errorMessage = "Only MobileCrash files can be Decoded.";
						showErrorMessage();
						return;
					}
					
					// rom id hasn't yet been read
					if ("".equals(romId)) {
						romId = cFileBundle.getRomId();
					// rom ids of selected files must match
					} else if (!romId.equalsIgnoreCase(cFileBundle.getRomId())) {
						errorMessage = "Select only files which have same ROM IDs";
						showErrorMessage();
						return;
					}
					
					files.add(cFileBundle);
				}
				
				if (!files.isEmpty()) {
					EditorHandler.closeEditors(files);
					showWizard(files);
				}
			}
		};
		actionDecode.setText("Decode Files...");
		actionDecode.setToolTipText("Decode Selected Files");
		actionDecode.setImageDescriptor(ImageResourceManager.getImageDescriptor(ImageKeys.DECODE_FILES));
	}

	/**
	 * Delete Files button
	 */
	private void makeDeleteFilesAction() {
		// Delete file button
		actionDeleteFiles = new Action() {
			public void run() {
				ISelection selection = tableViewerCrashFiles.getSelection();
				if (selection == null || selection.isEmpty())
					return;
				
				// Confirm file delete
				MessageBox messageBox = new MessageBox(tableViewerCrashFiles.getControl().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		        messageBox.setText("Crash Analyser - Delete Files");
		        messageBox.setMessage("Are you sure you want to delete selected files?");
		        int buttonID = messageBox.open();
		        if (buttonID == SWT.YES) {
			        // go through all selected files and remove them
		        	List<CrashFileBundle> closeFiles = new ArrayList<CrashFileBundle>();
		        	@SuppressWarnings("unchecked")
					Iterator<CrashFileBundle> i = ((IStructuredSelection)selection).iterator();
					while (i.hasNext()) {
						CrashFileBundle cFileBundle = i.next();
						if (!cFileBundle.isEmpty()) {
							cFileBundle.delete();
							closeFiles.add(cFileBundle);
						}
					}
					EditorHandler.closeEditors(closeFiles);
					contentProvider.refresh();
				}

		        // All items were removed, add empty item
				if (tableViewerCrashFiles.getTable().getItemCount() == 0) {
					CrashFileBundle empty = new CrashFileBundle(true);
					tableViewerCrashFiles.add(empty);
				}
			}
		};
		actionDeleteFiles.setText("Delete Files");
		actionDeleteFiles.setToolTipText("Delete Selected Files");
		actionDeleteFiles.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
	}

	/**
	 * Shows a message box with given message
	 * @param message
	 */
	private void showMessage(String message) {
		MessageDialog.openInformation(
			tableViewerCrashFiles.getControl().getShell(),
			"Crash Analyser",
			message);
	}
	
	/**
	 * Shows an error message asynchronously.
	 *
	 */
	private void showErrorMessage() {
		Runnable showErrorMessageRunnable = new Runnable(){
			public void run(){
				MessageDialog.openError(
						tableViewerCrashFiles.getControl().getShell(),
						"Crash Analyser",
						errorMessage);
				errorMessage = ""; //$NON-NLS-1$
			}
		};
		
		// Has to be done in its own thread
		// in order not to cause invalid thread access
		Display.getDefault().asyncExec(showErrorMessageRunnable);        		
	}
	
	/**
	 * Returns a shell
	 * @return a shell
	 */
	private Shell getShell() {
		return tableViewerCrashFiles.getControl().getShell();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		tableViewerCrashFiles.getControl().setFocus();
	}
	
	/**
	 * When wizard is finished, wizard passes decoder engine to MainView so that 
	 * MainView can start decoding process and register itself as the progress listener.
	 * @param decoder decoder engine
	 */
	public void startDecoding(DecoderEngine decoder) {
		disableUIForImport(true);
		try {
			decoder.decode(this);
		} catch (Exception e) {
			disableUIForImport(false);
			showMessage("Processing Failed: "+e.getMessage());
			setMainViewVisible();
		}
	}
	
	/**
	 * While import is in progess, some actions should be disabled. This method
	 * enables/disables necessary actions after/before import.
	 * @param disable
	 */
	void disableUIForImport(boolean disable) {
		actionDecode.setEnabled(!disable);
		actionOpenWizard.setEnabled(!disable);
		actionDeleteFiles.setEnabled(!disable);
		wizardRunning = disable;
	}
	
	/**
	 * Makes Crash Analyser view visible asynchronously.
	 *
	 */
	private void setMainViewVisible() {
		Runnable decodingFinishedRunnable = new Runnable(){
			public void run(){
				updateView();
				if (fileToBeShown != null) {
					if (fileToBeShown instanceof CrashFile) {
						EditorHandler.openCrashAnalyserEditor((CrashFile)fileToBeShown);
					} else if (fileToBeShown instanceof SummaryFile) {
						EditorHandler.openCrashAnalyserEditor((SummaryFile)fileToBeShown);
					}
					fileToBeShown = null;
				}
			}
		};
		
		// Has to be done in its own thread
		// in order not to cause invalid thread access
		Display.getDefault().asyncExec(decodingFinishedRunnable);        		
	}
	
	/**
	 * Refreshes view asynchronously.
	 */
	private void refreshView() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				tableViewerCrashFiles.refresh();
			}
		};
		
		// Has to be done in its own thread
		// in order not to cause invalid thread access
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	/**
	 * Reloads Crash files to table
	 *
	 */
	private void updateView() {
		try {
			getViewSite().getPage().showView(MainView.ID);
			tableViewerCrashFiles.refresh();
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}

	/**
	 * Used by Decoder Engine to inform when decoding is finished.
	 * @param error error message if something went wrong with decoding. Empty if no error occurred
	 */
	public void decodingFinished(String error, CrashAnalyserFile caFile) {
		disableUIForImport(false);
		// no errors while decoding
		if ("".equals(error)) { //$NON-NLS-1$
			contentProvider.refresh();
			setMainViewVisible();
			fileToBeShown = caFile;
		// there were errors in decoding process
		} else {
			errorMessage = error;
			showErrorMessage();
			contentProvider.refresh();
		}
	}
	
	/**
	 * Refreshes table
	 */
	public void refresh() {
		contentProvider.refresh();
		tableViewerCrashFiles.refresh();
	}
	
	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}

	public void dragLeave(DropTargetEvent event) {
		// Nothing to be done
	}

	public void dragOperationChanged(DropTargetEvent event) {
		// Nothing to be done
	}

	public void dragOver(DropTargetEvent event) {
		event.feedback = DND.FEEDBACK_NONE;
	}

	public void drop(DropTargetEvent event) {
		// we accept only file drops
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
			if (event.data != null) {
				String[] files = (String[])event.data;
				executeDrop(files);
			}
		}
	}

	public void dropAccept(DropTargetEvent event) {
		// Nothing to be done
	}
	
	/**
	 * Executes drop functionality when files are drag&dropped to main view.
	 * Checks if supported files where dropped and starts wizard accordingly.
	 * @param files drag&dropped files (paths)
	 */
	void executeDrop(String[] files) {
		// just one (supported) file dropped
		if (files.length == 1 && DecoderEngine.isFileValidCrashFile(files[0])) {
			showWizard(files[0], files);
		// multiple files dropped
		} else if (files.length > 1) {
			String path = "";
			// go through all dropped files, and check that they are from same folder
			// (wizard doesn't know how to handle multiple files from multiple locations)
			for (int i = 0; i < files.length; i++) {
				if (DecoderEngine.isFileValidCrashFile(files[i])) {
					if ("".equals(path)) {
						path = FileOperations.getFolder(files[i]);
					} else if (!FileOperations.getFolder(files[i]).equalsIgnoreCase(path)){
						showMessage("Multiple files from different folders are not supported");
						break;
					}
				} else {
					showMessage("Unsupported file type");
					break;
				}
			}
			showWizard(path, files);
		} else {
			showMessage("Unsupported file type");
		}
	}

    /**
     * Opens MainView if it is not open. Can be called from a non-UI thread.
     */
    public static void showOrRefresh() {
    	
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MainView mv = showAndReturnYourself(true);
				mv.refresh();
			}
		}
		);
    }
    
    /**
     * Opens MainView if it is not open. Can be called from a non-UI thread.
     */
    public static void showOrRefreshAndOpenFile(SummaryFile summaryFile) {
    	MainView.summaryFileFromTrace = summaryFile;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MainView mv = showAndReturnYourself(true);
				mv.refresh();
				EditorHandler.openCrashAnalyserEditor(summaryFileFromTrace);
			}
		}
		);
    }

    /**
     * Makes main view visible and returns an instance of itself. Can be called from
     * an UI thread only.
     * @return instance of main view
     */
    public static MainView showAndReturnYourself() {
    	return showAndReturnYourself(false);
    }
    
    /**
     * Makes main view visible and returns an instance of itself. Can be called from
     * an UI thread only.
     * @return instance of main view
     */
	public static MainView showAndReturnYourself(boolean openOnly) {
    	try {
    		
    		IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    		if (ww == null)
    			return null;
    		
    		IWorkbenchPage page = ww.getActivePage();
    		IWorkbenchPart currentPart = page.getActivePart();
    		
    		// Checking if view is already open
    		IViewReference[] viewRefs = page.getViewReferences();
    		for (int i = 0; i < viewRefs.length; i++) {
				IViewReference reference = viewRefs[i];
				String id = reference.getId();
				if(id.equalsIgnoreCase(MainView.ID)){
					// Found, restoring the view
					IViewPart viewPart = reference.getView(true);
					if (!openOnly)
						page.activate(viewPart);
					return (MainView)viewPart;
				}
			}
    		
    		// View was not found, opening it up as a new view.
    		MainView mView = (MainView)page.showView(MainView.ID);
    		if (openOnly)
    			page.bringToTop(currentPart);
    		return mView;
    		
    	} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
}