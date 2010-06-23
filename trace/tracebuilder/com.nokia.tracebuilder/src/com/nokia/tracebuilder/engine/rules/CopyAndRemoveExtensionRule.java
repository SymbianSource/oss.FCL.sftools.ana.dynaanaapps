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
* Used be trace multipliers to move extensions into new traces
*
*/
package com.nokia.tracebuilder.engine.rules;

/**
 * Used be trace multipliers to move extensions into new traces. This extends
 * the copy extension rule and indicates that the rule needs to be removed from
 * the original object after it has been copied.
 * 
 */
public interface CopyAndRemoveExtensionRule extends CopyExtensionRule {

}
