<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="gtx" uri="http://www.gentics.com/taglib/rendering"%>
 <ul>
 <c:forEach var="item" items="${items}">
    <li> <a href="<gtx:simplelink object="${item}" linkAttribute="startpage" />"><gtx:render object="${item}" contentAttribute="name" /></a>
    	<c:if test="${item.childRepository != null}">
	        <c:set var="items" value="${item.childRepository}" scope="request"/>
	     	<jsp:include page="nav.jsp"/>
    	</c:if>
    </li>
</c:forEach>  
</ul>