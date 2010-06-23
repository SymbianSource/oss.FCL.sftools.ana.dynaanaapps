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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.MappingType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Mapping Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.MappingTypeImpl#getEnumString <em>Enum String</em>}</li>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.MappingTypeImpl#getKeyCode <em>Key Code</em>}</li>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.MappingTypeImpl#getLabel <em>Label</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MappingTypeImpl extends EObjectImpl implements MappingType {
	/**
	 * The default value of the '{@link #getEnumString() <em>Enum String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnumString()
	 * @generated
	 * @ordered
	 */
	protected static final String ENUM_STRING_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEnumString() <em>Enum String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnumString()
	 * @generated
	 * @ordered
	 */
	protected String enumString = ENUM_STRING_EDEFAULT;

	/**
	 * The default value of the '{@link #getKeyCode() <em>Key Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeyCode()
	 * @generated
	 * @ordered
	 */
	protected static final long KEY_CODE_EDEFAULT = 0L;

	/**
	 * The cached value of the '{@link #getKeyCode() <em>Key Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeyCode()
	 * @generated
	 * @ordered
	 */
	protected long keyCode = KEY_CODE_EDEFAULT;

	/**
	 * This is true if the Key Code attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean keyCodeESet;

	/**
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected String label = LABEL_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MappingTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PIConfigPackage.Literals.MAPPING_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getEnumString() {
		return enumString;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEnumString(String newEnumString) {
		String oldEnumString = enumString;
		enumString = newEnumString;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PIConfigPackage.MAPPING_TYPE__ENUM_STRING, oldEnumString, enumString));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public long getKeyCode() {
		return keyCode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setKeyCode(long newKeyCode) {
		long oldKeyCode = keyCode;
		keyCode = newKeyCode;
		boolean oldKeyCodeESet = keyCodeESet;
		keyCodeESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PIConfigPackage.MAPPING_TYPE__KEY_CODE, oldKeyCode, keyCode, !oldKeyCodeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetKeyCode() {
		long oldKeyCode = keyCode;
		boolean oldKeyCodeESet = keyCodeESet;
		keyCode = KEY_CODE_EDEFAULT;
		keyCodeESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, PIConfigPackage.MAPPING_TYPE__KEY_CODE, oldKeyCode, KEY_CODE_EDEFAULT, oldKeyCodeESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetKeyCode() {
		return keyCodeESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PIConfigPackage.MAPPING_TYPE__LABEL, oldLabel, label));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PIConfigPackage.MAPPING_TYPE__ENUM_STRING:
				return getEnumString();
			case PIConfigPackage.MAPPING_TYPE__KEY_CODE:
				return Long.valueOf(getKeyCode());
			case PIConfigPackage.MAPPING_TYPE__LABEL:
				return getLabel();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PIConfigPackage.MAPPING_TYPE__ENUM_STRING:
				setEnumString((String)newValue);
				return;
			case PIConfigPackage.MAPPING_TYPE__KEY_CODE:
				setKeyCode(((Long)newValue).longValue());
				return;
			case PIConfigPackage.MAPPING_TYPE__LABEL:
				setLabel((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case PIConfigPackage.MAPPING_TYPE__ENUM_STRING:
				setEnumString(ENUM_STRING_EDEFAULT);
				return;
			case PIConfigPackage.MAPPING_TYPE__KEY_CODE:
				unsetKeyCode();
				return;
			case PIConfigPackage.MAPPING_TYPE__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case PIConfigPackage.MAPPING_TYPE__ENUM_STRING:
				return ENUM_STRING_EDEFAULT == null ? enumString != null : !ENUM_STRING_EDEFAULT.equals(enumString);
			case PIConfigPackage.MAPPING_TYPE__KEY_CODE:
				return isSetKeyCode();
			case PIConfigPackage.MAPPING_TYPE__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (enumString: ");
		result.append(enumString);
		result.append(", keyCode: ");
		if (keyCodeESet) result.append(keyCode); else result.append("<unset>");
		result.append(", label: ");
		result.append(label);
		result.append(')');
		return result.toString();
	}

} //MappingTypeImpl
