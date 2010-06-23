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

package com.nokia.carbide.cpp.internal.pi.manager;

import java.util.Hashtable;
import java.util.Vector;

/**
 * A utility for predictively resolving classes
 * This can be used to help offset one of the biggest detractors from
 * the Java language:  Class loading delays.
 *
 **/

public final class BackgroundLoader implements Runnable
{
	/**
	 * Thread used to load classes in the background
	 **/

	private static Thread controlThread;

	/**
	 * Storage for the class names still to be resolved
	 **/

	private static Vector queue;

	static
	{
		try
		{
			// This is an IBM-specific code that prevents a nasty error in the IBM resource lookup code -JDM
			java.text.DateFormat.getDateInstance();
		}
		catch (Exception e)
		{
		}

		queue = new Vector();
	}

    /**
     * Private constructor, to enforce singleton status
     * Creation date: (12/20/99 3:10:32 PM)
     **/

    private BackgroundLoader()
    {
    }

    /**
     * Enqueue a class for background loading
     * Creation date: (12/20/99 3:23:28 PM)
     * @param className java.lang.String
     **/

    public static void enqueueClass(String className)
    {
    	synchronized(queue)
    	{
    		queue.addElement(className);

    		if (controlThread == null)
    			start();
    		else
    			queue.notify();
    	}
    }

    /**
     * Start loading
     * Creation date: (12/20/99 3:15:29 PM)
     **/

    public void run()
    {
    	if (Thread.currentThread() != controlThread)
    		return;

    	Hashtable done = new Hashtable();

    	while (controlThread.isAlive())
    	{
    		String className;

    		synchronized(queue)
    		{
    			if (queue.isEmpty())
    			{
    				try
    				{
    					queue.wait();
    				}
    				catch (InterruptedException ie)
    				{
    				}
    			}

    			if (!queue.isEmpty())
    			{
    				className = (String) queue.elementAt(0);
    				queue.removeElementAt(0);
    			}
    			else
    				continue;
    		}

    		if (!done.containsKey(className))
    		{
    			try
    			{
    				Class resolved = Class.forName(className);
    				done.put(resolved.getName(), resolved);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}
    		}

    		className = null;
    	}

    	controlThread = null;
    }

    /**
     * Insert the method's description here.
     * Creation date: (4/5/00 3:24:04 PM)
     */

    private static void start()
    {
    	controlThread = new Thread(new BackgroundLoader());

    	try
    	{
    		controlThread.setName(Messages.getString("BackgroundLoader.backgroundLoader")); //$NON-NLS-1$
    		controlThread.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
    	}
    	catch (Exception e)
    	{
    	}

    	controlThread.start();
    }
}
