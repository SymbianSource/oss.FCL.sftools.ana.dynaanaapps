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

package com.nokia.carbide.cpp.pi.util;
// import for com.nokai.sdt.utils
//package com.nokia.sdt.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Resource;

/**
 * Map-like class for managing disposable objects.
 * <p>
 * Entries are accessed via keys.  When the map is 
 * released, all entries are released.  If entries
 * implement IDisposable or like interfaces, we call
 * their dispose() methods.  Null values are not allowed.
 */
public class CacheMap<K,V> implements IDisposable {
	
	private Map<Object,Object> map = new HashMap<Object,Object> ();

    public Set keySet() {
        return map.keySet();
    }
    
	public void put(Object key, Object value) {
		//Check.checkArg(value);
		synchronized(map) {
			map.put(key, value);
		}
	}
	
	public Object get(Object key) {
		Object result;
		synchronized(map) {
			result = map.get(key);
		}
		return result;
	}
	
	public void remove(Object key) {
		synchronized(map) {
			map.remove(key);
		}
	}
	
	public void dispose() {
		if (map != null) {
			// object should no longer be used after IDisposable.dispose
			disposeAll();
			map = null;
		}
	}
	
    private void disposeObject(Object value) {
        if (value instanceof IDisposable) {
            IDisposable disposable = (IDisposable) value;
            disposable.dispose();
        }
        else if (value instanceof Resource) {
        	Resource r = (Resource) value;
        	r.dispose();
        }
    }
    
	public void disposeItem(Object key) {
		Object item;
		synchronized(map) {
			item = map.remove(key);
		}
		if (item != null)
            disposeObject(item);
	}
	
	public void disposeAll() {
		synchronized(map) {
			for (Iterator iter = map.values().iterator(); iter.hasNext();) {
				Object element = iter.next();
                disposeObject(element);
			}
			map.clear();
		}
	}
	
	public int size() {
		return map.size();
	}
}

