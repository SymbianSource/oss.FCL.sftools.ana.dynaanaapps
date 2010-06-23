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

package com.nokia.carbide.cpp.pi.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.nokia.carbide.cpp.internal.pi.analyser.AnalyserDataProcessor;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.utils.PIUtilities;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class SampleImporter {
	private static SampleImporter instance;
	
	// location of input .dat file
	private String datFileName;
	// location of output project
	private String outputContainerName;
	// location of output .pi file
	private String piFileName;
	// workspace resource .npi file
	private IFile piFile;
	// PKG file and their SDK_ID or EPOCROOT
	public static class PkgObyFile {
		public String fileAbsolutePath;
		public String epocRoot;
		PkgObyFile(String epocRoot, String fileAbsolutePath) {
			this.fileAbsolutePath = fileAbsolutePath;
			this.epocRoot = epocRoot;
		}
	}
	private ArrayList<PkgObyFile> pkgObyFileList = new ArrayList<PkgObyFile>();
	
	private boolean importStripTimeStamp = false;	// importer is just doing dummy import for stripping time stamp for diffing
	
	// ROM related stuff
	private String romEpocroot;
	private String romSymbolFile;
	private String romObyFile;
	private ArrayList<String> rofsSymbolFileList = new ArrayList<String>();
	private ArrayList<String> rofsObyFileList = new ArrayList<String>();
	
	// Custom sample
	private String custAbsolutePath;
	private boolean custIsValueBased;
	private String custDelimitor;
	
	// Bup sample
	private String bupMapProfileId;
	private String bupMapSymbianSDKId;
	private boolean bupMapIsBuiltIn;
	private boolean bupMapIsWorkspace;
	
	private boolean profilerActivator;
	
	
	private SampleImporter() {
		clear();
		// singleton
	}
	
	public static SampleImporter getInstance() {
		if (instance == null) {
			instance = new SampleImporter();
		}
		
		return instance;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public String getDatFileName () {
		return datFileName;
	}

	public void setDatFileName (String file) {
		datFileName = file;
	}

	public void clearDatFileName () {
		setDatFileName(""); //$NON-NLS-1$
	}

	public String getProjectName () {
		return outputContainerName;
	}

	public void setProjectName (String file) {
		outputContainerName = file;
	}

	public void clearProjectName () {
		setProjectName(""); //$NON-NLS-1$
	}
	
	public String getPiFileName () {
		return piFileName;
	}

	public void setPiFileName (String file) {
		piFileName = file;
	}
	
	public void clearPiFileName () {
		setPiFileName(""); //$NON-NLS-1$
	}
	
	public IFile getPiFile() {
		return this.piFile;
	}
	
	public void setPiFile(IFile piFile) {
		this.piFile = piFile;
	}
	
	public void setStripTimeStampActive () {
		this.importStripTimeStamp = true;
	}
	
	public void setStripTimeStampDone() {
		this.importStripTimeStamp = false;
	}
	
	public boolean isStrippingTimeStamp() {
		return this.importStripTimeStamp;
	}

	public PkgObyFile[] getPkgObyFilesList () {
		ArrayList<PkgObyFile> result = new ArrayList<PkgObyFile>();
		// round up all ROM OBY and app PKG
		if (romObyFile != null && romObyFile.length() > 0) {
			PkgObyFile romObyEntry = new PkgObyFile(romEpocroot, romObyFile);
			result.add(romObyEntry);
		}
		for (String rofsObyFile : rofsObyFileList) {
			PkgObyFile rofsObyEntry = new PkgObyFile(romEpocroot, rofsObyFile);
			result.add(rofsObyEntry);
		}
		result.addAll(pkgObyFileList);
		return result.toArray(new PkgObyFile[result.size()]);
	}

	public void addPkgObyFile (String epocroot, String filePath) {
		pkgObyFileList.add(new PkgObyFile(epocroot, filePath));
	}
	
	public void clearPkgObyFileList () {
		pkgObyFileList.clear();
	}

	public String getRomEpocroot () {
		return romEpocroot;
	}
	
	public void setRomEpocroot (String epocroot) {
		romEpocroot = epocroot;
	}

	public void clearRomEpocroot () {
		setRomEpocroot(""); //$NON-NLS-1$
	}

	public String getRomSymbolFile () {
		return romSymbolFile;
	}
	
	public void setRomSymbolFile (String filename) {
		romSymbolFile = filename;
	}

	public void clearRomSymbolFile () {
		setRomSymbolFile(""); //$NON-NLS-1$
	}
	
	public String getRomObyFile () {
		return romObyFile;
	}
	
	public void setRomObyFile (String filename) {
		romObyFile = filename;
	}
	
	public void clearRomObyFile () {
		setRomObyFile(""); //$NON-NLS-1$
	}

	public String[] getRofsSymbolFileList () {
		return rofsSymbolFileList.toArray(new String[rofsSymbolFileList.size()]);
	}
	
	public void addRofsSymbolFile (String filename) {
		rofsSymbolFileList.add(filename);
	}

	public void clearRofsSymbolFileList () {
		rofsSymbolFileList.clear();
	}

	public String[] getRofsObyFileList () {
		return rofsObyFileList.toArray(new String[rofsObyFileList.size()]);
	}
	
	public void addRofsObyFile (String filename) {
		rofsObyFileList.add(filename);
	}

	public void clearRofsObyFileList () {
		rofsObyFileList.clear();
	}

	public String getCustAbsolutePath () {
		return custAbsolutePath;
	}
	
	public void setCustAbsolutePath (String pathName) {
		custAbsolutePath = pathName;
	}

	public void clearCustAbsolutePath () {
		setCustAbsolutePath(""); //$NON-NLS-1$
	}

	public boolean getCustIsValueBased () {
		return custIsValueBased;
	}
	
	public void setCustIsValueBased (boolean value) {
		custIsValueBased = value;
	}

	public void clearCustIsValueBased () {
		setCustIsValueBased(true);
	}
	
	public String getCustDelimitor () {
		return custDelimitor;
	}
	
	public void setCustDelimitor (String delimitor) {
		custDelimitor = delimitor;
	}

	public void clearCustDelimitor () {
		setCustDelimitor(""); //$NON-NLS-1$
	}
	
	public String getBupMapProfileId() {
		return bupMapProfileId;
	}

	public void setBupMapProfileId(String bupMapProfileId) {
		this.bupMapProfileId = bupMapProfileId;
	}
	
	public void clearBupMapProfileId() {
		setBupMapProfileId("");	//$NON-NLS-1$
	}

	public String getBupMapSymbianSDKId() {
		return bupMapSymbianSDKId;
	}

	public void setBupMapSymbianSDKId(String bupMapSymbianSDKId) {
		this.bupMapSymbianSDKId = bupMapSymbianSDKId != null ? bupMapSymbianSDKId : "";	//$NON-NLS-1$
	}
	
	public void clearBupMapSymbianSDKId() {
		setBupMapSymbianSDKId("");	//$NON-NLS-1$
	}

	public boolean isBupMapIsBuiltIn() {
		return bupMapIsBuiltIn;
	}

	public void setBupMapIsBuiltIn(boolean bupMapIsBuiltIn) {
		this.bupMapIsBuiltIn = bupMapIsBuiltIn;
	}
	
	public void clearBupMapIsBuiltIn() {
		setBupMapIsBuiltIn(false);
	}

	public boolean isBupMapIsWorkspace() {
		return bupMapIsWorkspace;
	}

	public void setBupMapIsWorkspace(boolean bupMapIsWorkspace) {
		this.bupMapIsWorkspace = bupMapIsWorkspace;
	}

	public void clearBupMapIsWorkspace() {
		setBupMapIsWorkspace(false);
	}

	public void clear() {
		clearDatFileName ();
		clearProjectName ();
		clearPiFileName ();
		clearPkgObyFileList ();
		clearRomEpocroot ();
		clearRomSymbolFile ();
		clearRomObyFile ();
		clearRofsSymbolFileList ();
		clearRofsObyFileList ();
		clearCustAbsolutePath ();
		clearCustIsValueBased ();
		clearCustDelimitor ();
		clearBupMapProfileId ();
		clearBupMapSymbianSDKId ();
		clearBupMapIsBuiltIn ();
		clearBupMapIsWorkspace ();
		profilerActivator = false;
	}
	
	public boolean validate () {
		if (getDatFileName().equals("")) { //$NON-NLS-1$
			return false;
		} else if (new java.io.File(getDatFileName()).exists() == false) {
			return false;
		}
		
		if (getProjectName().equals("")) {	//$NON-NLS-1$
			return false;
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(getProjectName());
		if (resource.exists() == false) {
			return false;
		}
		
		if (getPiFileName().equals("")) {	//$NON-NLS-1$
			return false;
		}
		
		if (getRomEpocroot().equals("") != false) {	//$NON-NLS-1$
			if (getRomSymbolFile().equals("") == false) { 	//$NON-NLS-1$
				if (new java.io.File(getRomSymbolFile()).exists() == false) {
					return false;
				}
			}
			
			if (getRomObyFile().equals("") == false) { 	//$NON-NLS-1$
				if (new java.io.File(getRomObyFile()).exists() == false) {
					return false;
				}
			}
			
			for (String fileName : getRofsObyFileList()) {
				if (fileName.equals("") == false) {	//$NON-NLS-1$
					if (new java.io.File(fileName).exists() == false) {
						return false;
					}
				}	
			}			
		}
		
		if (pkgObyFileList.size() < 0) {
			return false;
		}
		
		return true;
	}

	
	/**
	 * Kick-starts the import of an analysis file with the current settings in SampleImporter
	 * @param pollTillNpiSaved if true, will poll until the .npi file is saved
	 */
	public void importSamples(boolean pollTillNpiSaved) {
		try {
			List<ITrace> pluginsInTraceFile  = PIUtilities.getPluginsForTraceFile(SampleImporter.getInstance().getDatFileName());
			importSamples(pollTillNpiSaved, pluginsInTraceFile, true, null, null);
		} catch (IOException e) {
			GeneralMessages.showErrorMessage(Messages.getString("SampleImporter.0") + e.getLocalizedMessage()); //$NON-NLS-1$
		}		
	}
	
	/**
	 * Kick-starts the import of an analysis file with the current settings in SampleImporter
	 * @param pollTillNpiSaved if true, will poll until the .npi file is saved
	 * @param pluginsInTraceFile List of plugins to use for the import. The purpose is to be able to select which analysis to perform.
	 * @param activate if true, will open an editor and activate it otherwise will import the file
	 * @param suffixTaskName suffix for IProgressMonitor task name
	 * @param progressMonitor instance of IProgressMonitor
	 */
	public void importSamples(boolean pollTillNpiSaved, List<ITrace> pluginsInTraceFile, boolean activate, String suffixTaskName, IProgressMonitor progressMonitor) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(getProjectName());
		
		IContainer piContainer;
		if (resource instanceof IContainer) {
			piContainer = (IContainer) resource;
		} else {
			return;
		}
		
		// empty file if there is no symbol file specified, or analyser would crash
		java.io.File dummySymbol = null;
		if (getRomSymbolFile() == null || getRomSymbolFile().length() <= 0) {
			try {
				dummySymbol = java.io.File.createTempFile("dummySymbol", ".symbol");	//$NON-NLS-1$ //$NON-NLS-2$
				setRomSymbolFile(dummySymbol.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		// allow to set specific npi file name that is used from the JUnit test cases
		boolean piFileNameForced = false;
		if(piFileName.length() == 0){
			// import the new project according to wizard
			piFileName = new Path(getDatFileName()).removeFileExtension().addFileExtension("npi").lastSegment(); //$NON-NLS-1$
		}else{
			piFileNameForced = true;
		}
		piFile = piContainer.getFile(new Path(piFileName));

	
		if (piFile.exists())
		{
			if(piFileNameForced){
				try {
					piFile.delete(true, null);
				} catch (CoreException e) {
					GeneralMessages.showErrorMessage(com.nokia.carbide.cpp.pi.importer.Messages.getString("SampleImporter.importerInternalError"));  //$NON-NLS-1$
					GeneralMessages.piLog(com.nokia.carbide.cpp.pi.importer.Messages.getString("SampleImporter.importerCoreException"), GeneralMessages.ERROR);  //$NON-NLS-1$
					return;
				}
			}else{
				piFile = piContainer.getFile(new Path(generateNpiFileName(piContainer, piFileName)));
			}			
		}

		// import and save the file as npi
		if(activate){
			AnalyserDataProcessor.getInstance().importSaveAndOpen(piFile, pollTillNpiSaved, pluginsInTraceFile);
		}else{				
			AnalyserDataProcessor.getInstance().importSave(piFile, pluginsInTraceFile, suffixTaskName, progressMonitor);				
		}
		if(profilerActivator){
			IPath datPath = new Path(getDatFileName());
			IPath piPath = piFile.getLocation();
			piPath = piPath.removeFileExtension().addFileExtension(datPath.getFileExtension());
			if(!datPath.toFile().renameTo(piPath.toFile())){
				GeneralMessages.piLog(Messages.getString("SampleImporter.failedToMoveProfilerDataFile"), GeneralMessages.INFO);  //$NON-NLS-1$
			}

		}
		if (dummySymbol != null) {
			if(getRomSymbolFile().equals(dummySymbol.toString())){
				dummySymbol.delete();
				dummySymbol = null;
				setRomSymbolFile(null);
			}			
		}
	}
	
	private String generateNpiFileName(IContainer container, String initialFilename) {
		// get just the file name(last part)
		initialFilename = new java.io.File(initialFilename).getName();
		
		String baseName;
		Long suffixNumber = Long.valueOf(0);
		
		int dot = initialFilename.lastIndexOf("."); //$NON-NLS-1$
		if (dot > 1) {
			baseName = initialFilename.substring(0, dot); //$NON-NLS-1$
		} else {
			baseName = initialFilename;
		}
		
		if (initialFilename.endsWith(".npi")) {	//$NON-NLS-1$
			// the input is a .npi doesn't exist in container, user should have manually typed it
			if (container.getFile(new Path(initialFilename)).exists() == false) {
				return initialFilename;
			}
			
			// this is probably an ***(<number>).npi we need to increament
			// just suffix (<number>) if the name was derived from input sample name
	
			if(baseName.lastIndexOf(")") == (baseName.length() - 1) && baseName.lastIndexOf("(") != -1){ //$NON-NLS-1$ //$NON-NLS-2$
				String number = baseName.substring(baseName.lastIndexOf("(") + 1, baseName.lastIndexOf(")")); //$NON-NLS-1$ //$NON-NLS-2$
				if(isPositiveLong(number)){
					suffixNumber = Long.parseLong(number); 
					baseName = baseName.substring(0, baseName.lastIndexOf("(")); //$NON-NLS-1$
				}
			
			}
		}
				
		// check existing npi and bump number
		while (container.getFile(new Path(baseName + "(" + suffixNumber.toString()+ ")" + ".npi")).exists()) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			suffixNumber++;
		}

		return baseName + "(" + suffixNumber.toString() + ")" +  ".npi"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	private boolean isPositiveLong(String x) {
		try {
			if (Long.parseLong(x) >= 0) {
				return true;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return false;
	}
	
	// support for removing timestamp to support test
	public void stripeTimestamp(final String sourceFilePath, final String dstFilePath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject piProjectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject("pifilecompartor_temp"); //$NON-NLS-1$
		final IProjectDescription description = workspace.newProjectDescription(piProjectHandle.getName());
		SampleImporter sampleImporter = SampleImporter.getInstance();

		// strip first file of timestamp by importer like trick
		sampleImporter.setDatFileName(sourceFilePath);
		sampleImporter.setPiFileName(dstFilePath);

		setStripTimeStampActive();
		
		try {
			if (!piProjectHandle.exists()) {
				piProjectHandle.create(description, null);
				piProjectHandle.open(null);
			} else {
				piProjectHandle.open(null);
			}
			
			// import the new project according to wizard
			IFile piFile = piProjectHandle.getFile("temp.npi"); //$NON-NLS-1$

			if (piFile.exists())
			{
				piFile.delete(true, null);
			}
			// create the .pi on the project so IDE.openEditor works
			piFile.create(new ByteArrayInputStream("".getBytes()), false, null); //$NON-NLS-1$

			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() , piFile, true);
			// take out stalled temp.npi window
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true);

		} catch (CoreException e) {
			e.printStackTrace();
		}
		setStripTimeStampDone();
	}
		
	public void setProfilerActivator(boolean profilerActivator){
		this.profilerActivator = profilerActivator;
	}
	public boolean isProfilerActivator(){
		return profilerActivator;
	}
}
