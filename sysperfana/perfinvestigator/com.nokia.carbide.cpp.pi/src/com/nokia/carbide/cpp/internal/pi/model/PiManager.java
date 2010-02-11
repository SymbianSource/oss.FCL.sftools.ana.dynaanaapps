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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

import com.nokia.carbide.cpp.internal.pi.analyser.PIChangeEvent;
import com.nokia.carbide.cpp.internal.pi.interfaces.IPiItem;
import com.nokia.carbide.cpp.pi.PiPlugin;



public class PiManager {
	private static PiManager manager;
	private Collection<IPiItem> favorites;
	private ArrayList<PiManagerListener> listeners = new ArrayList<PiManagerListener>();
	
	private PiManager() {
	}
	
   ///////////// IPiItem accessors ///////////////////////

	public static PiManager getManager() {
		if (manager == null)
			manager = new PiManager();
		return manager;
	}
	
	public void synchronizePI() {
		PIChangeEvent.action("synchronise"); //$NON-NLS-1$
	}

	public IPiItem[] getFavorites() {
		if (favorites == null)
			loadFavorites();
		return favorites.toArray(
			new IPiItem[favorites.size()]);
	}
	
	public IPiItem newFavoriteFor(Object obj) {
		PiItemType[] types =
			PiItemType.getTypes();
		for (int i = 0; i < types.length; i++) {
			IPiItem item = types[i].newFavorite(obj);
			if (item != null)
				return item;
		}
		return null;
	}

	public IPiItem[] newFavoritesFor(Iterator iter) {
		if (iter == null)
			return IPiItem.NONE;
		Collection<IPiItem> items = new HashSet<IPiItem>(20);
		while (iter.hasNext()) {
			IPiItem item =
				newFavoriteFor((Object) iter.next());
			if (item != null)
				items.add(item);
		}
		return (IPiItem[]) items.toArray(
			new IPiItem[items.size()]);
	}

	public IPiItem[] newFavoritesFor(Object[] objects) {
		if (objects == null)
			return IPiItem.NONE;
		return newFavoritesFor(
			Arrays.asList(objects).iterator());
	}
	
	public IPiItem existingFavoriteFor(Object obj) {
		if (obj == null)
			return null;
		Iterator<IPiItem> iter = favorites.iterator();
		while (iter.hasNext()) {
			IPiItem item = iter.next();
			if (item.isPIFor(obj))
				return item;
		}
		return null;
	}

	public IPiItem[] existingFavoritesFor(Iterator iter)
	{
		ArrayList<IPiItem> result = new ArrayList<IPiItem>(10);
		while (iter.hasNext()) {
			IPiItem item =
				existingFavoriteFor(iter.next());
			if (item != null)
				result.add(item);
		}
		return (IPiItem[]) result.toArray(
			new IPiItem[result.size()]);
	}
	
	public void addFavorites(IPiItem[] items) {
		if (favorites == null)
			loadFavorites();
		if (favorites.addAll(Arrays.asList(items)))
			fireFavoritesChanged(items, IPiItem.NONE);
	}
	
	public void removeFavorites(IPiItem[] items) {
		if (favorites == null)
			loadFavorites();
		if (favorites.removeAll(Arrays.asList(items)))
			fireFavoritesChanged(IPiItem.NONE, items);
	}

   /////////// Loading and Saving Favorites /////////////////

   private static final String TAG_FAVORITES = "Favorites"; //$NON-NLS-1$
   private static final String TAG_FAVORITE = "Favorite"; //$NON-NLS-1$
   private static final String TAG_TYPEID = "TypeId"; //$NON-NLS-1$
   private static final String TAG_INFO = "Info"; //$NON-NLS-1$
   
   private void loadFavorites() {
      favorites = new HashSet<IPiItem>(20);
      FileReader reader = null;
      try {
         reader = new FileReader(getFavoritesFile());
         loadFavorites(XMLMemento.createReadRoot(reader));
      }
      catch (FileNotFoundException e) {
         // ignored... no favorites exist yet
      }
      catch (Exception e) {
         // log the exception and move on
//         PILog.logError(e);
      }
      finally {
         try {
            if (reader != null)
               reader.close();
         }
         catch (IOException e) {
//            PILog.logError(e);
         }
      }
   }
   
   private void loadFavorites(XMLMemento memento) {
      IMemento[] children =
         memento.getChildren(TAG_FAVORITE);
      for (int i = 0; i < children.length; i++) {
         IPiItem item =
            newFavoriteFor(
               children[i].getString(TAG_TYPEID),
               children[i].getString(TAG_INFO));
         if (item != null)
            favorites.add(item);
      }
   }
   
   public IPiItem newFavoriteFor(String typeId, String info) {
      PiItemType[] types = PiItemType.getTypes();
      for (int i = 0; i < types.length; i++)
         if (types[i].getID().equals(typeId))
            return types[i].loadFavorite(info);
      return null;
   }
   
   public void saveFavorites() {
      if (favorites == null)
         return;
      XMLMemento memento =
         XMLMemento.createWriteRoot(TAG_FAVORITES);
      saveFavorites(memento);
      FileWriter writer = null;
      try {
         writer = new FileWriter(getFavoritesFile());
         memento.save(writer);
      }
      catch (IOException e) {
 //        PILog.logError(e);
      }
      finally {
         try {
            if (writer != null)
               writer.close();
         }
         catch (IOException e) {
//            PILog.logError(e);
         }
      }
   }
   
   private void saveFavorites(XMLMemento memento) {
      Iterator iter = favorites.iterator();
      while (iter.hasNext()) {
         IPiItem item = (IPiItem) iter.next();
         IMemento child = memento.createChild(TAG_FAVORITE);
         child.putString(TAG_TYPEID, item.getType().getID());
         child.putString(TAG_INFO, item.getInfo());
      }
   }
   
   private File getFavoritesFile() {
      return PiPlugin
         .getDefault()
         .getStateLocation()
         .append(Messages.getString("PiManager.favoritesXmlFile")) //$NON-NLS-1$
         .toFile();
	}
	
   ///////////// FavoriteManagerListener methods ///////////////////////

	public void addFavoritesManagerListener(PiManagerListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeFavoritesManagerListener(PiManagerListener listener) {
		listeners.remove(listener);
	}
	
	private void fireFavoritesChanged(IPiItem[] itemsAdded, IPiItem[] itemsRemoved) {
		PiManagerEvent event = new PiManagerEvent(this, itemsAdded, itemsRemoved);
		for (Iterator<PiManagerListener> iter = listeners.iterator(); iter.hasNext();)
			iter.next().piChanged(event);
	}

   ///////////// IResourceChangeListener //////////////////////
   
}
