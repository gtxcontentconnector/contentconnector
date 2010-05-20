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
/**
 * Request wrapper for accessing a {@link ServletRequest} or a
 * {@link PortletRequest} in a generic manner.
 * @author perhab
 *
 */
public class RequestWrapper implements ServletRequest, PortletRequest {

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
    return null;
  }

  public Enumeration getAttributeNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getCharacterEncoding() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getContentLength() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getContentType() {
    // TODO Auto-generated method stub
    return null;
  }

  public ServletInputStream getInputStream() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public Locale getLocale() {
    // TODO Auto-generated method stub
    return null;
  }

  public Enumeration getLocales() {
    // TODO Auto-generated method stub
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

  public Map getParameterMap() {
    // TODO Auto-generated method stub
    return null;
  }

  public Enumeration getParameterNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getParameterValues(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProtocol() {
    // TODO Auto-generated method stub
    return null;
  }

  public BufferedReader getReader() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRealPath(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRemoteAddr() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRemoteHost() {
    // TODO Auto-generated method stub
    return null;
  }

  public RequestDispatcher getRequestDispatcher(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getScheme() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getServerName() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getServerPort() {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean isSecure() {
    // TODO Auto-generated method stub
    return false;
  }

  public void removeAttribute(String arg0) {
    // TODO Auto-generated method stub

  }

  public void setAttribute(String arg0, Object arg1) {
    // TODO Auto-generated method stub

  }

  public void setCharacterEncoding(String arg0)
      throws UnsupportedEncodingException {
    // TODO Auto-generated method stub

  }

  public String getAuthType() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getContextPath() {
    // TODO Auto-generated method stub
    return null;
  }

  public Cookie[] getCookies() {
    // TODO Auto-generated method stub
    return null;
  }

  public PortalContext getPortalContext() {
    // TODO Auto-generated method stub
    return null;
  }

  public PortletMode getPortletMode() {
    // TODO Auto-generated method stub
    return null;
  }

  public PortletSession getPortletSession() {
    // TODO Auto-generated method stub
    return null;
  }

  public PortletSession getPortletSession(boolean arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public PortletPreferences getPreferences() {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<String, String[]> getPrivateParameterMap() {
    // TODO Auto-generated method stub
    return null;
  }

  public Enumeration<String> getProperties(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProperty(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Enumeration<String> getPropertyNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<String, String[]> getPublicParameterMap() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRemoteUser() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRequestedSessionId() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getResponseContentType() {
    // TODO Auto-generated method stub
    return null;
  }

  public Enumeration<String> getResponseContentTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  public Principal getUserPrincipal() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getWindowID() {
    // TODO Auto-generated method stub
    return null;
  }

  public WindowState getWindowState() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isPortletModeAllowed(PortletMode arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isRequestedSessionIdValid() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isUserInRole(String arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isWindowStateAllowed(WindowState arg0) {
    // TODO Auto-generated method stub
    return false;
  }

}
