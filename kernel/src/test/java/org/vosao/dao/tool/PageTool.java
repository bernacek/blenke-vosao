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

package org.vosao.dao.tool;

import org.vosao.dao.Dao;
import org.vosao.entity.PageEntity;
import org.vosao.enums.PageState;

public class PageTool {

	private Dao dao;
	
	public PageTool(Dao aDao) {
		dao = aDao;
	}
	
	public PageEntity addPage(final String title, 
			final String url) {
		return addPage(title, url, PageState.APPROVED);
	}
	
	public PageEntity addPage(final String name) {
		return addPage(name, "/" + name);
	}

	public PageEntity addPage(final String title, final String url, 
			PageState state) {
		PageEntity page = new PageEntity(title, url);
		page.setState(state);
		dao.getPageDao().save(page);
		return page;
	}
	
}
