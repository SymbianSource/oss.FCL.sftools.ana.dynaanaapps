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

package com.nokia.carbide.cpp.internal.pi.utils;

import java.util.Stack;
import java.util.Vector;


public class QuickSortImpl
{
	public static void sort(Sortable[] array)
	{
		quickSort(array,0,array.length-1);
	}
	
	public static void sort(Vector sortables)
	{
		Sortable[] s = new Sortable[sortables.size()];
		s = (Sortable[])sortables.toArray(s);
		sort(s);
		sortables.clear();
		for (int i=0;i<s.length;i++)
		{
			sortables.add(s[i]);
		}
	}
	
	public static void sortReversed(Vector sortables)
	{
		Sortable[] s = new Sortable[sortables.size()];
		s = (Sortable[])sortables.toArray(s);
		sort(s);
		sortables.clear();
		for (int i=(s.length-1);i>=0;i--)
		{
			sortables.add(s[i]);
		}
	}
		
	public static void quickSort(Sortable[] a,int low, int high)
	{
		Stack stack = new Stack();
		
		//System.out.println("QS both sides "+low+" "+high);
		int pivot;
		/* Termination condition! */
		if ( high > low )
		{
			while(high > low)
			{
				pivot = partition( a, low, high );
				//System.out.println("QS left side is "+low+"-"+pivot+" right side is "+pivot+"-"+high);

				//quickSort( a, low, pivot-1 );
				stack.push(new Integer(pivot+1));
				stack.push(new Integer(high));
				//System.out.println("Pushed right side");
				high = pivot-1;
			}				
			
			while(!stack.isEmpty())
			{
				high = ((Integer)stack.pop()).intValue();
				low = ((Integer)stack.pop()).intValue();
				//System.out.println("Popping right side "+low+"-"+high);
				quickSort( a, low, high );
			}
		}
	}
	
	public static int partition(Sortable[] a,int low,int high)
	{
		int left, right;
		int pivot;
		
		pivot = left = low;
		right = high;
		long pivotValue = a[pivot].valueOf();
		Sortable pivotItem = a[pivot];
		
		while ( left < right ) 
		{
			// Move left while item < pivot 
				//System.out.println("Starting left "+left);
				while(a[left].valueOf() <= pivotValue ) 
				{
					//System.out.println(valueOf(a[left])+" < "+pivotValue);
					left++;
					//System.out.println("left ="+left);
					if (left == a.length)
						break;
				}
				//System.out.println("Final left = "+left);
			// Move right while item > pivot
			while( a[right].valueOf() > pivotValue ) 
			{
				right--;
				//System.out.println("right ="+right);
			}
			if ( left < right )
			{
				//System.out.println("Swapping "+left+" and "+right);
				Sortable tmp = a[left];
				a[left] = a[right];
				a[right] = tmp;
				//SWAP(a,left,right);
			}
		}
		// right is final position for the pivot
		a[low] = a[right];
		a[right] = pivotItem;
		//System.out.println("Pivot is now "+right+" pivot value "+valueOf(a[right]));
		return right;
	}
}
