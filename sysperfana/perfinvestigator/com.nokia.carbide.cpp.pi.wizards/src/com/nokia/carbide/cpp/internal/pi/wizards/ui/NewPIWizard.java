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

package com.nokia.carbide.cpp.internal.pi.wizards.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.carbide.cdt.builder.builder.CarbideCPPBuilder;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ISISBuilderInfo;
import com.nokia.carbide.cpp.internal.api.sdk.SymbianBuildContext;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.IPkgEntry;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.RofsObySymbolPair;
import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.ButtonPlugin;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;
import com.nokia.carbide.cpp.pi.wizards.WizardsPlugin;
import com.nokia.carbide.cpp.sdk.core.ISymbianBuildContext;
import com.nokia.carbide.cpp.ui.CarbideUIPlugin;
import com.nokia.carbide.cpp.ui.ICarbideSharedImages;
import com.nokia.cpp.internal.api.utils.core.Check;
import com.nokia.cpp.internal.api.utils.core.FileUtils;

public class NewPIWizard extends Wizard implements IImportWizard, INewWizard{
	
	//	The shared instance.
	private static NewPIWizard wizard;
	
	static final int PROGRESS_WORK_TOTAL = 10000; // handy total for progress
													// indicator
	static final String DIALOG_SETTING_FILE = WizardsPlugin.getDefault().getStateLocation().append("dialog_settings.xml").toOSString();	//$NON-NLS-1$
	
	private NewPIWizardPageInputTask pageInput;
	private NewPIWizardPageConfigSelectorTask pageConfigSelector;
	private NewPIWizardPagePkgListTask pagePkgList;
	@SuppressWarnings("unused") //$NON-NLS-1$
	private NewPIWizardPageRomSdkSubTask pageRomPkgSdk;
	private NewPIWizardPageRomSubTask pageObySym;
	private NewPIWizardPageCustomTask pageCustom;
	private NewPIWizardPageBupMapTask pageBupMap;
	private NewPIWizardPageOutputTask pageOutput;
	private ArrayList<IFile> tmpPkgList = new ArrayList<IFile>();
	private Set<Integer> traceSet;
	
	protected IProject piProject;
	IWorkbench workbench;
	
	NewPIWizardSettings wizardSettings = NewPIWizardSettings.getInstance();

	/**
	 * Constructor for NewPIWizard.
	 */
	public NewPIWizard() {
		super();
		wizard = this;
		setNeedsProgressMonitor(true);
		ICarbideSharedImages carbideImages = CarbideUIPlugin.getDefault().getSharedImages();
		setDefaultPageImageDescriptor(carbideImages.getImageDescriptor(carbideImages.IMG_PI_IMPORT_WIZARD_BANNER_75_66));
		setWindowTitle(Messages.getString("NewPIWizard.window.title")); //$NON-NLS-1$

		//DialogSettings dialogSettings = (DialogSettings) WizardsPlugin.getDefault().getDialogSettings();
		IDialogSettings dialogSettings = new DialogSettings("import_wizard");	//$NON-NLS-1$
		try {
			dialogSettings.load(DIALOG_SETTING_FILE);
		} catch (IOException e) {
			// saving last session is not that important
			//e.printStackTrace();
		}

		wizardSettings.clear();
		wizardSettings.restoreState(dialogSettings);
		setDialogSettings(dialogSettings);
	}
	
	static public NewPIWizard getDefault() {
		return wizard;
	}
	
	/**
	 * Adding the pages to the wizard.
	 */

	public void addPages() {
		addPage(pageInput = new NewPIWizardPageInputTask(this));
		addPage(pageConfigSelector = new NewPIWizardPageConfigSelectorTask());
		addPage(pagePkgList = new NewPIWizardPagePkgListTask());
		// merge SDK to symbol page
		//addPage(pageRomPkgSdk = new NewPIWizardPageRomSdkSubTask());
		addPage(pageObySym = new NewPIWizardPageRomSubTask());
		addPage(pageBupMap = new NewPIWizardPageBupMapTask());
		addPage(pageCustom = new NewPIWizardPageCustomTask(this));
		addPage(pageOutput = new NewPIWizardPageOutputTask());		
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		// flush out all settings for very page, so we can hit finish
		IWizardPage[] pages = getPages();
		for (IWizardPage page: pages) {
			if (page instanceof INewPIWizardSettings) {
				((INewPIWizardSettings)page).setupPageFromFromNewPIWizardSettings();
			}
		}

	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		pageOutput.regenerateOutputIfIntputChanged(); // this must stay out of asyncExec() since it require treeviewer selection
		Display.getDefault().asyncExec( new Runnable() {
			public void run () {
				//DialogSettings dialogSettings = (DialogSettings) WizardsPlugin.getDefault().getDialogSettings();
				IDialogSettings dialogSettings = getDialogSettings();
				if (dialogSettings != null) {
					NewPIWizardSettings.getInstance().saveState(dialogSettings);
					setDialogSettings(dialogSettings);
					try {
						dialogSettings.save(DIALOG_SETTING_FILE);
					} catch (IOException e) {
						// saving last session is not that important
						e.printStackTrace();
					}
				}
				createNewProject();
			
			}
		});
		
		return true;
	}
	
	private IFile createTempPkgFile() {
		try {
			java.io.File prepocessedPkgFile;
			prepocessedPkgFile = java.io.File.createTempFile("importpi", ".pkg", new java.io.File(wizardSettings.outputContainer.getLocation().toString()));	//$NON-NLS-1$ //$NON-NLS-2$
			prepocessedPkgFile.deleteOnExit();
			// force eclipse be be aware of the added file from the IFile refresh
			wizardSettings.outputContainer.refreshLocal(0, null);
			IFile tmpPkgFile = FileUtils.convertFileToIFile(wizardSettings.outputContainer, prepocessedPkgFile);
			if (tmpPkgFile == null) {
				Check.reportFailure(prepocessedPkgFile.getAbsolutePath() + Messages.getString("NewPIWizard.failed.convertFileToIFile"), new Throwable()); //$NON-NLS-1$
				return null;
			}
			tmpPkgList.add(tmpPkgFile);
			return tmpPkgFile;
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void cleanTempPkgFile() {
		for(IFile file : tmpPkgList) {
			// just to delete using java.io as well for consistence
			java.io.File javaFile = new java.io.File(file.getLocation().toString());
			javaFile.delete();
			try {
				// force eclipse be be aware of the removed file from the IFile refresh
				file.refreshLocal(0, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		tmpPkgList.clear();
	}

	private void createNewProject() {
		SampleImporter sampleImporter = SampleImporter.getInstance();
		NewPIWizardSettings wizardSettings = NewPIWizardSettings.getInstance();
		Map<Object, IFile> preprocessedMap = new HashMap<Object, IFile>();
		sampleImporter.clear();
		
		sampleImporter.setDatFileName(wizardSettings.sampleFileName);
		String container = wizardSettings.outputContainer.getFullPath().toString();
		if (container.startsWith("/")) //$NON-NLS-1$
			container = container.substring(1, container.length());
		sampleImporter.setProjectName(container);
		sampleImporter.setPiFileName(wizardSettings.piFileName);
		if (wizardSettings.haveRomOnly || wizardSettings.haveAppRom) {
			if (wizardSettings.romSdk != null && wizardSettings.romSdk.getEPOCROOT() != null) {
				sampleImporter.setRomEpocroot(wizardSettings.romSdk.getEPOCROOT());
			}
			if (wizardSettings.romSymbolFile != null) {
				sampleImporter.setRomSymbolFile(wizardSettings.romSymbolFile);
			}
			if (wizardSettings.romObyFile != null) {
				sampleImporter.setRomObyFile(wizardSettings.romObyFile);
			}
			sampleImporter.clearRofsObyFileList();
			sampleImporter.clearRofsSymbolFileList();
			for (RofsObySymbolPair pair : wizardSettings.rofsObySymbolPairList) {
				sampleImporter.addRofsObyFile(pair.getObyFile());
				sampleImporter.addRofsSymbolFile(pair.getSymbolFile());
			}
		}
		if (wizardSettings.haveAppOnly || wizardSettings.haveAppRom) {
			for (IPkgEntry entry :wizardSettings.selectedAppFileList) {
				java.io.File javaFile = new java.io.File(entry.getPkgFile());
				if (javaFile.exists()) {
					IFile tmpPkgFile = createTempPkgFile();
					if (tmpPkgFile == null) {
						Check.reportFailure(tmpPkgFile.getLocation().toString() + Messages.getString("NewPIWizard.failed.convertFileToIFile"), new Throwable()); //$NON-NLS-1$
						return;
					}
					{
						try {
							char[] pkgFileBuf = FileUtils.readFileContents(javaFile, null);
							String pkgFileStr = new String(pkgFileBuf);
							FileUtils.writeFileContents(new java.io.File(tmpPkgFile.getLocation().toString()), pkgFileStr.toCharArray(), null);
						} catch (CoreException e) {
							e.printStackTrace();
						}
					}
					preprocessedMap.put(entry, tmpPkgFile);
				}
			}
			for (ICarbideBuildConfiguration config : wizardSettings.selectedBuildConfigList) {
				ISymbianBuildContext context = SymbianBuildContext.getBuildContextFromDisplayName(config.getDisplayString());
				IFile tmpPkgFile;
				
				
				List<ISISBuilderInfo> sisBuilderInfoList = config.getSISBuilderInfoList();
				for (ISISBuilderInfo sisBuildInfo :sisBuilderInfoList) {
					tmpPkgFile= createTempPkgFile();
					if (tmpPkgFile == null) {
						Check.reportFailure(tmpPkgFile.getLocation().toString() + Messages.getString("NewPIWizard.failed.convertFileToIFile"), new Throwable()); //$NON-NLS-1$
						return;
					}
					IPath tmpPkgPath = tmpPkgFile.getLocation();
					CarbideCPPBuilder.resolvePKGFile(sisBuildInfo.getPKGFullPath(), 
							context, 
							tmpPkgPath);
					preprocessedMap.put(config, tmpPkgFile);
				}
			}
		}
		if (preprocessedMap.size() > 0) {
			for(Entry<Object, IFile> entry : preprocessedMap.entrySet()) {
				String epocroot = ""; //$NON-NLS-1$
				if (entry.getKey() instanceof IPkgEntry) {
					epocroot = ((IPkgEntry)entry.getKey()).getSdk().getEPOCROOT();
				} else if (entry.getKey() instanceof ICarbideBuildConfiguration) {
					epocroot = ((ICarbideBuildConfiguration)entry.getKey()).getSDK().getEPOCROOT();
				}
				sampleImporter.addPkgObyFile(epocroot, entry.getValue().getLocation().toString());
			}
		}
		
		if (wizardSettings.keyMapProfile != null) {
			//BUP key press profile
			sampleImporter.setBupMapProfileId(wizardSettings.keyMapProfile.getProfileId());
			if (wizardSettings.keyMapProfile.getSDK() != null) {
				sampleImporter.setBupMapSymbianSDKId(wizardSettings.keyMapProfile.getSDK().getUniqueId());
				sampleImporter.setBupMapIsBuiltIn(false);
				sampleImporter.setBupMapIsWorkspace(false);
			}
			if (wizardSettings.keyMapProfile.getURI().equals(BupEventMapManager.DEFAULT_PROFILE_URI)) {
				sampleImporter.setBupMapSymbianSDKId(""); //$NON-NLS-1$
				sampleImporter.setBupMapIsBuiltIn(true);
				sampleImporter.setBupMapIsWorkspace(false);
			} else if (wizardSettings.keyMapProfile.getURI().equals(BupEventMapManager.WORKSPACE_PREF_KEY_MAP_URI)) {
				sampleImporter.setBupMapSymbianSDKId(""); //$NON-NLS-1$
				sampleImporter.setBupMapIsBuiltIn(false);
				sampleImporter.setBupMapIsWorkspace(true);
			}			
		}

		sampleImporter.importSamples(false);	// not waiting NPI save complete for responsive UI, save NPI on background
		
		cleanTempPkgFile();
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		setNeedsProgressMonitor(true);
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}
			
	NewPIWizardSettings getWizardData() {
		return wizardSettings;
	}
	
	public boolean canFinish() {
		return isAllFollowingPagesComplete(getStartingPage());
	}
	
	private boolean isAllFollowingPagesComplete(IWizardPage page) {
		IWizardPage currentPage = page;
		while (currentPage != null) {
			if (currentPage.isPageComplete() == false) {
				return false;
			}
			currentPage = getNextPage(currentPage);
		}
		// after ending page
		return true;
	}
	
	// all trace ID, we can check this for trace specific handling
	public Set<Integer> getTraceSet() {
		return traceSet;
	}
	
	// all trace ID, we can do trace specific handling later
	public void setTraceSet(Set<Integer> myTraceSet) {
		traceSet = myTraceSet;
	}
	
	public IWizardPage getPreviousPage (IWizardPage page) {
		NewPIWizardSettings settings = NewPIWizardSettings.getInstance();
		if (page == pageOutput && settings.enableCust == false) {
			return getPreviousPage(pageCustom);
		} else if (page == pageCustom && getTraceSet().contains(ButtonPlugin.getDefault().getTraceId()) == false){
			return getPreviousPage(pageBupMap);
		} else if (page == pageBupMap && wizardSettings.haveRomOnly == false && wizardSettings.haveAppRom == false) {
			return getPreviousPage(pageObySym);
		} else if (page == pageObySym && wizardSettings.haveAppOnly == false && wizardSettings.haveAppRom == false) {
			return getPreviousPage(pagePkgList);
		}

		return super.getPreviousPage(page);
	}

	public IWizardPage getNextPage (IWizardPage page) {
		if (page == pageConfigSelector && wizardSettings.haveAppOnly == false && wizardSettings.haveAppRom == false) {
			return getNextPage(pagePkgList);
		} else if (page == pagePkgList && wizardSettings.haveRomOnly == false && wizardSettings.haveAppRom == false) {
			return getNextPage(pageObySym);
		} else if (page == pageObySym && getTraceSet().contains(ButtonPlugin.getDefault().getTraceId()) == false) {
			return getNextPage(pageBupMap);
		} else if (page == pageBupMap && wizardSettings.enableCust == false) {
			return getNextPage(pageCustom);
		}
		return super.getNextPage(page);
	}

}
