<%
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
%>

<%
    String parent = request.getParameter("parent");
    String query = "?id=" + request.getParameter("id") 
    		+ (parent == null ? "" : "&parent=" + parent);
%>
<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
    <li class="pageTab ui-corner-top ui-state-default">
        <a href="index.jsp<%= query %>"><fmt:message key="page" /></a>
    </li>
    <li class="contentTab ui-corner-top ui-state-default">
        <a href="content.jsp<%= query %>"><fmt:message key="content" /></a>
    </li>
    <li class="childrenTab ui-corner-top ui-state-default">
        <a href="children.jsp<%= query %>"><fmt:message key="page.children_pages" /></a>
    </li>
    <li class="commentsTab ui-corner-top ui-state-default">
        <a href="comments.jsp<%= query %>"><fmt:message key="comments" /></a>
    </li>
    <li class="securityTab ui-corner-top ui-state-default">
        <a href="security.jsp<%= query %>"><fmt:message key="security" /></a>
    </li>
    <li class="securityTab ui-corner-top ui-state-default">
        <a id="resources" href="#"><fmt:message key="resources" /></a>
    </li>
</ul>

<script type="text/javascript">

$(function() {
	$('#resources').click(onResources);
});

function onResources() {
	Vosao.jsonrpc.folderService.createFolderByPath(function(r) {
		$.cookie('folderReturnPath', '/cms/page/content.jsp?id=' + pageId, 
		    {path:'/', expires: 10});
		location.href = '/cms/folder.jsp?tab=1&id=' + r.id;
	}, '/page' + page.friendlyURL);
}

</script>