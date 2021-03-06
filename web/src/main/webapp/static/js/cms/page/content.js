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
 
/**
 * Declared in page.jsp
 * 
 *   var pageId;
 *   var pageParentUrl;
 */

var page = null;
var contents = null;
var titles = null;
var currentLanguage = '';
var languages = null;
var editMode = pageId != '';
var contentEditor = null;
var etalonContent = '';
var autosaveTimer = '';
var pageRequest = null;
var contentEditors = null;
var browseId = '';
var editTextarea = false;
    
$(function(){
    $("#restore-dialog").dialog({ width: 400, autoOpen: false });
    Vosao.initJSONRpc(loadData);
    // hover states on the link buttons
    $('a.button').hover(
     	function() { $(this).addClass('ui-state-hover'); },
       	function() { $(this).removeClass('ui-state-hover'); }
    ); 
    initVersionDialog();
    $('#autosave').change(onAutosave);
    $('#language').change(onLanguageChange);
    $('#saveContinueContentButton').click(function(){onPageUpdate(true)});
    $('#pageForm').submit(function() {onPageUpdate(false); return false;});
    $('#contentPreviewButton').click(onPagePreview);
    $('#approveButton').click(onPageApprove);
    $('#restoreButton').click(onRestore);
    $('#contentCancelButton').click(onPageCancel);
    $('ul.ui-tabs-nav li:nth-child(2)').addClass('ui-state-active')
    		.removeClass('ui-state-default');
    $('#restoreForm').submit(function() {onRestoreSave(); return false;});
    $('#restoreCancelButton').click(onRestoreCancel);
});

function loadData() {
	Vosao.jsonrpc.pageService.getPageRequest(function(r) {
		pageRequest = r;
		page = pageRequest.page;
		loadLanguages();
		loadPage();
		breadcrumbsShow();
	}, pageId, pageParentUrl);
}

function callLoadPage() {
	Vosao.jsonrpc.pageService.getPageRequest(function(r) {
		pageRequest = r;
		page = pageRequest.page;
		editMode = pageId != null;
		loadPage();
	}, pageId, pageParentUrl);
}

function loadPage() {
	pageId = String(page.id);
	pageParentUrl = page.parentUrl;
	loadVersions();
	loadTitles();
	initPageForm();
	loadPagePermission();
	loadContents();
	$.cookie('fileBrowserPath', '/page' + page.friendlyURL, 
			{path:'/', expires: 10});
}

function loadLanguages() {
	var r = pageRequest.languages;
	languages = {};
	var h = '';
	$.each(r.list, function(i, value) {
		languages[value.code] = value;
		h += '<option value="' + value.code + '" ' + '>' 
			+ value.title + '</option>';
	});
	$('#language').html(h);
}

function initPageForm() {
	var urlEnd = pageParentUrl == '/' ? '' : '/';
	if (page != null) {
		$('#pageState').html(page.stateString == 'EDIT' ? messages.edit : 
				messages.approved);
		$('#pageCreateDate').html(page.createDateString);
		$('#pageModDate').html(page.modDateString);
		$('#pageCreateUser').html(page.createUserEmail);
		$('#pageModUser').html(page.modUserEmail);
		$('.contentTab').show();
		$('.childrenTab').show();
		$('.commentsTab').show();
		$('.securityTab').show();
		$('#pagePreview').show();
		$('#versions').show();
		showContentEditor();
	} else {
		$('#titleLocal').val('');
		$('#pageState').html(messages.edit);
		$('#pageCreateUser').html('');
		$('#pageCreateDate').html('');
		$('#pageModUser').html('');
		$('#pageModDate').html('');
		$('.contentTab').hide();
		$('.childrenTab').hide();
		$('.commentsTab').hide();
		$('.securityTab').hide();
		$('#pagePreview').hide();
		$('#versions').hide();
	}
}

function onPageUpdate(continueFlag) {
	var pageVO = Vosao.javaMap( {
		id : pageId,
		titles : getTitles(),
		content : getEditorContent(),
		approve : String($('#approveOnPageSave:checked, #approveOnContentSave:checked').size() > 0),
		languageCode : currentLanguage
	});
	Vosao.jsonrpc.pageService.savePage(function(r) {
		if (r.result == 'success') {
			Vosao.info(messages['page.success_save']);
			if (!continueFlag) {
				location.href = '/cms/pages.jsp';
			}
			else {
				callLoadPage();
			}
		} else {
			Vosao.showServiceMessages(r);
		}
	}, pageVO);
}
    
function startAutosave() {
	if (editMode) {
		if (autosaveTimer == '') {
			autosaveTimer = setInterval(saveContentByTimer, 
					Vosao.AUTOSAVE_TIMEOUT * 1000);
		}
	}
}

function saveContentByTimer() {
	onPageUpdate(true);
}

function stopAutosave() {
	if (autosaveTimer != '') {
		clearInterval(autosaveTimer);
		autosaveTimer = '';
	}
}

function getEditorContent() {
	if (!editMode) return '';
	if (page.simple) {
		if (editTextarea) {
			return $('#content').val();
		}
		else {
			return contentEditor.getData();
		}
	}
	if (page.structured) {
		var xml = '<?xml version="1.0" encoding="utf-8"?>\n<content>\n';
		$.each(pageRequest.structureFields.list, function(i, field) {
			if (field.type == 'TEXT' || field.type == 'DATE' 
				|| field.type == 'RESOURCE') {
				xml += '<' + field.name + '>' + $('#field' + field.name).val()
					+ '</' + field.name + '>\n';
			}
			if (field.type == 'TEXTAREA') {
				xml += '<' + field.name + '>'
					+ Vosao.escapeHtml(contentEditors[field.name].getData())  
					+ '</' + field.name + '>\n';
			}
		});
		return xml + '</content>';
	}
}

function setEditorContent(data) {
	if (page.simple) {
	    if (editTextarea) {
	    	$('#content').val(data);
	    }
	    else {
	    	contentEditor.setData(data);
	    }
	}
	if (page.structured) {
		var domData = $.xmlDOM(data, function(error) {
			Vosao.error(messages['page.parsing_error'] + ' ' + error);
		});
		$.each(pageRequest.structureFields.list, function(i, field) {
			if (field.type == 'TEXT' || field.type == 'DATE' 
				|| field.type == 'RESOURCE') {
				$(domData).find(field.name).each(function() {
					$('#field' + field.name).val($(this).text())					
				});
			}
			if (field.type == 'TEXTAREA') {
				$(domData).find(field.name).each(function() {
					contentEditors[field.name].setData($(this).text());					
				});
			}
		});
	}
}

function isContentChanged() {
	return contents[currentLanguage] != getEditorContent();
}

function onAutosave() {
	if ($("#autosave:checked").length > 0) {
		startAutosave();
	} else {
		stopAutosave();
	}
}

function onPagePreview() {
	var url = page.friendlyURL + '?language=' + currentLanguage 
		+ '&version=' + page.version;
	window.open(url, "preview");
}

function onPageCancel() {
	location.href = '/cms/pages.jsp';
}

function onLanguageChange() {
	if (!isContentChanged()
			|| confirm(messages.are_you_sure_changes_lost)) {
		currentLanguage = $('#language').val();
		if (contents[currentLanguage] == undefined) {
			contents[currentLanguage] = '';
		}
		setEditorContent(contents[currentLanguage]);
		$('#titleLocal').val(getTitle());
	} else {
		$('#language').val(currentLanguage);
	}
}

function loadContents() {
	if (editMode) {
		var r = pageRequest.contents;
		contents = [];
		$.each(r.list, function(i, value) {
			contents[value.languageCode] = value.content;
		});
		if (!currentLanguage) {
			if (languages[Vosao.ENGLISH_CODE] != undefined) {
				currentLanguage = Vosao.ENGLISH_CODE;
			}
			else {
				currentLanguage = r.list[0].languageCode;
			}
		}
		$('#language').val(currentLanguage);
		if (page.simple) {
			$('#editorButtons').html('<a href="#" onclick="onEditTextarea()">'
				+ messages['page.edit_textarea'] + '</a>');
		}
		setEditorContent(contents[currentLanguage]);
		$('#titleLocal').val(getTitle());
	} else {
		setEditorContent('');
	}
}

function onPageApprove() {
	Vosao.jsonrpc.pageService.approve(function(r) {
		Vosao.showServiceMessages(r);
		loadData();
	}, pageId);
}

function loadPagePermission() {
    var r = pageRequest.pagePermission;
   	if (r.publishGranted) {
   		$('#approveButton').show();
   		$('#approveOnPageSaveDiv').show();
   		$('#approveOnContentSaveDiv').show();
   	}
   	else {
   		$('#approveButton').hide();
   		$('#approveOnPageSaveDiv').hide();
   		$('#approveOnContentSaveDiv').hide();
   	}
   	if (r.changeGranted) {
   		$('#pageSaveButton').show();
   		$('#saveContinueContentButton').show();
   		$('#saveContentButton').show();
   	}
   	else {
   		$('#pageSaveButton').hide();
   		$('#saveContinueContentButton').hide();
   		$('#saveContentButton').hide();
   	}
   	if (r.admin && editMode) {
   		$('.securityTab').show();
   	}
   	else {
   		$('.securityTab').hide();
   	}
}

function showContentEditor() {
	$.each(CKEDITOR.instances, function(i,value) {
		CKEDITOR.remove(value);
	});
	if (page.simple) {
		$('#page-content').html('<textarea id="content" rows="20" cols="80"></textarea>');
	    if (!editTextarea) {
	    	contentEditor = CKEDITOR.replace('content', {
	    		height: 350, width: 'auto',
	    		filebrowserUploadUrl : '/cms/upload',
	    		filebrowserBrowseUrl : '/cms/fileBrowser.jsp',
	    		toolbar : 'Vosao'
	    	});
	    }
	}
	if (page.structured) {
		var h = '';
		$.each(pageRequest.structureFields.list, function(i, field) {
			h += '<div><div class="label">' + field.title + ':</div>';
			if (field.type == 'TEXT') {
				h += '<input id="field' + field.name + '" size="30"/>';
			}
			if (field.type == 'TEXTAREA') {
				h += '<textarea id="field' + field.name + '"></textarea>';
			}
			if (field.type == 'DATE') {
				h += '<input id="field' + field.name + '" class="datepicker" size="8" />';
			}
			if (field.type == 'RESOURCE') {
				h += '<input id="field' + field.name + '" size="60"/> '
					+ '<a href="#" onclick="browseResources(\'field' + field.name 
					+ '\')">' + messages.browse + '</a>';
			}
			h += '</div>';
		});
		$('#page-content').html(h);
		$('#page-content').css('float','left');
	    $(".datepicker").datepicker({dateFormat:'dd.mm.yy'});
		contentEditors = [];
		$.each(pageRequest.structureFields.list, function(i, field) {
			if (field.type == 'TEXTAREA') {
				if (contentEditors[field.name] == undefined) {
					var ckeditor = CKEDITOR.replace('field' + field.name, {
				        height: 150, width: 'auto',
				        filebrowserUploadUrl : '/cms/upload',
				        filebrowserBrowseUrl : '/cms/fileBrowser.jsp',
				        toolbar : 'Vosao'
				    });
					contentEditors[field.name] = ckeditor;
				}
			}
		});
	}
}

function browseResources(id) {
	browseId = id;
	window.open('/cms/fileBrowser.jsp?mode=page');
}

function setResource(path) {
	$('#' + browseId).val(path);
}

function loadTitles() {
	titles = page.titles.map;
}

function getTitle() {
	if (titles[currentLanguage] == undefined) {
		return '';
	}
	return titles[currentLanguage];
}

function getTitles() {
	if (!editMode) {
		return 'en' + $('#title').val();
	}
	titles[currentLanguage] = $('#titleLocal').val();
	var result = '';
	var count = 0;
	$.each(titles, function(lang, value) {
		var coma = count++ == 0 ? '' : ',';
		result += coma + lang + value;
	});
	return result;
}

function onRestore() {
	$("#restore-dialog").dialog('open');
}

function onRestoreCancel() {
	$("#restore-dialog").dialog('close');
}

function onRestoreSave() {
	var pageType = $('input[name=page]:checked').val();
	Vosao.jsonrpc.pageService.restore(function(r) {
		Vosao.showServiceMessages(r);
		if (r.result == 'success') {
			location.reload();
		}
	}, pageId, pageType, currentLanguage);
}

function onEditTextarea() {
	editTextarea = true;
	$('#editorButtons').html('<a href="#" onclick="onEditCKEditor()">'
		+ messages['page.edit_ckeditor'] + '</a>');
	showContentEditor();
	setEditorContent(contents[currentLanguage]);
}

function onEditCKEditor() {
	editTextarea = false;
	$('#editorButtons').html('<a href="#" onclick="onEditTextarea()">'
		+ messages['page.edit_textarea'] + '</a>');
	showContentEditor();
	setEditorContent(contents[currentLanguage]);
}