/*
 * Copyright (c) 2007-2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of "Eclipse Public License v1.0"
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
 * Spacer Field Editor
 *
 */
package com.nokia.traceviewer.engine.preferences;

import org.eclipse.swt.widgets.Composite;

/**
 * Spacer Field Editor
 * 
 */
public class SpacerFieldEditor extends LabelFieldEditor {

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 */
	public SpacerFieldEditor(Composite parent) {
		super("", parent); //$NON-NLS-1$
	}
}
