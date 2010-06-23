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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.nokia.carbide.cdt.builder.builder.CarbideCPPBuilder;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ISISBuilderInfo;
import com.nokia.carbide.cpp.internal.api.sdk.SymbianBuildContext;
import com.nokia.carbide.cpp.internal.pi.analyser.AnalyserDataProcessor;
import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.utils.PIUtilities;
import com.nokia.carbide.cpp.internal.pi.wizards.model.SessionHandler;
import com.nokia.carbide.cpp.internal.pi.wizards.model.TraceFile;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.IPkgEntry;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.RofsObySymbolPair;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.views.PIView;
import com.nokia.carbide.cpp.pi.PiPlugin;
import com.nokia.carbide.cpp.pi.button.BupEventMapManager;
import com.nokia.carbide.cpp.pi.button.ButtonPlugin;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.pi.wizards.WizardsPlugin;
import com.nokia.carbide.cpp.sdk.core.ISymbianBuildContext;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
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
	
	protected IProject piProject;
	IWorkbench workbench;
	
	NewPIWizardSettings wizardSettings = NewPIWizardSettings.getInstance();

	private List<ProfilerDataPlugins> profilerDataPlugins;

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
		
		final SampleImporter sampleImporter = SampleImporter.getInstance();
		NewPIWizardSettings wizardSettings = NewPIWizardSettings.getInstance();
		Map<Object, IFile> preprocessedMap = new HashMap<Object, IFile>();
		sampleImporter.clear();
		
		String container = wizardSettings.outputContainer.getFullPath().toString();
		if (container.startsWith("/")) //$NON-NLS-1$
			container = container.substring(1, container.length());
		sampleImporter.setProjectName(container);
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
		
		sampleImporter.setProfilerActivator(wizardSettings.profilerActivator);

		if(profilerDataPlugins.size() <= 1){
			for(ProfilerDataPlugins pdp : profilerDataPlugins){
				sampleImporter.setDatFileName(pdp.getProfilerDataPath().toString());	
				// due to PI shortcomings (i.e. plugins that create pages have to come first)
				// the plugins have to be sorted by trace id
				sampleImporter.setPiFileName(""); //$NON-NLS-1$
				sampleImporter.importSamples(false, PIUtilities.sortPlugins(pdp.getSelectedPlugins()), true, null, null);						
				logImportedFile(pdp);
				break;
			}
		}	
		else{		
			final int[] i={1};
			final int count = profilerDataPlugins.size();
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			IRunnableWithProgress runnable= new IRunnableWithProgress() {

				public void run(IProgressMonitor progressMonitor) {					
					progressMonitor.beginTask("", count*AnalyserDataProcessor.TOTAL_PROGRESS_COUNT); //$NON-NLS-1$
					String suffixTaskName;
					for (ProfilerDataPlugins pdp : profilerDataPlugins) {
						if(progressMonitor.isCanceled()){
							break;
						}						
						suffixTaskName = MessageFormat.format(Messages.getString("NewPIWizard.suffixTaskName"), i[0]++, count);						 //$NON-NLS-1$
						sampleImporter.setDatFileName(pdp.getProfilerDataPath().toString());	
						sampleImporter.setPiFileName(""); //$NON-NLS-1$
						sampleImporter.importSamples(false, PIUtilities
								.sortPlugins(pdp.getSelectedPlugins()), false, suffixTaskName,
								new SubProgressMonitor(progressMonitor, AnalyserDataProcessor.TOTAL_PROGRESS_COUNT));
						logImportedFile(pdp);
					}		
				}

			};
			try {
				progressService.busyCursorWhile(runnable);
			} catch (Exception e) {
				GeneralMessages.showErrorMessage(e.getMessage());
			}
		}
		cleanTempPkgFile();	
		showPIViewer();
	}
	
	/**
	 * Log given imported file to show it on the PI view
	 * 
	 * @param pdp instance of the ProfilerDataPlugins
	 */
	private void logImportedFile(ProfilerDataPlugins pdp){
		SampleImporter sampleImporter = SampleImporter.getInstance();
		ISymbianSDK sdk = NewPIWizardSettings.getInstance().romSdk;
		String sdkName = "";
		if(sdk != null && (NewPIWizardSettings.getInstance().haveRomOnly || NewPIWizardSettings.getInstance().haveAppRom)){
			sdkName = NewPIWizardSettings.getInstance().romSdk.getUniqueId();
		}
		long sampleTime = -1;

		Iterator<ParsedTraceData> traces = TraceDataRepository.getInstance().getTraceCollectionIter(NpiInstanceRepository.getInstance().activeUid());
		while(traces.hasNext()){
			ParsedTraceData ptd = traces.next();
			GenericTrace gt = ptd.traceData;
			if(gt instanceof GenericSampledTrace){
				GenericSampledTrace gst = (GenericSampledTrace)gt;
				long lastSampleTime = gst.getLastSampleTime();
				if(sampleTime < lastSampleTime){
					sampleTime = lastSampleTime;
				}
			}
		}
		List<ITrace> plugins = pdp.getSelectedPlugins();
		int[] pluginIds = new int[plugins.size()];
		int i=0;
		for(ITrace plugin : plugins){
			pluginIds[i++] = plugin.getTraceId();
		}
		IPath filePath = sampleImporter.getPiFile().getFullPath();
		SessionHandler.getInstance().addTraceFile(new TraceFile(filePath, sampleImporter.getProjectName(), sdkName, filePath.toFile().length(), sampleTime, pluginIds));
		Display.getDefault().asyncExec(new Runnable() {
			
			public void run() {
				showPIViewer();
				
			}
		});
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
	
	public IWizardPage getPreviousPage (IWizardPage page) {
		NewPIWizardSettings settings = NewPIWizardSettings.getInstance();
		if (page == pageOutput && settings.enableCust == false) {
			return getPreviousPage(pageCustom);
		} else if (page == pageCustom && isButtonPluginUsed() == false){
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
		} else if (page == pageObySym && isButtonPluginUsed() == false) {
			return getNextPage(pageBupMap);
		} else if (page == pageBupMap && wizardSettings.enableCust == false) {
			return getNextPage(pageCustom);
		}
		return super.getNextPage(page);
	}
	
	public void setProfilerDataFiles(List<ProfilerDataPlugins> profilerDataPlugins){
		this.profilerDataPlugins = profilerDataPlugins;
	}
	
	private boolean isButtonPluginUsed(){
		if(profilerDataPlugins != null){
			for(ProfilerDataPlugins pdp : profilerDataPlugins){
				if(pdp.getSelectedPlugins().contains(ButtonPlugin.getDefault())){
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		if(PiPlugin.isTraceProviderAvailable() && PiPlugin.getTraceProvider().isListening()){
			showInformationDialog();
			return false;
		}
		pageInput.handleTemporaryProfilerDataFiles(false);
		return super.performCancel();
	}
	
	/**
	 * Show information dialog about to stop tracing in order to go back or
	 * close the wizard page
	 */
	public void showInformationDialog(){
		MessageDialog.openInformation(getShell(), Messages.getString("NewPIWizard.informationDialogTitle"), Messages.getString("NewPIWizard.informationDialogMessage")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Show PI view
	 */
	private void showPIViewer(){
	   	try {
    		IWorkbenchWindow workbenchWindow = PiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
    		if (workbenchWindow == null)
    			return;
    		IWorkbenchPage page = workbenchWindow.getActivePage();
    		// Checking if view is already open
    		IViewReference[] viewRefs = page.getViewReferences();
    		for (int i = 0; i < viewRefs.length; i++) {
				IViewReference reference = viewRefs[i];
				String id = reference.getId();
				if(PIView.ID.equalsIgnoreCase(id)){
					// Found, restoring the view
					IViewPart viewPart = reference.getView(true);
					page.activate(viewPart);			
					((PIView)viewPart).updateView();
					return;
				}
			}
    		
    		// View was not found, opening it up as a new view.
    		IViewPart viewPart = page.showView(PIView.ID);
    		((PIView)viewPart).updateView();
    	} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#dispose()
	 */
	@Override
	public void dispose() {
		showPIViewer();
		super.dispose();
	}
	
}
