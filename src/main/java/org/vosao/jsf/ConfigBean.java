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

package org.vosao.jsf;
import java.io.IOException;
import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vosao.entity.ConfigEntity;
import org.vosao.servlet.MimeType;


public class ConfigBean extends AbstractJSFBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ConfigBean.class);
	
	private ConfigEntity config;
	
	public void init() {
		config = getBusiness().getConfigBusiness().getConfig();
	}
	
	public void export() throws IOException {
		log.debug("Exporting site.");
		HttpServletResponse response = JSFUtil.getResponse();
		response.setContentType(MimeType.getContentTypeByExt("zip"));
		String downloadFile = "exportSite.zip";
		response.addHeader("Content-Disposition", "attachment; filename=\"" 
				+ downloadFile + "\"");
		ServletOutputStream out = response.getOutputStream();
		byte[] file = getBusiness().getImportExportBusiness().createExportFile();
		response.setContentLength(file.length);
		out.write(file);
		out.flush();
		out.close();
		FacesContext.getCurrentInstance().responseComplete();
	}
	
	public void save() {
		getDao().getConfigDao().save(config);		
		JSFUtil.addInfoMessage("Configuration was successfully saved.");
	}

	public ConfigEntity getConfig() {
		return config;
	}

	public void setConfig(ConfigEntity config) {
		this.config = config;
	}
	
}
