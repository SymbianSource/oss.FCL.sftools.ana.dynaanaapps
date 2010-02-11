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

import java.math.BigDecimal;
import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileListType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Button Event Profile List Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileListTypeImpl#getButtonEventProfile <em>Button Event Profile</em>}</li>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileListTypeImpl#getButtonEventProfileVersion <em>Button Event Profile Version</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ButtonEventProfileListTypeImpl extends EObjectImpl implements ButtonEventProfileListType {
	/**
	 * The cached value of the '{@link #getButtonEventProfile() <em>Button Event Profile</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getButtonEventProfile()
	 * @generated
	 * @ordered
	 */
	protected EList<ButtonEventProfileType> buttonEventProfile;

	/**
	 * The default value of the '{@link #getButtonEventProfileVersion() <em>Button Event Profile Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getButtonEventProfileVersion()
	 * @generated
	 * @ordered
	 */
	protected static final BigDecimal BUTTON_EVENT_PROFILE_VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getButtonEventProfileVersion() <em>Button Event Profile Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getButtonEventProfileVersion()
	 * @generated
	 * @ordered
	 */
	protected BigDecimal buttonEventProfileVersion = BUTTON_EVENT_PROFILE_VERSION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ButtonEventProfileListTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PIConfigPackage.Literals.BUTTON_EVENT_PROFILE_LIST_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ButtonEventProfileType> getButtonEventProfile() {
		if (buttonEventProfile == null) {
			buttonEventProfile = new EObjectContainmentEList<ButtonEventProfileType>(ButtonEventProfileType.class, this, PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE);
		}
		return buttonEventProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BigDecimal getButtonEventProfileVersion() {
		return buttonEventProfileVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setButtonEventProfileVersion(BigDecimal newButtonEventProfileVersion) {
		BigDecimal oldButtonEventProfileVersion = buttonEventProfileVersion;
		buttonEventProfileVersion = newButtonEventProfileVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE_VERSION, oldButtonEventProfileVersion, buttonEventProfileVersion));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE:
				return ((InternalEList<?>)getButtonEventProfile()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE:
				return getButtonEventProfile();
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE_VERSION:
				return getButtonEventProfileVersion();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE:
				getButtonEventProfile().clear();
				getButtonEventProfile().addAll((Collection<? extends ButtonEventProfileType>)newValue);
				return;
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE_VERSION:
				setButtonEventProfileVersion((BigDecimal)newValue);
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
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE:
				getButtonEventProfile().clear();
				return;
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE_VERSION:
				setButtonEventProfileVersion(BUTTON_EVENT_PROFILE_VERSION_EDEFAULT);
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
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE:
				return buttonEventProfile != null && !buttonEventProfile.isEmpty();
			case PIConfigPackage.BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE_VERSION:
				return BUTTON_EVENT_PROFILE_VERSION_EDEFAULT == null ? buttonEventProfileVersion != null : !BUTTON_EVENT_PROFILE_VERSION_EDEFAULT.equals(buttonEventProfileVersion);
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
		result.append(" (buttonEventProfileVersion: ");
		result.append(buttonEventProfileVersion);
		result.append(')');
		return result.toString();
	}

} //ButtonEventProfileListTypeImpl
