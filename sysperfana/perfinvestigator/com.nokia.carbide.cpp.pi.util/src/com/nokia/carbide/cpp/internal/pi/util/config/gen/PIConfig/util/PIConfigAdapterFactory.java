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

package com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileListType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.DocumentRoot;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.MappingType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage
 * @generated
 */
public class PIConfigAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static PIConfigPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PIConfigAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = PIConfigPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PIConfigSwitch<Adapter> modelSwitch =
		new PIConfigSwitch<Adapter>() {
			@Override
			public Adapter caseButtonEventProfileListType(ButtonEventProfileListType object) {
				return createButtonEventProfileListTypeAdapter();
			}
			@Override
			public Adapter caseButtonEventProfileType(ButtonEventProfileType object) {
				return createButtonEventProfileTypeAdapter();
			}
			@Override
			public Adapter caseDocumentRoot(DocumentRoot object) {
				return createDocumentRootAdapter();
			}
			@Override
			public Adapter caseMappingType(MappingType object) {
				return createMappingTypeAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType <em>Button Event Profile List Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType
	 * @generated
	 */
	public Adapter createButtonEventProfileListTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType <em>Button Event Profile Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType
	 * @generated
	 */
	public Adapter createButtonEventProfileTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot
	 * @generated
	 */
	public Adapter createDocumentRootAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType <em>Mapping Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType
	 * @generated
	 */
	public Adapter createMappingTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //PIConfigAdapterFactory
