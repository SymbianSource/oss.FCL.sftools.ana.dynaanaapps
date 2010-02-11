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

/*
 * GppTableSorter.java
 */
package com.nokia.carbide.cpp.pi.address;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.ProfiledFunction;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledThread;


public class GppTableSorter
{
	private Vector<ProfiledGeneric> sortedList = null;
//	private int columnID;
	private int graphIndex;
	private boolean sortAscending;
	
	public void setupSort(int columnID, int graphIndex, boolean sortAscending)
	{
//		this.columnID      = columnID;
		this.graphIndex    = graphIndex;
		this.sortAscending = sortAscending;
	}

    public boolean isSortedByLoad(Vector<ProfiledGeneric> v)
	{
    	//	  check if ProfiledGeneric vector is already sorted by load
	  	int i = 0;
		ProfiledGeneric tmp1 = null;
		ProfiledGeneric tmp2 = null;
		for (Enumeration e = v.elements(); e.hasMoreElements() ;i++)
		{
			tmp2 = (ProfiledGeneric)e.nextElement();
			if (tmp1 != null)
			{
	      		float first  = tmp1.getPercentLoad(graphIndex);
	      		float second = tmp2.getPercentLoad(graphIndex);

	      		if ((first != second) && ((first < second) ^ sortAscending))
				{
					return false;
				}
			}
			tmp1 = (ProfiledGeneric)v.elementAt(i);
		}
		return true;
	}

	public void quickSortByShow(Vector<ProfiledGeneric> elements)
	{
		Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortByShow(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByAverageLoad(Vector<ProfiledGeneric> elements)
	{
	  	Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty() & !isSortedByLoad(v))
	    {
	  		this.quickSortByAverageLoad(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByName(Vector<ProfiledGeneric> elements, boolean caseSensitive)
	{
		Vector<ProfiledGeneric> v = elements;
	  	if (!v.isEmpty())
	    {
	  		this.quickSortByName(v, 0, v.size()-1, caseSensitive);
	    }

	  	sortedList = v;
	}

	public void quickSortByThread(Vector<ProfiledGeneric> elements)
	{
		quickSortByName(elements, false);
	}

	public void quickSortByFunction(Vector<ProfiledGeneric> elements)
	{
		quickSortByName(elements, false);
	}

	public void quickSortByBinary(Vector<ProfiledGeneric> elements)
	{
		Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortByBinary(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByBinaryPath(Vector<ProfiledGeneric> elements)
	{
		Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortByBinaryPath(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByFullBinaryPath(Vector<ProfiledGeneric> elements)
	{
		Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortByFullBinaryPath(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByStartAddress(Vector<ProfiledGeneric> elements)
	{
		Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortByStartAddress(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByAssocBinary(Vector<ProfiledGeneric> elements)
	{
		Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortByAssocBinary(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByAssocBinaryPath(Vector<ProfiledGeneric> elements)
	{
		Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortByAssocBinaryPath(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByFullAssocBinaryPath(Vector<ProfiledGeneric> elements)
	{
		Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortByFullAssocBinaryPath(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortBySampleCount(Vector<ProfiledGeneric> elements)
	{
	  	Vector<ProfiledGeneric> v = elements;

	  	if (!v.isEmpty())
	    {
	  		this.quickSortBySampleCount(v, 0, v.size()-1);
	    }

	  	sortedList = v;
	}

	public void quickSortByPriority(Vector<ProfiledGeneric> elements, Hashtable priorities)
	{
	    sortedList = new Vector<ProfiledGeneric>();
	    Vector<ProfiledGeneric> v = elements;
	  	int i = 0;
		ProfiledThread tmp1 = null;
		ProfiledThread tmp2 = null;
		for (Enumeration e = v.elements(); e.hasMoreElements() ;i++)
		{
			tmp2 = (ProfiledThread)e.nextElement();
			if (tmp1 != null)
			{
				Integer p1 = (Integer)priorities.get(new Integer(tmp1.getThreadId()));
				Integer p2 = (Integer)priorities.get(new Integer(tmp2.getThreadId()));
				p1 = (p1 == null) ? new Integer(Integer.MIN_VALUE) : p1;
				p2 = (p2 == null) ? new Integer(Integer.MIN_VALUE) : p2;

				if ((p1.compareTo(p2) != 0) && (p1.compareTo(p2) < 0) ^ sortAscending)
				{
					quickSortByPriority(v, 0, v.size()-1, priorities);

					sortedList = v;
					return;
				}
			}
			tmp1 = (ProfiledThread)v.elementAt(i);
		}
		sortedList = v;
		return;
	}

	private void quickSortByShow(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledGeneric pivotValue;  // change the values to suit your application
	    ProfiledGeneric lowToHighValue;
	    ProfiledGeneric highToLowValue;
	    ProfiledGeneric parking;
	    int newLowIndex;
	    int newHighIndex;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledGeneric)elements.elementAt(pivotIndex);

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
				// chose false is less than true
				// when sortAscending is true, sort from false to true
	      		boolean first  = lowToHighValue.isEnabled(graphIndex);
	      		boolean second = pivotValue.isEnabled(graphIndex);

	      		if ((first == second) || ((!first && second) ^ sortAscending))
	      			break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
				// chose false is less than true
				// when sortAscending is true, sort from false to true
	      		boolean first  = highToLowValue.isEnabled(graphIndex);
	      		boolean second = pivotValue.isEnabled(graphIndex);

	      		if ((first == second) || ((first && !second) ^ sortAscending))
	      			break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
	      		boolean low  = lowToHighValue.isEnabled(graphIndex);
	      		boolean high = highToLowValue.isEnabled(graphIndex);
				if ((low == high) || ((!low & high) ^ sortAscending)) // swap even if equal
				{
					if (low != high) {
				    	parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
					}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByShow(elements, lowIndex, newHighIndex); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByShow(elements, newLowIndex, highIndex); // sort higher subpart
	    }
	}

	private void quickSortByAverageLoad(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		ProfiledGeneric pivotValue;  // change the values to suit your application
		ProfiledGeneric lowToHighValue;
		ProfiledGeneric highToLowValue;
		ProfiledGeneric parking;
		int newLowIndex;
		int newHighIndex;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotValue = (ProfiledGeneric) elements.elementAt(pivotIndex);

		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;
		// loop until low meets high
		while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
		{
		  	lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
		  	while (lowToHighIndex < newLowIndex)
		  	{
		  		float first  = lowToHighValue.getPercentLoad(graphIndex);
		    	float second = pivotValue.getPercentLoad(graphIndex);

		    	if ((first == second) || ((first < second) ^ sortAscending))
		    		break;

			    newHighIndex = lowToHighIndex; // add element to lower part
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			}

		    highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
		    while (newHighIndex <= highToLowIndex)
		    {
		    	float first  = highToLowValue.getPercentLoad(graphIndex);
		    	float second = pivotValue.getPercentLoad(graphIndex);

			    if ((first == second) || ((first > second) ^ sortAscending))
			    	break;

			    newLowIndex = highToLowIndex; // add element to higher part
			    highToLowIndex --;
			    highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
		    }

		    // swap if needed
		    if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
		    {
				newHighIndex = lowToHighIndex; // move element arbitrary to lower part
		    }
		    else if (lowToHighIndex < highToLowIndex) // not last element yet
		    {
				float first  = lowToHighValue.getPercentLoad(graphIndex);
				float second = highToLowValue.getPercentLoad(graphIndex);
				if ((first == second) || ((first < second) ^ sortAscending))
			    {
			       	if (first != second) {
			       		parking = lowToHighValue;
				        elements.setElementAt(highToLowValue, lowToHighIndex);
				        elements.setElementAt(parking, highToLowIndex);
			       	}

				    newLowIndex = highToLowIndex;
				    newHighIndex = lowToHighIndex;

				    lowToHighIndex ++;
				    highToLowIndex --;
			    }
		    }
		}

		// Continue recursion for parts that have more than one element
		if (lowIndex < newHighIndex)
		{
		  	// sort lower subpart
		  	this.quickSortByAverageLoad(elements, lowIndex, newHighIndex);
		}
		if (newLowIndex < highIndex)
		{
		  	// sort higher subpart
		  	this.quickSortByAverageLoad(elements, newLowIndex, highIndex);
		}
	}

	private void quickSortByName(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex, boolean caseSensitive)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledGeneric pivotValue;  // change the values to suit your application
	    ProfiledGeneric lowToHighValue;
	    ProfiledGeneric highToLowValue;
	    ProfiledGeneric parking;
	    int newLowIndex;
	    int newHighIndex;
	    int compareResult;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledGeneric)elements.elementAt(pivotIndex);

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
				if (caseSensitive)
					compareResult = lowToHighValue.getNameString().compareTo(pivotValue.getNameString());
				else
					compareResult = lowToHighValue.getNameString().compareToIgnoreCase(pivotValue.getNameString());
					
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
					break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
				if (caseSensitive)
					compareResult = highToLowValue.getNameString().compareTo(pivotValue.getNameString());
				else
					compareResult = highToLowValue.getNameString().compareToIgnoreCase(pivotValue.getNameString());
					
				if ((compareResult == 0) || ((compareResult > 0) ^ sortAscending))
					break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
				if (caseSensitive)
					compareResult = lowToHighValue.getNameString().compareTo(highToLowValue.getNameString());
				else
					compareResult = lowToHighValue.getNameString().compareToIgnoreCase(highToLowValue.getNameString());
					
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
				{
			    	if (compareResult != 0) {
			    		parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
			    	}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByName(elements, lowIndex, newHighIndex, caseSensitive); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByName(elements, newLowIndex, highIndex, caseSensitive); // sort higher subpart
	    }
	}

	private void quickSortByBinary(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledGeneric pivotValue;  // change the values to suit your application
	    ProfiledGeneric lowToHighValue;
	    ProfiledGeneric highToLowValue;
	    ProfiledGeneric parking;
	    int newLowIndex;
	    int newHighIndex;
	    int compareResult;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledGeneric)elements.elementAt(pivotIndex);

	    String pivotName = pivotValue.getNameString();
		int index = pivotName.lastIndexOf('\\');
		if (index != -1)
			pivotName = pivotName.substring(index);

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
				String lowName = lowToHighValue.getNameString();
				index = lowName.lastIndexOf('\\');
				if (index != -1)
					lowName = lowName.substring(index);

				compareResult = lowName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
					break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
				String highName = highToLowValue.getNameString();
				index = highName.lastIndexOf('\\');
				if (index != -1)
					highName = highName.substring(index);

				compareResult = highName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult > 0) ^ sortAscending))
					break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
				String lowName = lowToHighValue.getNameString();
				index = lowName.lastIndexOf('\\');
				if (index != -1)
					lowName = lowName.substring(index);

				String highName = highToLowValue.getNameString();
				index = highName.lastIndexOf('\\');
				if (index != -1)
					highName = highName.substring(index);

				compareResult = lowName.compareToIgnoreCase(highName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
				{
			    	if (compareResult != 0) {
			    		parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
			    	}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByBinary(elements, lowIndex, newHighIndex); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByBinary(elements, newLowIndex, highIndex); // sort higher subpart
	    }
	}

	private void quickSortByBinaryPath(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledGeneric pivotValue;  // change the values to suit your application
	    ProfiledGeneric lowToHighValue;
	    ProfiledGeneric highToLowValue;
	    ProfiledGeneric parking;
	    int newLowIndex;
	    int newHighIndex;
	    int compareResult;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledGeneric)elements.elementAt(pivotIndex);

		String pivotName = pivotValue.getNameString();
		int index = pivotName.lastIndexOf('\\');
		if (index == -1)
			pivotName = ""; //$NON-NLS-1$
		else
			pivotName = pivotName.substring(0, index);

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
				String lowName = lowToHighValue.getNameString();
				index = lowName.lastIndexOf('\\');
				if (index == -1)
					lowName = ""; //$NON-NLS-1$
				else
					lowName = lowName.substring(0, index);

				compareResult = lowName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
					break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
				String highName = highToLowValue.getNameString();
				index = highName.lastIndexOf('\\');
				if (index == -1)
					highName = ""; //$NON-NLS-1$
				else
					highName = highName.substring(0, index);

				compareResult = highName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult > 0) ^ sortAscending))
					break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
				String lowName = lowToHighValue.getNameString();
				index = lowName.lastIndexOf('\\');
				if (index == -1)
					lowName = ""; //$NON-NLS-1$
				else
					lowName = lowName.substring(0, index);

				String highName = highToLowValue.getNameString();
				index = highName.lastIndexOf('\\');
				if (index == -1)
					highName = ""; //$NON-NLS-1$
				else
					highName = highName.substring(0, index);

				compareResult = lowName.compareToIgnoreCase(highName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
				{
			    	if (compareResult != 0) {
			    		parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
			    	}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByBinaryPath(elements, lowIndex, newHighIndex); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByBinaryPath(elements, newLowIndex, highIndex); // sort higher subpart
	    }
	}

	private void quickSortByFullBinaryPath(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledGeneric pivotValue;  // change the values to suit your application
	    ProfiledGeneric lowToHighValue;
	    ProfiledGeneric highToLowValue;
	    ProfiledGeneric parking;
	    int newLowIndex;
	    int newHighIndex;
	    int compareResult;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledGeneric)elements.elementAt(pivotIndex);

		String pivotName = pivotValue.getNameString();

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
				String lowName = lowToHighValue.getNameString();

				compareResult = lowName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
					break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
				String highName = highToLowValue.getNameString();

				compareResult = highName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult > 0) ^ sortAscending))
					break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
				String lowName = lowToHighValue.getNameString();
				String highName = highToLowValue.getNameString();

				compareResult = lowName.compareToIgnoreCase(highName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
				{
			    	if (compareResult != 0) {
			    		parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
			    	}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByFullBinaryPath(elements, lowIndex, newHighIndex); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByFullBinaryPath(elements, newLowIndex, highIndex); // sort higher subpart
	    }
	}

	private void quickSortByStartAddress(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledFunction pivotValue;  // change the values to suit your application
	    ProfiledFunction lowToHighValue;
	    ProfiledFunction highToLowValue;
	    ProfiledFunction parking;
	    int newLowIndex;
	    int newHighIndex;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledFunction)elements.elementAt(pivotIndex);

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledFunction)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
	      		long first  = lowToHighValue.getFunctionAddress();
	      		long second = pivotValue.getFunctionAddress();

	      		if ((first == second) || ((first < second) ^ sortAscending))
	      			break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledFunction)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledFunction)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
		      	long first  = highToLowValue.getFunctionAddress();
		      	long second = pivotValue.getFunctionAddress();

		      	if ((first == second) || ((first > second) ^ sortAscending))
		      		break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledFunction)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
			    long first  = lowToHighValue.getFunctionAddress();
			    long second = highToLowValue.getFunctionAddress();

			    if ((first == second) || ((first < second) ^ sortAscending))
				{
			    	if (first != second) {
			    		parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
			    	}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByStartAddress(elements, lowIndex, newHighIndex); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByStartAddress(elements, newLowIndex, highIndex); // sort higher subpart
	    }
	}

	private void quickSortByAssocBinary(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledFunction pivotValue;  // change the values to suit your application
	    ProfiledFunction lowToHighValue;
	    ProfiledFunction highToLowValue;
	    ProfiledFunction parking;
	    int newLowIndex;
	    int newHighIndex;
	    int compareResult;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledFunction)elements.elementAt(pivotIndex);

	    String pivotName = pivotValue.getFunctionBinaryName();
		int index = pivotName.lastIndexOf('\\');
		if (index != -1)
			pivotName = pivotName.substring(index);

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledFunction)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
				String lowName = lowToHighValue.getFunctionBinaryName();
				index = lowName.lastIndexOf('\\');
				if (index != -1)
					lowName = lowName.substring(index);

				compareResult = lowName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
					break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledFunction)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledFunction)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
				String highName = highToLowValue.getFunctionBinaryName();
				index = highName.lastIndexOf('\\');
				if (index != -1)
					highName = highName.substring(index);

				compareResult = highName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult > 0) ^ sortAscending))
					break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledFunction)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
				String lowName = lowToHighValue.getFunctionBinaryName();
				index = lowName.lastIndexOf('\\');
				if (index != -1)
					lowName = lowName.substring(index);

				String highName = highToLowValue.getFunctionBinaryName();
				index = highName.lastIndexOf('\\');
				if (index != -1)
					highName = highName.substring(index);

				compareResult = lowName.compareToIgnoreCase(highName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
				{
					if (compareResult != 0) {
				    	parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
					}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByAssocBinary(elements, lowIndex, newHighIndex); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByAssocBinary(elements, newLowIndex, highIndex); // sort higher subpart
	    }
	}

	private void quickSortByAssocBinaryPath(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledFunction pivotValue;  // change the values to suit your application
	    ProfiledFunction lowToHighValue;
	    ProfiledFunction highToLowValue;
	    ProfiledFunction parking;
	    int newLowIndex;
	    int newHighIndex;
	    int compareResult;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledFunction)elements.elementAt(pivotIndex);

		String pivotName = pivotValue.getFunctionBinaryName();
		int index = pivotName.lastIndexOf('\\');
		if (index == -1)
			pivotName = ""; //$NON-NLS-1$
		else
			pivotName = pivotName.substring(0, index);

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledFunction)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
				String lowName = lowToHighValue.getFunctionBinaryName();
				index = lowName.lastIndexOf('\\');
				if (index == -1)
					lowName = ""; //$NON-NLS-1$
				else
					lowName = lowName.substring(0, index);

				compareResult = lowName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
					break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledFunction)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledFunction)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
				String highName = highToLowValue.getFunctionBinaryName();
				index = highName.lastIndexOf('\\');
				if (index == -1)
					highName = ""; //$NON-NLS-1$
				else
					highName = highName.substring(0, index);

				compareResult = highName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult > 0) ^ sortAscending))
					break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledFunction)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
				String lowName = lowToHighValue.getFunctionBinaryName();
				index = lowName.lastIndexOf('\\');
				if (index == -1)
					lowName = ""; //$NON-NLS-1$
				else
					lowName = lowName.substring(0, index);

				String highName = highToLowValue.getFunctionBinaryName();
				index = highName.lastIndexOf('\\');
				if (index == -1)
					highName = ""; //$NON-NLS-1$
				else
					highName = highName.substring(0, index);

				compareResult = lowName.compareToIgnoreCase(highName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
				{
					if (compareResult != 0) {
				    	parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
					}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByAssocBinaryPath(elements, lowIndex, newHighIndex); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByAssocBinaryPath(elements, newLowIndex, highIndex); // sort higher subpart
	    }
	}

	private void quickSortByFullAssocBinaryPath(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
		int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledFunction pivotValue;  // change the values to suit your application
	    ProfiledFunction lowToHighValue;
	    ProfiledFunction highToLowValue;
	    ProfiledFunction parking;
	    int newLowIndex;
	    int newHighIndex;
	    int compareResult;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;

	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledFunction)elements.elementAt(pivotIndex);

	    String pivotName = pivotValue.getFunctionBinaryName();

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    while ((newHighIndex + 1) < newLowIndex)
	    {
			lowToHighValue = (ProfiledFunction)elements.elementAt(lowToHighIndex);
			while (lowToHighIndex < newLowIndex)
			{
				String lowName = lowToHighValue.getFunctionBinaryName();

				compareResult = lowName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
					break;

				newHighIndex = lowToHighIndex;
			    lowToHighIndex ++;
			    lowToHighValue = (ProfiledFunction)elements.elementAt(lowToHighIndex);
			}
			highToLowValue = (ProfiledFunction)elements.elementAt(highToLowIndex);
			while (newHighIndex <= highToLowIndex)
			{
				String highName = highToLowValue.getFunctionBinaryName();

				compareResult = highName.compareToIgnoreCase(pivotName);
				if ((compareResult == 0) || ((compareResult > 0) ^ sortAscending))
					break;

				newLowIndex = highToLowIndex;
			    highToLowIndex --;
			    highToLowValue = (ProfiledFunction)elements.elementAt(highToLowIndex);
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex)
			{
				newHighIndex = lowToHighIndex;
			}
			else if (lowToHighIndex < highToLowIndex)
			{
				String lowName = lowToHighValue.getFunctionBinaryName();
				String highName = highToLowValue.getFunctionBinaryName();

				compareResult = lowName.compareToIgnoreCase(highName);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
				{
					if (compareResult != 0) {
				    	parking = lowToHighValue;
				    	elements.setElementAt(highToLowValue, lowToHighIndex);
				    	elements.setElementAt(parking, highToLowIndex);
					}

			      	newLowIndex = highToLowIndex;
			      	newHighIndex = lowToHighIndex;

			      	lowToHighIndex ++;
			      	highToLowIndex --;
				}
			}
	    }

	    // Continue recursion for parts that have more than one element
	    if (lowIndex < newHighIndex)
	    {
	    	this.quickSortByFullAssocBinaryPath(elements, lowIndex, newHighIndex); // sort lower subpart
	    }
	    if (newLowIndex < highIndex)
	    {
	    	this.quickSortByFullAssocBinaryPath(elements, newLowIndex, highIndex); // sort higher subpart
	    }
	}

	private void quickSortByPriority(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex, Hashtable priorities)
	{
		int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		ProfiledThread pivotValue;  // change the values to suit your application
		ProfiledThread lowToHighValue;
		ProfiledThread highToLowValue;
		ProfiledThread parking;
		int newLowIndex;
		int newHighIndex;
		int compareResult;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotValue = (ProfiledThread)elements.elementAt(pivotIndex);
		Integer pivot = (Integer)priorities.get(new Integer(pivotValue.getThreadId()));
		pivot = (pivot == null) ? new Integer(Integer.MIN_VALUE) : pivot;

		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;

		// loop until low meets high
		while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
		{
			lowToHighValue = (ProfiledThread)elements.elementAt(lowToHighIndex);
			Integer low = (Integer)priorities.get(new Integer(lowToHighValue.getThreadId()));
			low = (low == null) ? new Integer(Integer.MIN_VALUE) : low;
			while (lowToHighIndex < newLowIndex
					&& ((low.compareTo(pivot) != 0) && ((low.compareTo(pivot) > 0) ^ sortAscending)))
			{
				newHighIndex = lowToHighIndex; // add element to lower part
				lowToHighIndex ++;
				lowToHighValue = (ProfiledThread)elements.elementAt(lowToHighIndex);
				low = (Integer)priorities.get(new Integer(lowToHighValue.getThreadId()));
				low = (low == null) ? new Integer(Integer.MIN_VALUE) : low;
			}

			highToLowValue = (ProfiledThread)elements.elementAt(highToLowIndex);
			Integer high = (Integer)priorities.get(new Integer(highToLowValue.getThreadId()));
			high = (high == null) ? new Integer(Integer.MIN_VALUE) : high;
			while (newHighIndex <= highToLowIndex
					&& ((high.compareTo(pivot) != 0) && ((high.compareTo(pivot) < 0) ^ sortAscending)))
			{
				newLowIndex = highToLowIndex; // add element to higher part
				highToLowIndex --;
				highToLowValue = (ProfiledThread)elements.elementAt(highToLowIndex);
				high = (Integer)priorities.get(new Integer(highToLowValue.getThreadId()));
			  	high = (high == null) ? new Integer(Integer.MIN_VALUE) : high;
			}

			// swap if needed
			if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
			{
				newHighIndex = lowToHighIndex; // move element arbitrary to lower part
			}
			else if (lowToHighIndex < highToLowIndex) // not last element yet
			{
				high = (Integer)priorities.get(new Integer(highToLowValue.getThreadId()));
				high = (high == null) ? new Integer(Integer.MIN_VALUE) : high;
				low  = (Integer)priorities.get(new Integer(lowToHighValue.getThreadId()));
				low  = (low == null) ? new Integer(Integer.MIN_VALUE) : low;

				compareResult = low.compareTo(high);
				if ((compareResult == 0) || ((compareResult < 0) ^ sortAscending))
				{
					if (compareResult != 0) {
						parking = lowToHighValue;
						elements.setElementAt(highToLowValue, lowToHighIndex);
						elements.setElementAt(parking, highToLowIndex);
					}

					newLowIndex = highToLowIndex;
					newHighIndex = lowToHighIndex;

					lowToHighIndex ++;
					highToLowIndex --;
				}
			}
		}

		// Continue recursion for parts that have more than one element
		if (lowIndex < newHighIndex)
		{
			this.quickSortByPriority(elements, lowIndex, newHighIndex, priorities); // sort lower subpart
		}
		if (newLowIndex < highIndex)
		{
			this.quickSortByPriority(elements, newLowIndex, highIndex, priorities); // sort higher subpart
		}
	}

	private void quickSortBySampleCount(Vector<ProfiledGeneric> elements, int lowIndex, int highIndex)
	{
	  	int lowToHighIndex;
	    int highToLowIndex;
	    int pivotIndex;
	    ProfiledGeneric pivotValue;  // change the values to suit your application
	    ProfiledGeneric lowToHighValue;
	    ProfiledGeneric highToLowValue;
	    ProfiledGeneric parking;
	    int newLowIndex;
	    int newHighIndex;

	    lowToHighIndex = lowIndex;
	    highToLowIndex = highIndex;
	    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
	    pivotValue = (ProfiledGeneric) elements.elementAt(pivotIndex);

	    newLowIndex = highIndex + 1;
	    newHighIndex = lowIndex - 1;
	    // loop until low meets high
	    while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
	    {
	    	lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
	      	while (lowToHighIndex < newLowIndex)
		    {
	      		int first  = lowToHighValue.getSampleCount(graphIndex);
	      		int second = pivotValue.getSampleCount(graphIndex);

	      		if ((first == second) || ((first < second) ^ sortAscending))
	      			break;

		      	newHighIndex = lowToHighIndex; // add element to lower part
		        lowToHighIndex ++;
		        lowToHighValue = (ProfiledGeneric)elements.elementAt(lowToHighIndex);
		    }

	      	highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
		    while (newHighIndex <= highToLowIndex)
		    {
		    	int first  = highToLowValue.getSampleCount(graphIndex);
			    int second = pivotValue.getSampleCount(graphIndex);

			    if ((first == second) || ((first > second) ^ sortAscending))
			     	break;

			    newLowIndex = highToLowIndex; // add element to higher part
			    highToLowIndex --;
			    highToLowValue = (ProfiledGeneric)elements.elementAt(highToLowIndex);
		   }

		   // swap if needed
		   if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
		   {
			    newHighIndex = lowToHighIndex; // move element arbitrary to lower part
		   }
		   else if (lowToHighIndex < highToLowIndex) // not last element yet
		   {
				int first  = lowToHighValue.getSampleCount(graphIndex);
				int second = highToLowValue.getSampleCount(graphIndex);
				if ((first == second) || ((first < second) ^ sortAscending))
			    {
					if (first != second) {
						parking = lowToHighValue;
					    elements.setElementAt(highToLowValue, lowToHighIndex);
					    elements.setElementAt(parking, highToLowIndex);
					}

				    newLowIndex = highToLowIndex;
				    newHighIndex = lowToHighIndex;

				    lowToHighIndex ++;
				    highToLowIndex --;
			    }
		   }
	    }

		// Continue recursion for parts that have more than one element
		if (lowIndex < newHighIndex)
		{
			// sort lower subpart
		    this.quickSortBySampleCount(elements, lowIndex, newHighIndex);
		}
		if (newLowIndex < highIndex)
		{
		    // sort higher subpart
		    this.quickSortBySampleCount(elements, newLowIndex, highIndex);
	    }
	}

	public Vector<ProfiledGeneric> getSortedList()
	{
		return sortedList;
	}

}
