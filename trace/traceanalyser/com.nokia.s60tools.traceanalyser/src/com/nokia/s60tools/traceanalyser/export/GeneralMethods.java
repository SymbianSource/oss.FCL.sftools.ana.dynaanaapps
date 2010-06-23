/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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
*/



package com.nokia.s60tools.traceanalyser.export;

public class GeneralMethods {
    /**
     * getTextBetweenQuotes.
     * Returns text between two first quotation marks on line that is give as parameter
     * @param line where quotations are searched.
     * @return text between quotation marks
     */
    public static String getTextBetweenQuotes(String line){
    	int index = 0;
    	if((index =line.indexOf("\"")) > -1){
			line = line.substring(index+1);
    		if((index =line.indexOf("\"")) > -1){
    			return line.substring(0,index); 
    		}
		}
    	return null;
    }
}
