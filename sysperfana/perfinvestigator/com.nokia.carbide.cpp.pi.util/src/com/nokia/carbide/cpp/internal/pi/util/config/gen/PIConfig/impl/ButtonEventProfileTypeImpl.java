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

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.MappingType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Button Event Profile Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileTypeImpl#getMapping <em>Mapping</em>}</li>
 *   <li>{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileTypeImpl#getProfileId <em>Profile Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ButtonEventProfileTypeImpl extends EObjectImpl implements ButtonEventProfileType {
	/**
	 * The cached value of the '{@link #getMapping() <em>Mapping</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMapping()
	 * @generated
	 * @ordered
	 */
	protected EList<MappingType> mapping;

	/**
	 * The default value of the '{@link #getProfileId() <em>Profile Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProfileId()
	 * @generated
	 * @ordered
	 */
	protected static final String PROFILE_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getProfileId() <em>Profile Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProfileId()
	 * @generated
	 * @ordered
	 */
	protected String profileId = PROFILE_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ButtonEventProfileTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PIConfigPackage.Literals.BUTTON_EVENT_PROFILE_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MappingType> getMapping() {
		if (mapping == null) {
			mapping = new EObjectContainmentEList<MappingType>(MappingType.class, this, PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__MAPPING);
		}
		return mapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getProfileId() {
		return profileId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProfileId(String newProfileId) {
		String oldProfileId = profileId;
		profileId = newProfileId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__PROFILE_ID, oldProfileId, profileId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__MAPPING){
			return ((InternalEList<?>)getMapping()).basicRemove(otherEnd, msgs);
		}else{					
			return super.eInverseRemove(otherEnd, featureID, msgs);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__MAPPING){
			return getMapping();
		}else if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__PROFILE_ID){
			return getProfileId();
		}else{					
			return super.eGet(featureID, resolve, coreType);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__MAPPING){
			getMapping().clear();
			getMapping().addAll((Collection<? extends MappingType>)newValue);
		}else if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__PROFILE_ID){
			setProfileId((String)newValue);
		}else{					
			super.eSet(featureID, newValue);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__MAPPING){
			getMapping().clear();
		}if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__PROFILE_ID){
			setProfileId(PROFILE_ID_EDEFAULT);
		}else{					
			super.eUnset(featureID);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__MAPPING){
			return mapping != null && !mapping.isEmpty();
		}else if(featureID == PIConfigPackage.BUTTON_EVENT_PROFILE_TYPE__PROFILE_ID){
			return PROFILE_ID_EDEFAULT == null ? profileId != null : !PROFILE_ID_EDEFAULT.equals(profileId);
		}else{					
			return super.eIsSet(featureID);
		}
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
		result.append(" (profileId: ");
		result.append(profileId);
		result.append(')');
		return result.toString();
	}

} //ButtonEventProfileTypeImpl
