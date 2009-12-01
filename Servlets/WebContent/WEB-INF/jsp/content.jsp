<%@ taglib prefix="gtx" uri="http://www.gentics.com/taglib/rendering"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/ea/core"%>

<c:forEach var="entry" items="${objects}" varStatus="counter">
			
			<gtx:render object="${entry}" contentAttribute="name" var="name" />
			hello ${name}<br/>
</c:forEach>
