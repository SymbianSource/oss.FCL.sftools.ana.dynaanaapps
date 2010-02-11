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

package com.nokia.carbide.cpp.internal.pi.analyser;

public abstract class AnalyserException extends Exception {
	public AnalyserException() {}
	public AnalyserException(String msg)
	{
		super(msg);
	}
}

class SymbolFileException extends AnalyserException {
	private static final long serialVersionUID = 8733886871246504119L;
	public SymbolFileException() {}
		public SymbolFileException(String msg) {
			super(msg);
		}
	}

class DatFileException extends AnalyserException {
	private static final long serialVersionUID = -437564833942616104L;
	public DatFileException() {}
		public DatFileException(String msg) {
			super(msg);
		}
	}
