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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigFactory
 * @model kind="package"
 *        extendedMetaData="qualified='false'"
 * @generated
 */
public interface PIConfigPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	static final String E_NAME = "PIConfig";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	static final String E_NS_URI = "platform:/resource/com.nokia.carbide.cpp.pi.util/schema/PIConfig.xsd";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	static final String E_NS_PREFIX = "PIConfig";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	static final PIConfigPackage E_INSTANCE = com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl.init();

	/**
	 * The meta object id for the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileListTypeImpl <em>Button Event Profile List Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileListTypeImpl
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl#getButtonEventProfileListType()
	 * @generated
	 */
	static final int BUTTON_EVENT_PROFILE_LIST_TYPE = 0;

	/**
	 * The feature id for the '<em><b>Button Event Profile</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE = 0;

	/**
	 * The feature id for the '<em><b>Button Event Profile Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE_VERSION = 1;

	/**
	 * The number of structural features of the '<em>Button Event Profile List Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int BUTTON_EVENT_PROFILE_LIST_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileTypeImpl <em>Button Event Profile Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileTypeImpl
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl#getButtonEventProfileType()
	 * @generated
	 */
	static final int BUTTON_EVENT_PROFILE_TYPE = 1;

	/**
	 * The feature id for the '<em><b>Mapping</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int BUTTON_EVENT_PROFILE_TYPE__MAPPING = 0;

	/**
	 * The feature id for the '<em><b>Profile Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int BUTTON_EVENT_PROFILE_TYPE__PROFILE_ID = 1;

	/**
	 * The number of structural features of the '<em>Button Event Profile Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int BUTTON_EVENT_PROFILE_TYPE_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.DocumentRootImpl <em>Document Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.DocumentRootImpl
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl#getDocumentRoot()
	 * @generated
	 */
	static final int DOCUMENT_ROOT = 2;

	/**
	 * The feature id for the '<em><b>Mixed</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int DOCUMENT_ROOT__MIXED = 0;

	/**
	 * The feature id for the '<em><b>XMLNS Prefix Map</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int DOCUMENT_ROOT__XMLNS_PREFIX_MAP = 1;

	/**
	 * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = 2;

	/**
	 * The feature id for the '<em><b>Button Event Profile List</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int DOCUMENT_ROOT__BUTTON_EVENT_PROFILE_LIST = 3;

	/**
	 * The number of structural features of the '<em>Document Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int DOCUMENT_ROOT_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.MappingTypeImpl <em>Mapping Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.MappingTypeImpl
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl#getMappingType()
	 * @generated
	 */
	static final int MAPPING_TYPE = 3;

	/**
	 * The feature id for the '<em><b>Enum String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int MAPPING_TYPE__ENUM_STRING = 0;

	/**
	 * The feature id for the '<em><b>Key Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int MAPPING_TYPE__KEY_CODE = 1;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int MAPPING_TYPE__LABEL = 2;

	/**
	 * The number of structural features of the '<em>Mapping Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	static final int MAPPING_TYPE_FEATURE_COUNT = 3;


	/**
	 * Returns the meta object for class '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType <em>Button Event Profile List Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Button Event Profile List Type</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType
	 * @generated
	 */
	EClass getButtonEventProfileListType();

	/**
	 * Returns the meta object for the containment reference list '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType#getButtonEventProfile <em>Button Event Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Button Event Profile</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType#getButtonEventProfile()
	 * @see #getButtonEventProfileListType()
	 * @generated
	 */
	EReference getButtonEventProfileListType_ButtonEventProfile();

	/**
	 * Returns the meta object for the attribute '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType#getButtonEventProfileVersion <em>Button Event Profile Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Button Event Profile Version</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileListType#getButtonEventProfileVersion()
	 * @see #getButtonEventProfileListType()
	 * @generated
	 */
	EAttribute getButtonEventProfileListType_ButtonEventProfileVersion();

	/**
	 * Returns the meta object for class '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType <em>Button Event Profile Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Button Event Profile Type</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType
	 * @generated
	 */
	EClass getButtonEventProfileType();

	/**
	 * Returns the meta object for the containment reference list '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType#getMapping <em>Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Mapping</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType#getMapping()
	 * @see #getButtonEventProfileType()
	 * @generated
	 */
	EReference getButtonEventProfileType_Mapping();

	/**
	 * Returns the meta object for the attribute '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType#getProfileId <em>Profile Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Profile Id</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.ButtonEventProfileType#getProfileId()
	 * @see #getButtonEventProfileType()
	 * @generated
	 */
	EAttribute getButtonEventProfileType_ProfileId();

	/**
	 * Returns the meta object for class '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document Root</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot
	 * @generated
	 */
	EClass getDocumentRoot();

	/**
	 * Returns the meta object for the attribute list '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot#getMixed <em>Mixed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Mixed</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot#getMixed()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EAttribute getDocumentRoot_Mixed();

	/**
	 * Returns the meta object for the map '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot#getXMLNSPrefixMap()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_XMLNSPrefixMap();

	/**
	 * Returns the meta object for the map '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XSI Schema Location</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot#getXSISchemaLocation()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_XSISchemaLocation();

	/**
	 * Returns the meta object for the containment reference '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot#getButtonEventProfileList <em>Button Event Profile List</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Button Event Profile List</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.DocumentRoot#getButtonEventProfileList()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	EReference getDocumentRoot_ButtonEventProfileList();

	/**
	 * Returns the meta object for class '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType <em>Mapping Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Mapping Type</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType
	 * @generated
	 */
	EClass getMappingType();

	/**
	 * Returns the meta object for the attribute '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getEnumString <em>Enum String</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Enum String</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getEnumString()
	 * @see #getMappingType()
	 * @generated
	 */
	EAttribute getMappingType_EnumString();

	/**
	 * Returns the meta object for the attribute '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getKeyCode <em>Key Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key Code</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getKeyCode()
	 * @see #getMappingType()
	 * @generated
	 */
	EAttribute getMappingType_KeyCode();

	/**
	 * Returns the meta object for the attribute '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getLabel <em>Label</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Label</em>'.
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.MappingType#getLabel()
	 * @see #getMappingType()
	 * @generated
	 */
	EAttribute getMappingType_Label();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	PIConfigFactory getPIConfigFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileListTypeImpl <em>Button Event Profile List Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileListTypeImpl
		 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl#getButtonEventProfileListType()
		 * @generated
		 */
		EClass BUTTON_EVENT_PROFILE_LIST_TYPE = E_INSTANCE.getButtonEventProfileListType();

		/**
		 * The meta object literal for the '<em><b>Button Event Profile</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE = E_INSTANCE.getButtonEventProfileListType_ButtonEventProfile();

		/**
		 * The meta object literal for the '<em><b>Button Event Profile Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE_VERSION = E_INSTANCE.getButtonEventProfileListType_ButtonEventProfileVersion();

		/**
		 * The meta object literal for the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileTypeImpl <em>Button Event Profile Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.ButtonEventProfileTypeImpl
		 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl#getButtonEventProfileType()
		 * @generated
		 */
		EClass BUTTON_EVENT_PROFILE_TYPE = E_INSTANCE.getButtonEventProfileType();

		/**
		 * The meta object literal for the '<em><b>Mapping</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BUTTON_EVENT_PROFILE_TYPE__MAPPING = E_INSTANCE.getButtonEventProfileType_Mapping();

		/**
		 * The meta object literal for the '<em><b>Profile Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BUTTON_EVENT_PROFILE_TYPE__PROFILE_ID = E_INSTANCE.getButtonEventProfileType_ProfileId();

		/**
		 * The meta object literal for the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.DocumentRootImpl <em>Document Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.DocumentRootImpl
		 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl#getDocumentRoot()
		 * @generated
		 */
		EClass DOCUMENT_ROOT = E_INSTANCE.getDocumentRoot();

		/**
		 * The meta object literal for the '<em><b>Mixed</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DOCUMENT_ROOT__MIXED = E_INSTANCE.getDocumentRoot_Mixed();

		/**
		 * The meta object literal for the '<em><b>XMLNS Prefix Map</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__XMLNS_PREFIX_MAP = E_INSTANCE.getDocumentRoot_XMLNSPrefixMap();

		/**
		 * The meta object literal for the '<em><b>XSI Schema Location</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = E_INSTANCE.getDocumentRoot_XSISchemaLocation();

		/**
		 * The meta object literal for the '<em><b>Button Event Profile List</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_ROOT__BUTTON_EVENT_PROFILE_LIST = E_INSTANCE.getDocumentRoot_ButtonEventProfileList();

		/**
		 * The meta object literal for the '{@link com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.MappingTypeImpl <em>Mapping Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.MappingTypeImpl
		 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.impl.PIConfigPackageImpl#getMappingType()
		 * @generated
		 */
		EClass MAPPING_TYPE = E_INSTANCE.getMappingType();

		/**
		 * The meta object literal for the '<em><b>Enum String</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MAPPING_TYPE__ENUM_STRING = E_INSTANCE.getMappingType_EnumString();

		/**
		 * The meta object literal for the '<em><b>Key Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MAPPING_TYPE__KEY_CODE = E_INSTANCE.getMappingType_KeyCode();

		/**
		 * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MAPPING_TYPE__LABEL = E_INSTANCE.getMappingType_Label();

	}

} //PIConfigPackage
