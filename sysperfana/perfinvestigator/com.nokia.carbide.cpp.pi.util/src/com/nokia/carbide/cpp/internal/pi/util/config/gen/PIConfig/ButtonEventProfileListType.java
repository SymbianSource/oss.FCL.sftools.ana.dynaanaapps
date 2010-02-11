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

package com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig;

import java.math.BigDecimal;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Button Event Profile List Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType#getButtonEventProfile <em>Button Event Profile</em>}</li>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType#getButtonEventProfileVersion <em>Button Event Profile Version</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getButtonEventProfileListType()
 * @model extendedMetaData="name='buttonEventProfileList_._type' kind='elementOnly'"
 * @generated
 */
public interface ButtonEventProfileListType extends EObject {
	/**
	 * Returns the value of the '<em><b>Button Event Profile</b></em>' containment reference list.
	 * The list contents are of type {@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Button Event Profile</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Button Event Profile</em>' containment reference list.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getButtonEventProfileListType_ButtonEventProfile()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='buttonEventProfile' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<ButtonEventProfileType> getButtonEventProfile();

	/**
	 * Returns the value of the '<em><b>Button Event Profile Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Button Event Profile Version</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Button Event Profile Version</em>' attribute.
	 * @see #setButtonEventProfileVersion(BigDecimal)
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getButtonEventProfileListType_ButtonEventProfileVersion()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='attribute' name='buttonEventProfileVersion' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getButtonEventProfileVersion();

	/**
	 * Sets the value of the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType#getButtonEventProfileVersion <em>Button Event Profile Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Button Event Profile Version</em>' attribute.
	 * @see #getButtonEventProfileVersion()
	 * @generated
	 */
	void setButtonEventProfileVersion(BigDecimal value);

} // ButtonEventProfileListType
