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

package org.vosao.business.impl;

import java.util.ArrayList;
import java.util.List;

import org.vosao.business.GroupBusiness;
import org.vosao.common.VosaoContext;
import org.vosao.entity.GroupEntity;
import org.vosao.entity.UserEntity;
import org.vosao.entity.UserGroupEntity;
import org.vosao.i18n.Messages;

import com.google.appengine.repackaged.com.google.common.base.StringUtil;

/**
 * @author Alexander Oleynik
 */
public class GroupBusinessImpl extends AbstractBusinessImpl 
	implements GroupBusiness {

	@Override
	public List<String> validateBeforeUpdate(final GroupEntity group) {
		List<String> errors = new ArrayList<String>();
		GroupEntity foundGroup = getDao().getGroupDao().getByName(
				group.getName());
		if (group.getId() == null) {
			if (foundGroup != null) {
				errors.add(Messages.get("group_already_exists"));
			}
		}
		else {
			if (foundGroup != null && !foundGroup.getId().equals(group.getId())) {
				errors.add(Messages.get("group_already_exists"));
			}
		}
		if (StringUtil.isEmpty(group.getName())) {
			errors.add(Messages.get("name_is_empty"));
		}
		return errors;
	}

	@Override
	public void addUserToGroup(GroupEntity group, UserEntity user) {
		UserGroupEntity userGroup = getDao().getUserGroupDao().getByUserGroup(
				group.getId(), user.getId());
		if (userGroup == null) {
			getDao().getUserGroupDao().save(new UserGroupEntity(
					group.getId(), user.getId()));
		}
	}
	
	@Override
	public void remove(List<Long> ids) {
		if (VosaoContext.getInstance().getUser().isAdmin()) {
			getDao().getUserGroupDao().removeByGroup(ids);
			getDao().getContentPermissionDao().removeByGroup(ids);
			getDao().getFolderPermissionDao().removeByGroup(ids);
			getDao().getGroupDao().remove(ids);
		}
	}
	
}
