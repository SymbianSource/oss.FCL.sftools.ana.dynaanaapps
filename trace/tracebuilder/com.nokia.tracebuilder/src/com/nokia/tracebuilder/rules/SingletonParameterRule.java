/*
* Copyright (c) 2007 Nokia Corporation and/or its subsidiary(-ies). 
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
* Rule for singleton parameters
*
*/
package com.nokia.tracebuilder.rules;

import com.nokia.tracebuilder.model.TraceObjectRule;

/**
 * Rule which marks a parameter type as singleton. If a parameter contains an
 * extension implementing this interface, a second parameter which contains the
 * same type of extension cannot be added. The extension type is determined by
 * <code>existingExtension.getClass().equals(newExtension.getClass())</code>
 * 
 */
public interface SingletonParameterRule extends TraceObjectRule {
}
