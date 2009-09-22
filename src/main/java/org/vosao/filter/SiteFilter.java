package org.vosao.filter;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.vosao.business.Business;
import org.vosao.dao.Dao;
import org.vosao.entity.PageEntity;


public class SiteFilter implements Filter {
    
    private static final Log log = LogFactory.getLog(SiteFilter.class);
	public static final String[] SKIP_URLS = {
		"/cms",
		"/static",
		"/login",
		"/file",
		"/init",
		"/initCron",
		"/JSON-RPC"};
    
    private FilterConfig config = null;
    private ServletContext servletContext = null;
  
    private Dao dao;
    private Business business;
    private PageEntity page;
    
    public SiteFilter() {
    }
  
    public void init(FilterConfig filterConfig) throws ServletException {
        config = filterConfig;
        servletContext = config.getServletContext();
    }
    
    private void prepare() {
        dao = (Dao)WebApplicationContextUtils
        	.getRequiredWebApplicationContext(servletContext)
    		.getBean("dao");
        business = (Business)WebApplicationContextUtils
    		.getRequiredWebApplicationContext(servletContext)
    		.getBean("business");
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, 
        FilterChain chain) throws IOException, ServletException {
        
    	prepare();
    	HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        String url = httpRequest.getServletPath();
        if (isSkipUrl(url)) {
            chain.doFilter(request, response);
            return;
        }
        if (isSiteUrl(url)) {
        	renderPage(httpRequest, httpResponse, url);
        	return;
        }
        httpResponse.sendRedirect("/");
    }
    
    public void destroy() {
    }

    private static boolean isSkipUrl(final String url) {
    	for (String u : SKIP_URLS) {
    		if (url.startsWith(u)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean isSiteUrl(final String url) {
    	page = dao.getPageDao().getByUrl(url);
    	if (page != null) {
        	return true;
    	}
    	return false;
    }
    
    private void renderPage(HttpServletRequest request, 
    		HttpServletResponse response, final String url) throws IOException {
    	response.setContentType("text/html");
    	response.setCharacterEncoding("UTF-8");
    	Writer out = response.getWriter();
    	String content = business.getPageBusiness().render(page);
    	out.write(content);
    }

    
}
