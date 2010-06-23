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

package com.nokia.carbide.cpp.internal.pi.button.ui;

import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;

import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.ButtonEventProfileType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.MappingType;
import com.nokia.carbide.cpp.internal.pi.util.config.gen.PIConfig.PIConfigFactory;
import com.nokia.carbide.cpp.pi.button.IBupEventMap;
import com.nokia.carbide.cpp.pi.button.IBupEventMapEditable;
import com.nokia.carbide.cpp.pi.button.IBupEventMapProfile;

/**
 *
 * A class that cache IBupEventMap, handles modifications and commits update
 *
 */
public class ModifyCachedBupEventMap implements IBupEventMap,  IBupEventMapEditable {
	
	private IBupEventMap srcMap;
	private boolean modified;
	class TableEntry {
		String enumString;
		String label;
		boolean modified = false;
	};
	private TreeMap<Integer, TableEntry> cachedMap = new TreeMap<Integer, TableEntry>();
	
	public ModifyCachedBupEventMap(IBupEventMap map) {
		srcMap = map;
		initializeFromMap(srcMap);
	}
	
	public void initializeFromMap(IBupEventMap map) {
		modified = !map.equals(srcMap);
		cachedMap.clear();
		Set<Integer> keyCodeSet = map.getKeyCodeSet();
		for (Integer keyCode : keyCodeSet) {
			TableEntry entry = new TableEntry();
			entry.enumString = map.getEnum(keyCode);
			entry.label = map.getLabel(keyCode);
			cachedMap.put(keyCode, entry);
		}
	}

	public void commitChanges() {
		if (haveUncommitedChanges()) {
			// reset the map to blank
			Integer [] keyCodeSet = srcMap.getKeyCodeSet().toArray(new Integer[srcMap.getKeyCodeSet().size()]);
			for (Integer keyCode : keyCodeSet) {
				srcMap.removeMapping(keyCode);
			}
			// replace with all new mapping
			Set<Entry<Integer, TableEntry>> entrySet = cachedMap.entrySet();
			for (Entry<Integer, TableEntry> mapEntry : entrySet) {
				srcMap.addMapping(mapEntry.getKey(), mapEntry.getValue().enumString, mapEntry.getValue().label);
			}
		}
		initializeFromMap(srcMap);	// now we are done, reload as if freshly instantiated
	}
	
	public Set<Integer> getKeyCodeSet() {
		return cachedMap.keySet();
	}
	
	public String getLabel(int keyCode) {
		TableEntry entry = cachedMap.get(keyCode);
	    
	    if (entry == null) {
	    	return "" + keyCode;	// default is just the decimal value string //$NON-NLS-1$
	    }

	    return entry.label;		
	}
	
	public String getEnum(int keyCode) {
		TableEntry entry = cachedMap.get(keyCode);
	    
	    if (entry == null) {
	    	return ""; //$NON-NLS-1$
	    }

	    return entry.enumString;
	}

	public boolean isModified(int keyCode) {
		TableEntry cachedEntry = cachedMap.get(keyCode);
	    
	    if (cachedEntry == null) {
	    	if (srcMap.getKeyCodeSet().contains(keyCode)) {
	    		return true;
	    	}
	    	return false;
	    }
	    return cachedEntry.modified;
	}

	
	public boolean haveUncommitedChanges() {
		return modified;
	}

	public boolean flagAsModified(int keyCode) {
		boolean inCachedMap = cachedMap.keySet().contains(keyCode);
		boolean inSrcMap = srcMap.getKeyCodeSet().contains(keyCode);
		
		if (inCachedMap != inSrcMap) {
			return true;
		}
		
		if (inCachedMap == false) {
			return false;	// both doesn't exist
		}
		
		TableEntry cachedEntry = cachedMap.get(keyCode);
		return (!cachedEntry.enumString.equals(srcMap.getEnum(keyCode)) ||
				!cachedEntry.label.equals(srcMap.getLabel(keyCode)));
	}

	public void addMapping(int keyCode, String enumString, String label) {
		modified = true;
		TableEntry entry = cachedMap.get(keyCode);
		if (entry == null) {	
			entry = new TableEntry();
			cachedMap.put(Integer.valueOf(keyCode), entry);
		}
		entry.enumString = enumString;
		entry.label = label;
		entry.modified = true;
	}

	public void removeMapping(int keyCode) {
		modified = true;
		cachedMap.remove(keyCode);
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.button.IBupEventMap#getProfile()
	 */
	public IBupEventMapProfile getProfile() {
		return srcMap.getProfile();
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.button.IBupEventMap#toEmfModel()
	 */
	public ButtonEventProfileType toEmfModel() {
		ButtonEventProfileType profile = PIConfigFactory.E_INSTANCE.createButtonEventProfileType();

		profile.setProfileId(getProfile().getProfileId());
		EList<MappingType> mappingList = profile.getMapping();
		for (Entry<Integer, TableEntry> entry : cachedMap.entrySet()) {
			MappingType mappingType = PIConfigFactory.E_INSTANCE.createMappingType();
			mappingType.setKeyCode(entry.getKey().longValue());
			mappingType.setEnumString(entry.getValue().enumString);
			mappingType.setLabel(entry.getValue().label);
			mappingList.add(mappingType);
		}
		
		return profile;
	}
}
