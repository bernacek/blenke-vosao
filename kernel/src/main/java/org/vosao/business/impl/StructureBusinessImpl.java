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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.vosao.business.StructureBusiness;
import org.vosao.business.vo.StructureFieldVO;
import org.vosao.entity.StructureEntity;

import com.google.appengine.repackaged.com.google.common.base.StringUtil;

/**
 * @author Alexander Oleynik
 */
public class StructureBusinessImpl extends AbstractBusinessImpl 
	implements StructureBusiness {

	private static final Log logger = LogFactory.getLog(StructureBusinessImpl.class);
	
	@Override
	public List<String> validateBeforeUpdate(final StructureEntity entity) {
		List<String> errors = new ArrayList<String>();
		StructureEntity foundStructure = getDao().getStructureDao().getByTitle(
				entity.getTitle());
		if (entity.getId() == null) {
			if (foundStructure != null) {
				errors.add("Structure with such title already exists");
			}
		}
		else {
			if (foundStructure != null && !foundStructure.getId().equals(entity.getId())) {
				errors.add("Structure with such title already exists");
			}
		}
		if (StringUtil.isEmpty(entity.getTitle())) {
			errors.add("Title is empty");
		}
		try {
			Document document = DocumentHelper.parseText(entity.getContent());
			if (!document.getRootElement().getName().equals("structure")) {
				errors.add("XML document must have 'structure' root element.");
			}
		} catch (DocumentException e) {
			errors.add("Problems parsing content. " + e.getMessage());
		}
		return errors;
	}

	@Override
	public List<StructureFieldVO> getFields(StructureEntity structure) {
		try {
			Document doc = DocumentHelper.parseText(structure.getContent());
			List<StructureFieldVO> result = new ArrayList<StructureFieldVO>();
			for (Iterator<Element> i = doc.getRootElement().elementIterator(); 
					i.hasNext(); ) {
					Element element = i.next();
					if (element.getName().equals("field")) {
						result.add(new StructureFieldVO(
								element.elementText("title"),
								element.elementText("name"),
								element.elementText("type")));
					}
			}
			return result;
		} catch (DocumentException e) {
			logger.error(e.getMessage());
			return Collections.EMPTY_LIST;
		}	
	}
	
}