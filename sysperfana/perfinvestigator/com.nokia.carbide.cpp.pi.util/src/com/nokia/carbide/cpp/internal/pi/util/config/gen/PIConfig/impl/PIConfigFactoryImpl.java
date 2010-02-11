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

package com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileListType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.DocumentRoot;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.MappingType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigFactory;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PIConfigFactoryImpl extends EFactoryImpl implements PIConfigFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static PIConfigFactory init() {
		try {
			PIConfigFactory thePIConfigFactory = (PIConfigFactory)EPackage.Registry.INSTANCE.getEFactory("platform:/resource/com.nokia.carbide.cpp.pi.util/schema/PIConfig.xsd"); 
			if (thePIConfigFactory != null) {
				return thePIConfigFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new PIConfigFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PIConfigFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE: return createButtonEventProfileListType();
			case PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE: return createButtonEventProfileType();
			case PIConfigPackage.DOCUMENT_ROOT: return createDocumentRoot();
			case PIConfigPackage.MAPPING_TYPE: return createMappingType();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ButtonEventProfileListType createButtonEventProfileListType() {
		ButtonEventProfileListTypeImpl buttonEventProfileListType = new ButtonEventProfileListTypeImpl();
		return buttonEventProfileListType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ButtonEventProfileType createButtonEventProfileType() {
		ButtonEventProfileTypeImpl buttonEventProfileType = new ButtonEventProfileTypeImpl();
		return buttonEventProfileType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DocumentRoot createDocumentRoot() {
		DocumentRootImpl documentRoot = new DocumentRootImpl();
		return documentRoot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MappingType createMappingType() {
		MappingTypeImpl mappingType = new MappingTypeImpl();
		return mappingType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PIConfigPackage getPIConfigPackage() {
		return (PIConfigPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static PIConfigPackage getPackage() {
		return PIConfigPackage.eINSTANCE;
	}

} //PIConfigFactoryImpl
