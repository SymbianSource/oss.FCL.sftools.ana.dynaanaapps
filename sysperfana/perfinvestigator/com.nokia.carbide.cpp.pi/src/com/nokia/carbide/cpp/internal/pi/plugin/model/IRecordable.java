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

package com.nokia.carbide.cpp.internal.pi.plugin.model;

import java.io.Serializable;

public interface IRecordable extends ITrace
{
/*	
 * The Serializable object this method returns is saved in to the .bap file when
 * analysis is saved. The Serializable object shouldn't contain plugins
 * trace data since it is automatically saved through a different mechanism.
 */
	public Serializable getAdditionalData(); 
								 
/*
 * AbstractPiPlugin receives the saved Serializable object via this method when .bap file is loaded.
 */	
	public void setAdditionalData(int graphIndex, Serializable data);
	
	public int getGraphCount();
}
