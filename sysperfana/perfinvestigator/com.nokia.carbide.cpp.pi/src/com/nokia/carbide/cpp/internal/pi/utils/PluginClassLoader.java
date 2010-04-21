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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.nokia.carbide.cpp.internal.pi.plugin.model.AbstractPiPlugin;


public class PluginClassLoader extends ClassLoader 
{	
	public ArrayList dirEntries;
	public ArrayList jarEntries;
	public Hashtable resolvedClasses;
	
	public PluginClassLoader(AbstractPiPlugin[] entries) 
	{
		super();
		
		dirEntries = new ArrayList();
		jarEntries = new ArrayList();
		resolvedClasses = new Hashtable();
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Class loadClass(String className) throws ClassNotFoundException
	{
		if (this.resolvedClasses.containsKey(className))
		{
			return (Class)this.resolvedClasses.get(className);
		}
		
		try
		{
			Class c = super.loadClass(className);
			return c;
		}
		catch(ClassNotFoundException e)
		{
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Class findClass(String className) throws ClassNotFoundException
	{
		if (this.resolvedClasses.containsKey(className))
		{
			return (Class)this.resolvedClasses.get(className);
		}
		else
		{
			byte[] classBytes = findClassBytes(className);
			if (classBytes == null)
			{
				Class c = super.findLoadedClass(className);
				if (c == null)
					throw new ClassNotFoundException(className);
				else
					return c;
			} 
			else 
			{
				Class c = defineClass(className, classBytes, 0, classBytes.length);
				this.resolvedClasses.put(className,c);
				return c;
			}
		}
	}
	
	public byte[] findClassBytes(String className)
	{
		Iterator i = this.dirEntries.iterator();
		while(i.hasNext())
		{
			File f = (File)i.next();
			byte[] b = this.findClassBytesDir(f,className);
			if (b != null) return b;
		}
		
		i = this.jarEntries.iterator();
		while(i.hasNext())
		{
			File f = (File)i.next();
			byte[] b = this.findClassBytesJar(f,className);
			if (b != null) return b;
		}
		
		return null;
	}
	
	private byte[] findClassBytesJar(File jarFile, String className)
	{
		//System.out.println("Finding class bytes for "+className+" from "+jarFile.getName());
		try
		{
			ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
			ZipEntry ze = zis.getNextEntry();
			
			while(zis.available() == 1 && ze != null)
			{
				String entryName = ze.getName().replace('/','.');
				
				if (entryName.equals(className+Messages.getString("PluginClassLoader.classEntry"))) //$NON-NLS-1$
				{
					//System.out.println("FOUND!!!");
					ArrayList<byte[]>  data    = new ArrayList<byte[]>();
					ArrayList<Integer> amounts = new ArrayList<Integer>();
					int totalAmount = 0;
					while(true)
					{
						byte[] tempBytes = new byte[1024];
						int amount = zis.read(tempBytes);
						if (amount != -1) 
						{
							amounts.add(Integer.valueOf(amount));
							totalAmount+=amount;
							data.add(tempBytes);
						}
						else
						{
							break;	
						}
					}
						
					byte[] classBytes = new byte[totalAmount];
						
					Iterator<byte[]>  bI = data.iterator();
					Iterator<Integer> aI = amounts.iterator();
					int pos = 0;
					while(bI.hasNext())
					{
						byte[] a = bI.next();
						int am = aI.next().intValue();
						System.arraycopy(a,0,classBytes,pos,am);
						pos += am;
					}

					zis.closeEntry();
					return classBytes;
				}
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			zis.close();
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private byte[] findClassBytesDir(File rootDir, String className) 
	{	
		try 
		{
			String pathName = rootDir.getAbsolutePath() + File.separatorChar
					+ className.replace('.', File.separatorChar) + Messages.getString("PluginClassLoader.classEntry"); //$NON-NLS-1$

			FileInputStream fis = new FileInputStream(pathName);
			byte[] classBytes = new byte[fis.available()];
			fis.read(classBytes);
			fis.close();
			return classBytes;
		} 
		catch (java.io.IOException ioEx)
		{
			return null;
		}
	}
}
