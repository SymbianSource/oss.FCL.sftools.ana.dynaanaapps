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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Button Event Profile Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType#getMapping <em>Mapping</em>}</li>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType#getProfileId <em>Profile Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getButtonEventProfileType()
 * @model extendedMetaData="name='buttonEventProfile_._type' kind='elementOnly'"
 * @generated
 */
public interface ButtonEventProfileType extends EObject {
	/**
	 * Returns the value of the '<em><b>Mapping</b></em>' containment reference list.
	 * The list contents are of type {@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mapping</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mapping</em>' containment reference list.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getButtonEventProfileType_Mapping()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='mapping' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<MappingType> getMapping();

	/**
	 * Returns the value of the '<em><b>Profile Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Profile Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Profile Id</em>' attribute.
	 * @see #setProfileId(String)
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getButtonEventProfileType_ProfileId()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='profileId' namespace='##targetNamespace'"
	 * @generated
	 */
	String getProfileId();

	/**
	 * Sets the value of the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType#getProfileId <em>Profile Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Profile Id</em>' attribute.
	 * @see #getProfileId()
	 * @generated
	 */
	void setProfileId(String value);

} // ButtonEventProfileType
