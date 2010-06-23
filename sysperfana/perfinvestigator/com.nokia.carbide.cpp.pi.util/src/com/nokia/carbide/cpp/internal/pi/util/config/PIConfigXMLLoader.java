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

package com.nokia.carbide.cpp.internal.pi.util.config;

import java.io.IOException;
import java.net.URL;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileListType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.DocumentRoot;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigFactory;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigPackage;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.util.PIConfigResourceFactoryImpl;


public class PIConfigXMLLoader {
	
	static public ButtonEventProfileListType loadPiSettings (URL url) throws IOException {
		if (url == null)
			return null;

		// blank file could cause IOException, which is unnecessary. Just return blank model
		if (url.openStream().available() == 0) {
			return PIConfigFactory.E_INSTANCE.createButtonEventProfileListType();
		}

		URI xmlURI = URI.createURI(url.toString());

		PIConfigResourceFactoryImpl resFactory = new PIConfigResourceFactoryImpl();
		Resource r = resFactory.createResource(xmlURI);

		r.load(null);
		EList<EObject> contents = r.getContents();
	
		DocumentRoot root = (DocumentRoot) contents.get(0);
		ButtonEventProfileListType list = root.getButtonEventProfileList();
		
		return list;

	}
	
	static public boolean writePiSettings(ButtonEventProfileListType list, URL url) throws IOException {
		if (url == null)
			return false;
		URI xmlURI = URI.createURI(url.toString());
	
		PIConfigResourceFactoryImpl resFactory = new PIConfigResourceFactoryImpl();
		Resource r = resFactory.createResource(xmlURI);
		EList<EObject> contents = r.getContents();
	
		PIConfigFactory factory = PIConfigPackage.E_INSTANCE.getPIConfigFactory();
		DocumentRoot root = factory.createDocumentRoot();
		root.setButtonEventProfileList(list);
		contents.add(root);
						
		// write to disk
		r.save(null);
		return true;

	}
}
