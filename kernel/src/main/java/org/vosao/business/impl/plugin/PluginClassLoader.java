/**
 * Vosao CMS. Simple CMS for Google App Engine.
 * Copyright (C) 2009 Vosao development team
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * email: vosao.dev@gmail.com
 */

package org.vosao.business.impl.plugin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.vosao.business.PluginResourceBusiness;

public class PluginClassLoader extends ClassLoader {

	private PluginResourceBusiness pluginResourceBusiness;

	public PluginClassLoader() {
		super(PluginClassLoader.class.getClassLoader());
	}
	
	@Override
	public Class findClass(String name) throws ClassNotFoundException {
		Class cls = findLoadedClass(name);
		if (cls != null) {
			return cls;
		}
		try {
			byte[] b = pluginResourceBusiness.findResource(name);
			if (b == null) {
				throw new ClassNotFoundException(name);
			}
			return defineClass(name, b, 0, b.length);
		}
		catch (SecurityException e) {
			return super.loadClass(name);
		}
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		byte[] b = pluginResourceBusiness.findResource(name);
		if (b == null) {
			throw new ResourceNotFoundException(name);
		}
		return new ByteArrayInputStream(b);
	}
	
	public PluginResourceBusiness getPluginResourceBusiness() {
		return pluginResourceBusiness;
	}

	public void setPluginResourceBusiness(
			PluginResourceBusiness pluginResourceBusiness) {
		this.pluginResourceBusiness = pluginResourceBusiness;
	}
	
}
