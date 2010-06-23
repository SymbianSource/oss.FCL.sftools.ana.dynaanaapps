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
 * Base class for importing configuration to XML file
 *
 */
package com.nokia.traceviewer.engine.preferences;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.nokia.traceviewer.TraceViewerPlugin;
import com.nokia.traceviewer.dialog.treeitem.TreeItem;
import com.nokia.traceviewer.engine.TraceViewerGlobals;

/**
 * Base class for importing configuration to XML file
 * 
 */
public abstract class XMLBaseConfigurationImporter implements
		XMLConfigurationFileConstants {

	/**
	 * Root tree item
	 */
	protected TreeItem root;

	/**
	 * File path
	 */
	protected String filePath;

	/**
	 * Constructor
	 * 
	 * @param root
	 *            root of the elements
	 * @param configurationFileName
	 *            file name where to import data from
	 * @param pathRelative
	 *            tells is the path relative
	 */
	public XMLBaseConfigurationImporter(TreeItem root,
			String configurationFileName, boolean pathRelative) {
		this.root = root;

		// If path is relative, get plugins metadata folder
		if (pathRelative) {
			IPath path = TraceViewerPlugin.getDefault().getStateLocation()
					.append(configurationFileName);
			filePath = path.toOSString();
		} else {
			filePath = configurationFileName;
		}
	}

	/**
	 * Gets DOM document for the file path
	 * 
	 * @return document
	 */
	protected Document getDocument() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		// Get the document
		Document doc = null;
		try {
			if (docBuilder != null) {
				File file = new File(filePath);
				doc = docBuilder.parse(file);
			}
		} catch (SAXException e1) {
			e1.printStackTrace();

			// Inform about import unsuccesful
			String importFailed = Messages
					.getString("XMLBaseConfigurationImporter.IncorrectFileErrorMsg"); //$NON-NLS-1$
			TraceViewerGlobals.getTraceViewer().getDialogs().showErrorMessage(
					importFailed);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return doc;
	}
}
