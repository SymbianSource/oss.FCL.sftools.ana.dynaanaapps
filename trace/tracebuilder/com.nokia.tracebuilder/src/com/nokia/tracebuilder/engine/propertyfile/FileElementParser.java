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
* FIle element parser
*
*/
package com.nokia.tracebuilder.engine.propertyfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Element;

import com.nokia.tracebuilder.engine.TraceBuilderGlobals;
import com.nokia.tracebuilder.engine.project.ProjectEngine;
import com.nokia.tracebuilder.engine.source.SourceEngine;
import com.nokia.tracebuilder.model.TraceBuilderException;

/**
 * File element parser
 * 
 */
final class FileElementParser implements PropertyFileElementParser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tracebuilder.engine.propertyfile.PropertyFileElementParser#
	 *      parse(java.lang.Object, org.w3c.dom.Element)
	 */
	public void parse(Object owner, Element element)
			throws TraceBuilderException {
		String filePath = element.getTextContent();
		SourceEngine sourceEngine = TraceBuilderGlobals.getSourceEngine();
		ArrayList<String> list = sourceEngine.getNonSourceFiles();

		String projectPath = TraceBuilderGlobals.getProjectPath();

		File file = new File(projectPath + File.separatorChar
				+ ProjectEngine.traceFolderName + File.separatorChar + filePath);

		try {
			filePath = file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!list.contains(filePath)) {
			sourceEngine.addNonSourceFile(filePath);
		}
	}
}