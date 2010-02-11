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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileListType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.DocumentRoot;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.MappingType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigFactory;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PIConfigPackageImpl extends EPackageImpl implements PIConfigPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass buttonEventProfileListTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass buttonEventProfileTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass documentRootEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mappingTypeEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see com.nokia.carbide.cpp.pi.util.config.gen.PIConfig.PIConfigPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private PIConfigPackageImpl() {
		super(eNS_URI, PIConfigFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this
	 * model, and for any others upon which it depends.  Simple
	 * dependencies are satisfied by calling this method on all
	 * dependent packages before doing anything else.  This method drives
	 * initialization for interdependent packages directly, in parallel
	 * with this package, itself.
	 * <p>Of this package and its interdependencies, all packages which
	 * have not yet been registered by their URI values are first created
	 * and registered.  The packages are then initialized in two steps:
	 * meta-model objects for all of the packages are created before any
	 * are initialized, since one package's meta-model objects may refer to
	 * those of another.
	 * <p>Invocation of this method will not affect any packages that have
	 * already been initialized.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static PIConfigPackage init() {
		if (isInited) return (PIConfigPackage)EPackage.Registry.INSTANCE.getEPackage(PIConfigPackage.eNS_URI);

		// Obtain or create and register package
		PIConfigPackageImpl thePIConfigPackage = (PIConfigPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof PIConfigPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new PIConfigPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		XMLTypePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		thePIConfigPackage.createPackageContents();

		// Initialize created meta-data
		thePIConfigPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		thePIConfigPackage.freeze();

		return thePIConfigPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getButtonEventProfileListType() {
		return buttonEventProfileListTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getButtonEventProfileListType_ButtonEventProfile() {
		return (EReference)buttonEventProfileListTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getButtonEventProfileListType_ButtonEventProfileVersion() {
		return (EAttribute)buttonEventProfileListTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getButtonEventProfileType() {
		return buttonEventProfileTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getButtonEventProfileType_Mapping() {
		return (EReference)buttonEventProfileTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getButtonEventProfileType_ProfileId() {
		return (EAttribute)buttonEventProfileTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDocumentRoot() {
		return documentRootEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDocumentRoot_Mixed() {
		return (EAttribute)documentRootEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDocumentRoot_XMLNSPrefixMap() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDocumentRoot_XSISchemaLocation() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDocumentRoot_ButtonEventProfileList() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMappingType() {
		return mappingTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMappingType_EnumString() {
		return (EAttribute)mappingTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMappingType_KeyCode() {
		return (EAttribute)mappingTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMappingType_Label() {
		return (EAttribute)mappingTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PIConfigFactory getPIConfigFactory() {
		return (PIConfigFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		buttonEventProfileListTypeEClass = createEClass(BUTTON_EVENT_PROFILE_LIST_TYPE);
		createEReference(buttonEventProfileListTypeEClass, BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE);
		createEAttribute(buttonEventProfileListTypeEClass, BUTTON_EVENT_PROFILE_LIST_TYPE__BUTTON_EVENT_PROFILE_VERSION);

		buttonEventProfileTypeEClass = createEClass(BUTTON_EVENT_PROFILE_TYPE);
		createEReference(buttonEventProfileTypeEClass, BUTTON_EVENT_PROFILE_TYPE__MAPPING);
		createEAttribute(buttonEventProfileTypeEClass, BUTTON_EVENT_PROFILE_TYPE__PROFILE_ID);

		documentRootEClass = createEClass(DOCUMENT_ROOT);
		createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
		createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
		createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
		createEReference(documentRootEClass, DOCUMENT_ROOT__BUTTON_EVENT_PROFILE_LIST);

		mappingTypeEClass = createEClass(MAPPING_TYPE);
		createEAttribute(mappingTypeEClass, MAPPING_TYPE__ENUM_STRING);
		createEAttribute(mappingTypeEClass, MAPPING_TYPE__KEY_CODE);
		createEAttribute(mappingTypeEClass, MAPPING_TYPE__LABEL);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		XMLTypePackage theXMLTypePackage = (XMLTypePackage)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(buttonEventProfileListTypeEClass, ButtonEventProfileListType.class, "ButtonEventProfileListType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getButtonEventProfileListType_ButtonEventProfile(), this.getButtonEventProfileType(), null, "buttonEventProfile", null, 0, -1, ButtonEventProfileListType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getButtonEventProfileListType_ButtonEventProfileVersion(), theXMLTypePackage.getDecimal(), "buttonEventProfileVersion", null, 0, 1, ButtonEventProfileListType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(buttonEventProfileTypeEClass, ButtonEventProfileType.class, "ButtonEventProfileType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getButtonEventProfileType_Mapping(), this.getMappingType(), null, "mapping", null, 1, -1, ButtonEventProfileType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getButtonEventProfileType_ProfileId(), theXMLTypePackage.getString(), "profileId", null, 0, 1, ButtonEventProfileType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_ButtonEventProfileList(), this.getButtonEventProfileListType(), null, "buttonEventProfileList", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(mappingTypeEClass, MappingType.class, "MappingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMappingType_EnumString(), theXMLTypePackage.getString(), "enumString", null, 0, 1, MappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMappingType_KeyCode(), theXMLTypePackage.getUnsignedInt(), "keyCode", null, 0, 1, MappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMappingType_Label(), theXMLTypePackage.getString(), "label", null, 0, 1, MappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";		
		addAnnotation
		  (this, 
		   source, 
		   new String[] {
			 "qualified", "false"
		   });		
		addAnnotation
		  (buttonEventProfileListTypeEClass, 
		   source, 
		   new String[] {
			 "name", "buttonEventProfileList_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getButtonEventProfileListType_ButtonEventProfile(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "buttonEventProfile",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getButtonEventProfileListType_ButtonEventProfileVersion(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "buttonEventProfileVersion",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (buttonEventProfileTypeEClass, 
		   source, 
		   new String[] {
			 "name", "buttonEventProfile_._type",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getButtonEventProfileType_Mapping(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "mapping",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getButtonEventProfileType_ProfileId(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "profileId",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (documentRootEClass, 
		   source, 
		   new String[] {
			 "name", "",
			 "kind", "mixed"
		   });		
		addAnnotation
		  (getDocumentRoot_Mixed(), 
		   source, 
		   new String[] {
			 "kind", "elementWildcard",
			 "name", ":mixed"
		   });		
		addAnnotation
		  (getDocumentRoot_XMLNSPrefixMap(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "xmlns:prefix"
		   });		
		addAnnotation
		  (getDocumentRoot_XSISchemaLocation(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "xsi:schemaLocation"
		   });		
		addAnnotation
		  (getDocumentRoot_ButtonEventProfileList(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "buttonEventProfileList",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (mappingTypeEClass, 
		   source, 
		   new String[] {
			 "name", "mapping_._type",
			 "kind", "empty"
		   });		
		addAnnotation
		  (getMappingType_EnumString(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "enumString",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getMappingType_KeyCode(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "keyCode",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (getMappingType_Label(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "label",
			 "namespace", "##targetNamespace"
		   });
	}

} //PIConfigPackageImpl
