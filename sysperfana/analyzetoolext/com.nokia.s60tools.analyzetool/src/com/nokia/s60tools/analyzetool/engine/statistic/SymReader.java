/*
 * Copyright (c) 2008-2009 Nokia Corporation and/or its subsidiary(-ies).
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
 * Description:  Definitions for the class SymReader
 *
 */


package com.nokia.s60tools.analyzetool.engine.statistic;

import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.nokia.carbide.cdt.builder.CarbideBuilderPlugin;
import com.nokia.carbide.cdt.builder.DefaultMMPViewConfiguration;
import com.nokia.carbide.cdt.builder.EpocEngineHelper;
import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ICarbideProjectInfo;
import com.nokia.carbide.cpp.epoc.engine.EpocEnginePlugin;
import com.nokia.carbide.cpp.epoc.engine.MMPDataRunnableAdapter;
import com.nokia.carbide.cpp.epoc.engine.model.mmp.EMMPStatement;
import com.nokia.carbide.cpp.epoc.engine.model.mmp.IMMPData;
import com.nokia.carbide.cpp.epoc.engine.preprocessor.AcceptedNodesViewFilter;
import com.nokia.cdt.debug.cw.symbian.symbolreader.ISymbolFile;
import com.nokia.cdt.debug.cw.symbian.symbolreader.ISymbolReaderManager;
import com.nokia.cdt.debug.cw.symbian.symbolreader.SymbolReaderManager;
import com.nokia.s60tools.analyzetool.global.Constants;

/**
 * Opens and closes symbol files.
 * @author kihe
 *
 */
@SuppressWarnings("restriction")
public class SymReader {

	/**Selected project*/
	IProject project;

	/**List of opened symbol files */
	HashMap<String, ISymbolFile> allreayOpen;

	/**Project target list*/
	AbstractList<String> targets;

	/**SymbolReaderManager*/
	ISymbolReaderManager manager;

	/**Build target*/
	String buildTarget;

	/**
	 * Constructor
	 * @param projectRef Active project reference
	 */
	public SymReader( IProject projectRef )
	{
		project = projectRef;
		targets = new ArrayList<String>();
		manager = SymbolReaderManager.getSymbolReaderManager();
		allreayOpen = new HashMap<String, ISymbolFile>();
	}


	/**
	 * Gets ISymbolFile object to corresponding symbol file
	 * @param moduleName Module name which is loaded
	 * @param onlyForProjecTargets Create results for only project modules
	 * @return Symbol file if found, otherwise null
	 */
	public ISymbolFile getSymbolFile( String moduleName, boolean onlyForProjecTargets )
	{
		//thru project targets
		if( onlyForProjecTargets )
		{
			Iterator<String> targetsIter = targets.iterator();
			while( targetsIter.hasNext() )
			{
				String oneTarget = targetsIter.next();
				if( oneTarget.contains( moduleName ) )
				{
					return getSymFile(moduleName);
				}
			}
		}
		else
		{
			return getSymFile(moduleName);
		}

		return null;
	}

	/**
	 * Opens and return ISymbolFile for the given module
	 * @param moduleName Module name
	 * @return Opened symbol file
	 */
	private ISymbolFile getSymFile(String moduleName )
	{
		if( allreayOpen.containsKey( moduleName ) )
		{
			return allreayOpen.get( moduleName );
		}

		//get symbol file name
		String symFile = getSymbolFileNameForTarget( moduleName );
		if( symFile != null && manager != null )
		{
			ISymbolFile symbolFile = manager.openSymbolFile( symFile );
			allreayOpen.put(moduleName, symbolFile);
			return symbolFile;
		}

		return null;
	}


	/**
	 *
	 * @param targetName Target name
	 * @return Target sym file name if found, otherwise null
	 */
	public String getSymbolFileNameForTarget( final String targetName )
	{
		if( targetName == null || ("").equals( targetName) )
			return null;

		buildTarget = getBuildTarget();
		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo( project );
		String platform = cpi.getDefaultConfiguration().getPlatformString();
		//set symbol file location
		char backslash = '\\';

		String epocRoot = getEpocroot();
		if( epocRoot == null || ("").equals( epocRoot )) {
			return null;
		}
		StringBuffer symbolFileLocation = new StringBuffer(32);
		symbolFileLocation.append( epocRoot );
		symbolFileLocation.append( "epoc32\\release\\" );
		symbolFileLocation.append(platform);
		symbolFileLocation.append(backslash);
		symbolFileLocation.append( buildTarget );
		symbolFileLocation.append(backslash);

		
		//build target is set to WINSCW we can use the target name
		if( (Constants.BUILD_TARGET_WINSCW).equalsIgnoreCase(platform) ) {
			symbolFileLocation.append(targetName);
		}
		//remove target name extension and add .sym
		else if( targetName.lastIndexOf('.') != -1 ) {
			String targetWithoutExt = targetName.substring(0, targetName.lastIndexOf('.'));
			symbolFileLocation.append( targetWithoutExt );
			symbolFileLocation.append(".sym");
		}
		
		//symbol file exists => return file location
		File symFile = new File( symbolFileLocation.toString() );
		if( symFile.exists() )
		{
			return symbolFileLocation.toString();
		}
		//symbol file not found
		//try find target(with extension) + .sym
		StringBuffer symFileLocWithExt = new StringBuffer(32);
		symFileLocWithExt.append( epocRoot );
		symFileLocWithExt.append( "epoc32\\release\\" );
		symFileLocWithExt.append(platform);
		symFileLocWithExt.append(backslash);
		symFileLocWithExt.append( buildTarget );
		symFileLocWithExt.append(backslash);
		symFileLocWithExt.append(targetName);
		symFileLocWithExt.append(".sym");

		File symFileWithExt = new File( symFileLocWithExt.toString() );
		if( symFileWithExt.exists() ) {
			return symFileLocWithExt.toString();
		}
		return null;

	}

	/**
	 * Returns build target
	 * @return Active build target
	 */
	public String getBuildTarget()
	{
		//get build target
		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo( project );
		EpocEngineHelper.getEpocRootForProject(project);
		String configName = cpi.getDefaultBuildConfigName();
		buildTarget = "udeb";
		if( configName.contains( "Release") )
		{
			buildTarget = "urel";
		}
		return buildTarget;
	}

	/**
	 * Returns default epocroot location
	 * @return Epocroot location
	 */
	public String getEpocroot()
	{
		if( project != null && project.isOpen() ) {
			IPath epocRootPath = EpocEngineHelper.getEpocRootForProject(project);
			if( epocRootPath != null ) {
				return epocRootPath.toOSString();
			}
		}
		return null;
	}

	/**
	 * Closes active ISymbolFiles
	 */
	public void dispose()
	{
		if( allreayOpen.isEmpty() ) {
			manager = null;
			return;
		}
		//thru open ISymbolFiles
		Set<String> keySet = allreayOpen.keySet();
		for (String moduleName : keySet) {
			ISymbolFile symFile = allreayOpen.get( moduleName );
			symFile.close();
			symFile = null;
			allreayOpen.remove( moduleName );
		}

		allreayOpen.clear();
		manager = null;
	}

	/**
	 * Gets current project targets info
	 */
	public void loadProjectTargetsInfo()
	{
		// Assumes buildConfig (ICarbideBuildConfiguration) is known
		ICarbideProjectInfo cpi = CarbideBuilderPlugin.getBuildManager().getProjectInfo( project );
		ICarbideBuildConfiguration buildConfig = cpi.getDefaultConfiguration();
		for (IPath mmpPath : EpocEngineHelper.getMMPFilesForBuildConfiguration(buildConfig)) {
		     Object data = EpocEnginePlugin.runWithMMPData(mmpPath,  new
		                              DefaultMMPViewConfiguration(buildConfig.getCarbideProject().getProject(),
		                              buildConfig, new AcceptedNodesViewFilter()),  new MMPDataRunnableAdapter()
		     {
					public Object run(IMMPData mmpData) {
						// The real return value, getting a single argument setting
					    return mmpData.getSingleArgumentSettings().get(EMMPStatement.TARGET);
					}
		      });

		     // Make sure to test for and cast to proper Object type!
			String mmpStatement = (String)data;  // Now we should have the TARGETTYPE
			targets.add( mmpStatement );
		}
	}
	
	/**
	 * Reopen all cached symbol files if not already open
	 */
	public void reOpenCachedSymbolFiles() {
		if (manager == null) {
			//it's strange that we need to create another Object here so that DE releases the
			//targets
			manager = new SymbolReaderManager().getSymbolReaderManager();
		}
		Set<String> keySet = allreayOpen.keySet();
		for (String moduleName : keySet) {
			if (allreayOpen.get(moduleName) == null) {
				String file = getSymbolFileNameForTarget(moduleName);
				if (file != null) {
					ISymbolFile symFile = manager.openSymbolFile(file);
					allreayOpen.put(moduleName, symFile);
				}
			}
		}
	}
	
	/**
	 * Close all cached symbol files
	 */
	public void closeCachedSymbolFiles() {
		Set<String> keySet = allreayOpen.keySet();
		for (String moduleName : keySet) {
			ISymbolFile symFile = allreayOpen.get( moduleName );
			if (symFile != null) {
				symFile.close();
				symFile = null;
			}
			allreayOpen.put(moduleName, null);
		}
		manager = null;
	}
}
