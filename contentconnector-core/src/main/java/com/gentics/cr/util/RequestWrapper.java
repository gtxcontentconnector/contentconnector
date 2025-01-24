package com.gentics.cr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import com.gentics.lib.log.NodeLogger;

/**
 * Request wrapper for accessing a {@link ServletRequest} in a generic manner.
 * @author perhab
 *
 */
public class RequestWrapper implements ServletRequest {

	/**
	 * Log4j logger for debug and error messages.
	 */
	private final static NodeLogger logger = NodeLogger.getNodeLogger(RequestWrapper.class);

	/**
	 * Internal variable for ServletRequest.
	 */
	private ServletRequest servletRequest = null;

	/**
	 * Wraps a {@link Servlet} into the {@link RequestWrapper}.
	 * @param request {@link ServletRequest} to wrap.
	 */
	public RequestWrapper(final HttpServletRequest request) {
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Encoding is not Supported for Servlet Reqquest.", e);
		}
		this.servletRequest = request;
	}

	/**
	 * get the wrapped request.
	 * @return wrapped request can be an instance {@link PortletRequest} or
	 * {@link ServletRequest}
	 */
	public final Object getRequest() {
		if (this.servletRequest != null) {
			return this.servletRequest;
		}
		return null;
	}

	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		logger.error("Method getAttribute(String) is not yet implemented.");
		return null;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		logger.error("Method getAttributeNames() is not yet implemented.");
		return null;
	}

	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		logger.error("Method getCharacterEncoding() is not yet implemented.");
		return null;
	}

	public int getContentLength() {
		// TODO Auto-generated method stub
		logger.error("Method getContentLength() is not yet implemented.");
		return 0;
	}

	public String getContentType() {
		// TODO Auto-generated method stub
		logger.error("Method getContentType() is not yet implemented.");
		return null;
	}

	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		logger.error("Method getInputStream() is not yet implemented.");
		return null;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		logger.error("Method getLocale() is not yet implemented.");
		return null;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		logger.error("Method getLocales() is not yet implemented.");
		return null;
	}

	/**
	 * Returns the value of the request parameter as String.
	 * @param name name of the parameter
	 * @return String representing the value of the parameter. <code>null</code>
	 * if the parameter doesn't exist.
	 */
	public final String getParameter(final String name) {
		if (this.servletRequest != null) {
			return this.servletRequest.getParameter(name);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		logger.error("Method getParameterMap() is not yet implemented.");
		return null;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getParameterNames() {
		// TODO Auto-generated method stub
		logger.error("Method getParameterNames() is not yet implemented.");
		return null;
	}

	/**
	 * Returns the value of the request parameter as array of {@link String}s.
	 * @param parameterName name of the parameter
	 * @return Array with {@link String}s representing the value of the parameter.
	 * <code>null</code> if the parameter doesn't exist.
	 */
	public final String[] getParameterValues(final String parameterName) {
		if (this.servletRequest != null) {
			return this.servletRequest.getParameterValues(parameterName);
		}
		return null;
	}

	public String getProtocol() {
		// TODO Auto-generated method stub
		logger.error("Method getProtocol() is not yet implemented.");
		return null;
	}

	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		logger.error("Method getReader() is not yet implemented.");
		return null;
	}

	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		logger.error("Method getRealPath() is not yet implemented.");
		return null;
	}

	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		logger.error("Method getRemoteAddr() is not yet implemented.");
		return null;
	}

	public String getRemoteHost() {
		// TODO Auto-generated method stub
		logger.error("Method getRemoteHost() is not yet implemented.");
		return null;
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		logger.error("Method getRequestDispatcher(String) is not yet implemented.");
		return null;
	}

	public String getScheme() {
		// TODO Auto-generated method stub
		logger.error("Method getScheme() is not yet implemented.");
		return null;
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		logger.error("Method getServerName() is not yet implemented.");
		return null;
	}

	public int getServerPort() {
		// TODO Auto-generated method stub
		logger.error("Method getServerPort() is not yet implemented.");
		return 0;
	}

	public boolean isSecure() {
		// TODO Auto-generated method stub
		logger.error("Method isSecure() is not yet implemented.");
		return false;
	}

	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		logger.error("Method removeAttribute(String) is not yet implemented.");

	}

	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		logger.error("Method setAttribute(Srting, Object) is not yet implemented.");
	}

	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		logger.error("Method setCharacterEncoding() is not yet implemented.");

	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		logger.error("Method getContentLengthLong() is not yet implemented.");
		return 0;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		logger.error("Method getRemotePort() is not yet implemented.");
		return 0;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		logger.error("Method getLocalName() is not yet implemented.");
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		logger.error("Method getLocalAddr() is not yet implemented.");
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		logger.error("Method getLocalPort() is not yet implemented.");
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		logger.error("Method getServletContext() is not yet implemented.");
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		logger.error("Method startAsync() is not yet implemented.");
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		logger.error("Method startAsync(ServletRequest, ServletResponse) is not yet implemented.");
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		logger.error("Method isAsyncStarted() is not yet implemented.");
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		logger.error("Method isAsyncSupported() is not yet implemented.");
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		logger.error("Method getAsyncContext() is not yet implemented.");
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		logger.error("Method getDispatcherType() is not yet implemented.");
		return null;
	}

	@Override
	public String getRequestId() {
		// TODO Auto-generated method stub
		logger.error("Method getRequestId() is not yet implemented.");
		return null;
	}

	@Override
	public String getProtocolRequestId() {
		// TODO Auto-generated method stub
		logger.error("Method getProtocolRequestId() is not yet implemented.");
		return null;
	}

	@Override
	public ServletConnection getServletConnection() {
		// TODO Auto-generated method stub
		logger.error("Method getServletConnection() is not yet implemented.");
		return null;
	}
}
