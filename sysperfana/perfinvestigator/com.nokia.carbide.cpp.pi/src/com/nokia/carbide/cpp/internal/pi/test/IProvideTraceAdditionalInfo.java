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

/**
 * 
 */
package com.nokia.carbide.cpp.internal.pi.test;

import java.util.Vector;

/**
 * 
 * Trace specific support for additional info in NPI file.
 * Additional info is saved as Vector<Object> in NPI file and loaded as
 * AnalysisInfoHandler in NPI file during runtime. 
 * -During load, additional info is transfer to AnalysisInfoHandler
 * -During save, AnalysisInfoHandler is transfer to additional info
 * -After import, AnalysisInfoHandler is set up (e.g. with sampleimporter 
 * data or anything we figure out during import)
 * -Any modification to additional info with an opened instance of NPI files are done
 * on AnalysisInfoHandler, and mark the file dirty
 * 
 * These allow us to use common write route used by import and save as.
 * 
 * Plugins should define their own internal class to encapsulate runtime
 * structure in AnalysisInfoHandler and use AnalysisInfoHandler.set/getTraceDefinedInfo
 * to access their own share of additional when NPI file is loaded.
 * 
 *
 */
public interface IProvideTraceAdditionalInfo {
	/**
	 * Set up info handler right after loading .dat file, so we can commit those
	 * from info handler to additional info with a common write route used by import 
	 * and save as. Info can be extracted info from importer.
	 * @param traceAdditionalInfos
	 */
	public void setupInfoHandler(AnalysisInfoHandler handler);
	/**
	 * Move info from additional info to info handler while loading a NPI file
	 * @param additional_info
	 * @param handler
	 */
	public void additionalInfoToAnalysisInfoHandler(Vector<Object> additional_info, AnalysisInfoHandler handler);
	/**
	 * Move info from info handler to additional info before writing to NPI file
	 * @param info
	 * @param handler
	 */
	public void analysisInfoHandlerToAdditonalInfo(TraceAdditionalInfo info, AnalysisInfoHandler handler);
	/**
	 * @return String representation for additional info used in property page
	 */
	public String InfoHandlerToDisplayString(AnalysisInfoHandler handler);	
}
