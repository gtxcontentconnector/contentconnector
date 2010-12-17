package com.gentics.cr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
/**
 * Request wrapper for accessing a {@link ServletRequest} or a
 * {@link PortletRequest} in a generic manner.
 * @author perhab
 *
 */
public class RequestWrapper implements ServletRequest, PortletRequest {

  /**
   * Log4j logger for debug and error messages
   */
  private final static Logger logger = Logger.getLogger(RequestWrapper.class);

  /**
   * Internal variable for ServletRequest.
   */
  private ServletRequest servletRequest = null;
  /**
   * Internal variable for PortletRequest.
   */
  private PortletRequest portletRequest = null;

  /**
   * Wraps a {@link Servlet} into the {@link RequestWrapper}.
   * @param request {@link ServletRequest} to wrap.
   */
  public RequestWrapper(final HttpServletRequest request) {
    this.servletRequest = request;
  }

  /**
   * Wraps a {@link Portlet} into the {@link RequestWrapper}.
   * @param request {@link PortletRequest} to wrap
   */
  public RequestWrapper(final PortletRequest request) {
    this.portletRequest = request;
  }

  /**
   * get the wrapped request.
   * @return wrapped request can be an instance {@link PortletRequest} or
   * {@link ServletRequest}
   */
  public final Object getRequest() {
    if (this.portletRequest != null) {
      return this.portletRequest;
    } else if (this.servletRequest != null) {
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
    if (this.portletRequest != null) {
      return portletRequest.getParameter(name);
    } else if (this.servletRequest != null) {
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
    if (this.portletRequest != null) {
      return this.portletRequest.getParameterValues(parameterName);
    } else if (this.servletRequest != null) {
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

  public void setCharacterEncoding(String arg0)
      throws UnsupportedEncodingException {
    // TODO Auto-generated method stub
    logger.error("Method setCharacterEncoding() is not yet implemented.");

  }

  public String getAuthType() {
    // TODO Auto-generated method stub
    logger.error("Method getAuthType() is not yet implemented.");
    return null;
  }

  public String getContextPath() {
    // TODO Auto-generated method stub
    logger.error("Method getContextPath() is not yet implemented.");
    return null;
  }

  public Cookie[] getCookies() {
    // TODO Auto-generated method stub
    logger.error("Method getCookies() is not yet implemented.");
    return null;
  }

  public PortalContext getPortalContext() {
    // TODO Auto-generated method stub
    logger.error("Method getPortalContext() is not yet implemented.");
    return null;
  }

  public PortletMode getPortletMode() {
    // TODO Auto-generated method stub
    logger.error("Method getPortletMode() is not yet implemented.");
    return null;
  }

  public PortletSession getPortletSession() {
    // TODO Auto-generated method stub
    logger.error("Method getPortletSession() is not yet implemented.");
    return null;
  }

  public PortletSession getPortletSession(boolean arg0) {
    // TODO Auto-generated method stub
    logger.error("Method getPortletSession(boolean) is not yet implemented.");
    return null;
  }

  public PortletPreferences getPreferences() {
    // TODO Auto-generated method stub
    logger.error("Method getPreferences() is not yet implemented.");
    return null;
  }

  public Map<String, String[]> getPrivateParameterMap() {
    // TODO Auto-generated method stub
    logger.error("Method getPrivateParameterMap() is not yet implemented.");
    return null;
  }

  public Enumeration<String> getProperties(String arg0) {
    // TODO Auto-generated method stub
    logger.error("Method getProperties(String) is not yet implemented.");
    return null;
  }

  public String getProperty(String arg0) {
    // TODO Auto-generated method stub
    logger.error("Method getProperty(String) is not yet implemented.");
    return null;
  }

  public Enumeration<String> getPropertyNames() {
    // TODO Auto-generated method stub
    logger.error("Method getPropertyNames() is not yet implemented.");
    return null;
  }

  public Map<String, String[]> getPublicParameterMap() {
    // TODO Auto-generated method stub
    logger.error("Method getPublicParameterMap() is not yet implemented.");
    return null;
  }

  public String getRemoteUser() {
    // TODO Auto-generated method stub
    logger.error("Method getRemoteUser() is not yet implemented.");
    return null;
  }

  public String getRequestedSessionId() {
    // TODO Auto-generated method stub
    logger.error("Method getRequestedSessionId() is not yet implemented.");
    return null;
  }

  public String getResponseContentType() {
    // TODO Auto-generated method stub
    logger.error("Method getResponseContentType() is not yet implemented.");
    return null;
  }

  public Enumeration<String> getResponseContentTypes() {
    // TODO Auto-generated method stub
    logger.error("Method getResponseContentTypes() is not yet implemented.");
    return null;
  }

  public Principal getUserPrincipal() {
    // TODO Auto-generated method stub
    logger.error("Method getuserPrincipal() is not yet implemented.");
    return null;
  }

  public String getWindowID() {
    // TODO Auto-generated method stub
    logger.error("Method getWindowID() is not yet implemented.");
    return null;
  }

  public WindowState getWindowState() {
    // TODO Auto-generated method stub
    logger.error("Method getWindowState() is not yet implemented.");
    return null;
  }

  public boolean isPortletModeAllowed(PortletMode arg0) {
    // TODO Auto-generated method stub
    logger.error("Method isPortletModeAllowed(PortletMode) is not yet implemented.");
    return false;
  }

  public boolean isRequestedSessionIdValid() {
    // TODO Auto-generated method stub
    logger.error("Method isRequestedSessionIdValid() is not yet implemented.");
    return false;
  }

  public boolean isUserInRole(String arg0) {
    // TODO Auto-generated method stub
    logger.error("Method isUserInRole(String) is not yet implemented.");
    return false;
  }

  public boolean isWindowStateAllowed(WindowState arg0) {
    // TODO Auto-generated method stub
    logger.error("Method isWindowStateAllowed(WindowState) is not yet implemented.");
    return false;
  }

}
