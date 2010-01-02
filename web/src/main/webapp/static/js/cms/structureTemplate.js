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

var structureTemplate = '';
var editMode = structureTemplateId != '';
var autosaveTimer = '';
    
$(function(){
    initJSONRpc(loadStructureTemplate);
    $("#tabs").tabs();
    $('#autosave').change(onAutosave);
    $('#bigLink').click(onBig);
    $('#smallLink').click(onSmall);
    $('#saveContinueButton').click(onSaveContinue);
    $('#saveButton').click(onSave);
    $('#cancelButton').click(onCancel);
});

function onSaveContinue() {
	onUpdate(true);
}

function onSave() {
	onUpdate(false);
}
    
function startAutosave() {
    if (structureTemplateId != 'null') {
        if (autosaveTimer == '') {
            autosaveTimer = setInterval(saveContent, AUTOSAVE_TIMEOUT * 1000);
        }
    }
}
    
function stopAutosave() {
    if (autosaveTimer != '') {
        clearInterval(autosaveTimer);
        autosaveTimer = '';
    }
}

function saveContent() {
	onUpdate(true);   
}

function onAutosave() {
    if ($("#autosave:checked").length > 0) {
        startAutosave(); 
    }
    else {
        stopAutosave();
    }
}

function loadStructureTemplate() {
	editMode = structureTemplateId != '';
	if (!editMode) {
		structureTemplate = null;
        initStructureTemplateForm();
	}
	jsonrpc.structureTemplateService.getById(function (r) {
		structureTemplate = r;
		if (editMode) {
			structureId = structureTemplate.structureId;
		}
		initStructureTemplateForm();
	}, structureTemplateId);
}

function initStructureTemplateForm() {
	if (structureTemplate != null) {
		$('#title').val(structureTemplate.title);
		$('#type').val(structureTemplate.typeString);
        $('#content').val(structureTemplate.content);
	}
	else {
        $('#title').val('');
        $('#type').val('VELOCITY');
        $('#content').val('');
	}
}

function onCancel() {
    location.href = '/cms/structure.jsp?id=' + structureId;
}

function onUpdate(cont) {
	var structureTemplateVO = javaMap({
	    id : structureTemplateId,
	    title : strip($('#title').val()),
	    type: $('#type').val(),
	    structureId: structureId,
        content : $('#content').val(),
	});
	jsonrpc.structureTemplateService.save(function (r) {
		if (r.result == 'success') {
			info('Structure template was successfully saved.');
			if (!cont) {
				onCancel();
			}
			else if (!editMode) {
				structureTemplateId = r.message;
				loadStructureTemplate();
			}
		}
		else {
			showServiceMessages(r);
		}			
	}, structureTemplateVO);
}

function onBig() {
	$('#content').attr('cols','120');
    $('#content').attr('rows','30');
}

function onSmall() {
    $('#content').attr('cols','80');
    $('#content').attr('rows','20');
}
