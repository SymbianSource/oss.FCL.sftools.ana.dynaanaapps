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

package com.nokia.carbide.cpp.internal.pi.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class TraceDataContainer 
{
	private Hashtable columns;
	private String name;
	
	private static class InternalColumn
	{		
		ArrayList columnData;

		InternalColumn(String columnName)
		{
			columnData = new ArrayList();
		}
	}
	
	public TraceDataContainer(String containerName, String[] initialColumnNames)
	{
		this.name = containerName;
		this.columns = new Hashtable();
		for (int i = 0; i < initialColumnNames.length; i++)
		{
			InternalColumn c = new InternalColumn(initialColumnNames[i]);
			this.columns.put(initialColumnNames[i], c);
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addDataToColumn(String columnName, Object data)
	{
		InternalColumn ic = (InternalColumn)this.columns.get(columnName);
		
		if (ic == null)
			throw new NullPointerException(Messages.getString("TraceDataContainer.columnNotFound1") + columnName + Messages.getString("TraceDataContainer.columnNotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
		ic.columnData.add(data);
	}
	
	public Iterator getColumn(String columnName)
	{
		InternalColumn ic = (InternalColumn)this.columns.get(columnName);
		
		if (ic == null) throw new NullPointerException(Messages.getString("TraceDataContainer.columnNotFound1") + columnName + Messages.getString("TraceDataContainer.columnNotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
		
		return ((InternalColumn)this.columns.get(columnName)).columnData.iterator();
	}
	
	private ArrayList getColumnInternal(String columnName)
	{
		InternalColumn ic = (InternalColumn)this.columns.get(columnName);
		
		if (ic == null)
			throw new NullPointerException(Messages.getString("TraceDataContainer.columnNotFound1") + columnName + Messages.getString("TraceDataContainer.columnNotFound2")); //$NON-NLS-1$ //$NON-NLS-2$
		
		return ((InternalColumn)this.columns.get(columnName)).columnData;		
	}
	
	public ArrayList getColumnMatch(String knownColumn, String searchedColumn, Object knownObject)
	{
		ArrayList c1 = this.getColumnInternal(knownColumn);
		ArrayList c2 = this.getColumnInternal(searchedColumn);
		
		ArrayList found = new ArrayList();
		
		for (int i = 0; i < c1.size(); i++)
		{
			 Object o = c1.get(i);
			 if (o.equals(knownObject))
			 {
				 if (i < c2.size())
				 {
					 found.add(c2.get(i));
				 }
			 }
		}
		return found;
	}
	
	public static void main(String[] a)
	{
		TraceDataContainer t = new TraceDataContainer("taulukko",new String[]{"nimi","puh","osoite"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		t.addDataToColumn("nimi","teemu"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("nimi","kaapo"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("nimi","erkki"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("nimi","kaapo"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("nimi","teemu"); //$NON-NLS-1$ //$NON-NLS-2$
		
		t.addDataToColumn("puh","1"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("puh","2"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("puh","3"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("puh","4"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("puh","24398243987"); //$NON-NLS-1$ //$NON-NLS-2$

		t.addDataToColumn("osoite","tie 1"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("osoite","tie 2"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("osoite","tie 3"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("osoite","tie 4"); //$NON-NLS-1$ //$NON-NLS-2$
		t.addDataToColumn("osoite","tie 243982473987"); //$NON-NLS-1$ //$NON-NLS-2$

		
		ArrayList al = t.getColumnMatch("nimi","osoite","teemu");		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Iterator i = al.iterator();
		while(i.hasNext())
		{
			System.out.println((String)i.next());
		}
			
	}
	
	
}
