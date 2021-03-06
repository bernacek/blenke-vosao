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

package org.vosao.dao.cache.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.cache.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vosao.dao.cache.CacheStat;
import org.vosao.dao.cache.EntityCache;
import org.vosao.global.SystemService;

import com.google.appengine.api.memcache.InvalidValueException;

public class EntityCacheImpl implements EntityCache, Serializable {

	protected static final Log logger = LogFactory.getLog(
			EntityCacheImpl.class);
	
	private SystemService systemService;
	private long calls;
	private long hits;
	
	public EntityCacheImpl() {
		calls = 0;
		hits = 0;
	}
	
	private String getEntityKey(Class clazz, Object id) {
		return "entity:" + clazz.getName() + id.toString();
	}
	
	@Override
	public Object getEntity(Class clazz, Object id) {
		calls++;
		Set<String> entityKeys = getEntityKeySet(clazz);
		String key = getEntityKey(clazz, id);
		if (entityKeys.contains(key) && getCache().containsKey(key)) {
			Object result = getCache().get(key);
			if (result != null) {
				hits++;
			}
			return result;
		}
		return null;
	}

	private String getEntityKeySetKey(Class clazz) {
		return "entityList:" + clazz.getName();
	}
	
	private Set<String> getEntityKeySet(Class clazz) {
		Set<String> result = (Set<String>) getCache().get(getEntityKeySetKey(clazz));
		if (result == null) {
			result = new HashSet<String>();
		}
		return result;
	}
	
	private void updateEntityKeySet(Class clazz, Set<String> set) {
		getCache().put(getEntityKeySetKey(clazz), set);
	}
	
	private void addEntityToKeySet(Class clazz, String key) {
		Set<String> set = getEntityKeySet(clazz);
		set.add(key);
		updateEntityKeySet(clazz, set);
	}
	
	private void removeEntityFromKeySet(Class clazz, String key) {
		Set<String> set = getEntityKeySet(clazz);
		set.remove(key);
		updateEntityKeySet(clazz, set);
	}
	
	@Override
	public void putEntity(Class clazz, Object id, Object entity) {
		String key = getEntityKey(clazz, id);
		getCache().put(key, entity);
		addEntityToKeySet(clazz, key);
	}

	@Override
	public void removeEntities(Class clazz) {
		Set<String> keySet = getEntityKeySet(clazz);
		for (String key : keySet) {
			try {
				getCache().remove(key);
			}
			catch (InvalidValueException e) {
				logger.error(e.getMessage());
			}
		}
		keySet.clear();
		updateEntityKeySet(clazz, keySet);
	}

	@Override
	public void removeEntity(Class clazz, Object id) {
		String key = getEntityKey(clazz, id);
		if (getCache().containsKey(key)) {
			getCache().remove(getEntityKey(clazz, id));
		}
		removeEntityFromKeySet(clazz, key);
	}

	public SystemService getSystemService() {
		return systemService;
	}

	public void setSystemService(SystemService systemService) {
		this.systemService = systemService;
	}
	
	private Cache getCache() {
		return systemService.getCache();
	}

	@Override
	public CacheStat getStat() {
		return new CacheStat(calls, hits);
	}

}
