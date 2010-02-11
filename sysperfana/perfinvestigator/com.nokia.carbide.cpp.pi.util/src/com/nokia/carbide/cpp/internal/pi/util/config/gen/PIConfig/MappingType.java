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

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Mapping Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getEnumString <em>Enum String</em>}</li>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getKeyCode <em>Key Code</em>}</li>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getLabel <em>Label</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getMappingType()
 * @model extendedMetaData="name='mapping_._type' kind='empty'"
 * @generated
 */
public interface MappingType extends EObject {
	/**
	 * Returns the value of the '<em><b>Enum String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Enum String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Enum String</em>' attribute.
	 * @see #setEnumString(String)
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getMappingType_EnumString()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='enumString' namespace='##targetNamespace'"
	 * @generated
	 */
	String getEnumString();

	/**
	 * Sets the value of the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getEnumString <em>Enum String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Enum String</em>' attribute.
	 * @see #getEnumString()
	 * @generated
	 */
	void setEnumString(String value);

	/**
	 * Returns the value of the '<em><b>Key Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Key Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Key Code</em>' attribute.
	 * @see #isSetKeyCode()
	 * @see #unsetKeyCode()
	 * @see #setKeyCode(long)
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getMappingType_KeyCode()
	 * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.UnsignedInt"
	 *        extendedMetaData="kind='attribute' name='keyCode' namespace='##targetNamespace'"
	 * @generated
	 */
	long getKeyCode();

	/**
	 * Sets the value of the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getKeyCode <em>Key Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Key Code</em>' attribute.
	 * @see #isSetKeyCode()
	 * @see #unsetKeyCode()
	 * @see #getKeyCode()
	 * @generated
	 */
	void setKeyCode(long value);

	/**
	 * Unsets the value of the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getKeyCode <em>Key Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetKeyCode()
	 * @see #getKeyCode()
	 * @see #setKeyCode(long)
	 * @generated
	 */
	void unsetKeyCode();

	/**
	 * Returns whether the value of the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getKeyCode <em>Key Code</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Key Code</em>' attribute is set.
	 * @see #unsetKeyCode()
	 * @see #getKeyCode()
	 * @see #setKeyCode(long)
	 * @generated
	 */
	boolean isSetKeyCode();

	/**
	 * Returns the value of the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Label</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Label</em>' attribute.
	 * @see #setLabel(String)
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#getMappingType_Label()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='label' namespace='##targetNamespace'"
	 * @generated
	 */
	String getLabel();

	/**
	 * Sets the value of the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getLabel <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Label</em>' attribute.
	 * @see #getLabel()
	 * @generated
	 */
	void setLabel(String value);

} // MappingType
