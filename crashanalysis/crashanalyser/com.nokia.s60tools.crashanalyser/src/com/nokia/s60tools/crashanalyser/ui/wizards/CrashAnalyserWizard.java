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

package com.nokia.s60tools.crashanalyser.ui.wizards;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import com.nokia.s60tools.ui.wizards.*;
import com.nokia.s60tools.crashanalyser.model.*;
import com.nokia.s60tools.crashanalyser.plugin.CrashAnalyserPlugin;
import com.nokia.s60tools.crashanalyser.resources.*;
import com.nokia.s60tools.crashanalyser.ui.views.*;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.export.ICrashFileProvider;

import java.util.*;

/**
 * Crash Analyser Wizard is used for collecting required data from user so that CrashAnalyser.exe 
 * can be executed.
 *
 */
public final class CrashAnalyserWizard extends S60ToolsWizard {

	static private final ImageDescriptor bannerImgDescriptor = ImageResourceManager.getImageDescriptor(ImageKeys.WIZARD_BANNER);
	List<CrashFileBundle> filesToDecode = null;
	IWizardPage returnPage = null;
	ErrorLibrary errorLibrary;
	FileOrPathSelectionPage crashFileOrFolderSelectionPage = null;
	FilesSelectionPage filesSelectionPage = null;
	ParameterFilesPage parameterFilesPage = null;
	DecoderEngine engine;
	String userProvidedCrashFileOrFolder;
	boolean crashAnalyserExeExecutionSuccess = false;
	boolean readCrashFileFromDevice = false;
	String providedFileOrFolder = "";
	String errorMessage = "";
	String[] filesToBeSelectedOnSecondPage = null;
	final String EXTENSION_FILE_PROVIDER = "fileprovider"; //$NON-NLS-1$
	ICrashFileProvider fileProvider = null;
	/**
	 * Constructor
	 * @param filesToBeDecoded list of files to be decoded. If null is passed, wizard will start from the beginning. 
	 * If list contains files, then wizard is started from the 3rd page
	 * @param library error library
	 */
	public CrashAnalyserWizard(List<CrashFileBundle> filesToBeDecoded, ErrorLibrary library) {
		super(bannerImgDescriptor);
		engine = new DecoderEngine();
		filesToDecode = filesToBeDecoded;
		errorLibrary = library;
		readFileProvider();
	}
	
	/**
	 * Constructor (used in drag&drop cases)
	 * @param prefilledFileOrFolder single file or folder which is filled to 1st wizard page
	 * @param filesToSelect only these files should be shown in 2nd wizard page
	 * @param library error library
	 */
	public CrashAnalyserWizard(String prefilledFileOrFolder, String[] filesToSelect, ErrorLibrary library) {
		super(bannerImgDescriptor);
		engine = new DecoderEngine();
		filesToDecode = null;
		errorLibrary = library;
		providedFileOrFolder = prefilledFileOrFolder;
		filesToBeSelectedOnSecondPage = filesToSelect;
		readFileProvider();
	}
	
	public void init(IWorkbench arg0, IStructuredSelection arg1) {
		// No implementation needed
	}


	@Override
	public boolean needsProgressMonitor() {
		return true;
	}	
	
	@Override
	public void addPages() {
		// we are doing a full import, add all pages
		if (filesToDecode == null) {
			crashFileOrFolderSelectionPage = 
				new FileOrPathSelectionPage(engine, 
											errorLibrary, 
											providedFileOrFolder, 
											this,
											fileProvider);
			addPage(crashFileOrFolderSelectionPage);
			
			filesSelectionPage = new FilesSelectionPage(engine, filesToBeSelectedOnSecondPage);
			addPage(filesSelectionPage);
			
			parameterFilesPage = new ParameterFilesPage(engine, false);
			addPage(parameterFilesPage);
			
			setWindowTitle("Crash Analyser - Crash File Import Wizard");
			
			// files were drag&dropped, "press next automatically"
			if (!"".equals(providedFileOrFolder))
				moveToSecondPage();
						
		// we are re-decoding files provided by user, show only "3rd" page
		} else {
			parameterFilesPage = new ParameterFilesPage(engine, true);
			addPage(parameterFilesPage);
			
			engine.setCrashFiles(filesToDecode);
			
			setWindowTitle("Crash Analyser - Re-Decoding Selected Files");
		}
	}
	
	public void moveToSecondPageRequest(String[] showOnlyTheseFiles) {
		filesSelectionPage.showOnlyTheseFiles(showOnlyTheseFiles);
		moveToSecondPage();
	}
	
	public void moveToSecondPageRequest(boolean readFilesFromDevice) {
		readCrashFileFromDevice = readFilesFromDevice;
		moveToSecondPage();
	}
	
	/**
	 * When user has drag&dropped files, we "automatically press next" 
	 * in the first wizard page.
	 */
	void moveToSecondPage() {
		Runnable showWizardRunnable = new Runnable(){
			public void run(){
				IWizardPage nextPage = getNextPage(crashFileOrFolderSelectionPage);
				if (nextPage != null)
					getContainer().showPage(nextPage);
			}
		};
		
		Display.getDefault().asyncExec(showWizardRunnable);   		
	}
	
	
	@Override
	public boolean performFinish() {
		boolean fullImport = crashFileOrFolderSelectionPage != null;
		
		// full import is the case when wizard is started from page 1
		if (fullImport) {
			// if Finish was pressed in 2nd wizard page, symbol page (3rd page) was not used
			// false here would mean that only "decoded" files (.crashxml) files were selected.
			boolean symbolPageWasUsed = this.getContainer().getCurrentPage() != filesSelectionPage;
	
			DecodingData decodingData = new DecodingData();
			decodingData.importingFiles = true;
	
			// save 1st page
			crashFileOrFolderSelectionPage.saveUserEnteredData();

			decodingData.crashFileIndexes = filesSelectionPage.getFileIndexes();
			
			if (symbolPageWasUsed) {
				// save 3rd page
				parameterFilesPage.saveUserEnteredData();
				decodingData.symbolFiles = parameterFilesPage.getSymbolFiles();
				decodingData.mapFilesFolder = parameterFilesPage.getMapFilesFolder();
				decodingData.mapFilesZip = parameterFilesPage.getMapFilesZip();
				decodingData.imageFiles = parameterFilesPage.getImageFiles();
				decodingData.traceDictionaryFiles = parameterFilesPage.getTraceFiles();
			}
			
			decodingData.errorLibrary = errorLibrary;
			
			engine.setDecodingData(decodingData);
			
		// not full import is the case when files are Re-Decoded via MainView
		} else {
			DecodingData decodingData = new DecodingData();
			decodingData.importingFiles = false;

			// save 3rd page
			parameterFilesPage.saveUserEnteredData();
			decodingData.symbolFiles = parameterFilesPage.getSymbolFiles();
			decodingData.mapFilesFolder = parameterFilesPage.getMapFilesFolder();
			decodingData.mapFilesZip = parameterFilesPage.getMapFilesZip();
			decodingData.imageFiles = parameterFilesPage.getImageFiles();
			decodingData.traceDictionaryFiles = parameterFilesPage.getTraceFiles();
			
			decodingData.errorLibrary = errorLibrary;
			
			engine.setDecodingData(decodingData);
		}

		MainView mainView = MainView.showAndReturnYourself();
		if (mainView != null) {
			mainView.startDecoding(engine);
		}

		return true;
	}
	
	public boolean canFinish()
	{
		if (this.getContainer().getCurrentPage() == filesSelectionPage) {
			return filesSelectionPage.canFinish();
		} else if (this.getContainer().getCurrentPage() == parameterFilesPage) {
			return parameterFilesPage.canFinish();
		} else {
			return false;
		}
	}
	
	public IWizardPage getNextPage(IWizardPage page) {
		// We are moving from 1st page to 2nd page.
		// We need to run CrashAnalyser.exe to get summary data for crash files
		if(page.equals(crashFileOrFolderSelectionPage)){
			returnPage = null;
			userProvidedCrashFileOrFolder = crashFileOrFolderSelectionPage.getSelectedFileOrFolder();
			crashAnalyserExeExecutionSuccess = false;
			try {
				getContainer().run(true, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						errorMessage = "";
						// run CrashAnalyser.exe for summary information for selected file(s).
						if (readCrashFileFromDevice && fileProvider != null) {
							errorMessage = fileProvider.executeFunctionality(DecoderEngine.getTemporaryCrashFileFolder(true));
							userProvidedCrashFileOrFolder = DecoderEngine.getTemporaryCrashFileFolder(false);
						}
						crashAnalyserExeExecutionSuccess = 
							engine.processSummaryInfoForFiles(userProvidedCrashFileOrFolder, errorLibrary, monitor);
					}
				}
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// CrashAnalyser.exe execution was successful -> return next wizard page
			if (crashAnalyserExeExecutionSuccess) {
				crashFileOrFolderSelectionPage.errorInProcessingNextPress(false);
				filesSelectionPage.loadTable();
				returnPage = filesSelectionPage;
			// CrashAnalyser.exe execution was unsuccessful, don't proceed to next 
			// wizard page -> return current page
			} else {
				if (readCrashFileFromDevice) {
					if ("".equals(errorMessage)) {
						showMessage("The device did not contain any crash files");
					} else {
						showMessage("Could not read crash files. Please check PC Suite connection.\n\nError:\n" + errorMessage);
					}
				} else {
					crashFileOrFolderSelectionPage.errorInProcessingNextPress(true);
				}
				returnPage = crashFileOrFolderSelectionPage;
				filesSelectionPage.showOnlyTheseFiles(null);
			}			

			readCrashFileFromDevice = false;
			
			return returnPage;
		}
		// We are moving from 2nd page to 3rd/4th page.
		else if (page.equals(filesSelectionPage)) {
			// only (already "decoded") output.crashxml files have been selected in 2nd page.
			// Skip parameterFiles page and go to additionalSettingsPage, because we don't have
			// to query for symbols etc. because there is nothing to decode.
			return parameterFilesPage;
		}

		return null;
	}
	
	/**
	 * Shows a message box with given message
	 * @param message
	 */
	private void showMessage(String message) {
		MessageDialog.openInformation(
			getShell(),
			"Crash Analyser",
			message);
	}
	
	@Override
	public boolean performCancel() {
		DecoderEngine.getTemporaryCrashFileFolder(true);
		return super.performCancel();
	}

	/**
	 * Tries to find plugins which are File Providers. Selectes the first found
	 * File provider plugin.
	 */
	void readFileProvider() {
		try {
			IExtensionRegistry er = Platform.getExtensionRegistry();
			IExtensionPoint ep = 
				er.getExtensionPoint(CrashAnalyserPlugin.PLUGIN_ID, EXTENSION_FILE_PROVIDER);
			IExtension[] extensions = ep.getExtensions();
			
			// if plug-ins were found.
			if (extensions != null && extensions.length > 0) {
				
				// read all found trace providers
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] ce = extensions[i].getConfigurationElements();
					if (ce != null && ce.length > 0) {
						try {
							ICrashFileProvider provider = (ICrashFileProvider)ce[0].createExecutableExtension("class");
							// we support only one trace provider
							if (provider != null) {
								fileProvider = provider;
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
