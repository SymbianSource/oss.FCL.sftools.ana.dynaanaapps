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

package com.nokia.carbide.cpp.pi.instr;

import com.nokia.carbide.cpp.internal.pi.model.Binary;


public class BinaryReaderResult
{
  public Binary[] possibleBinaries;
  public Boolean[] checksumValues;

  public String toString()
  {
    String s = ""; //$NON-NLS-1$
    for (int i=0;i<possibleBinaries.length;i++)
    {
      s = s+"\n"+possibleBinaries[i].getBinaryName()+" @ "+possibleBinaries[i].getStartAddress()+" "+checksumValues[i].booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return s;
  }

  public boolean hasAnyChecksumCorrectValues()
  {
    for (int i=0;i<this.checksumValues.length;i++)
    {
      if (this.checksumValues[i].booleanValue() == true) return true;
    }
    return false;
  }

}
