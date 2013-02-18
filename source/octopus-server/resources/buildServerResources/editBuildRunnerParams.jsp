<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="keys" class="octopus.teamcity.common.OctopusConstants" />
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="Octopus Packaging">
<tr>
  <th>Run OctoPack:</th>
  <td>
    <props:checkboxProperty name="${keys.runOctoPack}" />
    <span class="error" id="error_${keys.runOctoPack}"></span>
    <span class="smallNote">If checked, any projects with OctoPack installed will be packaged.</span>
  </td>
</tr>
<tr>
  <th>OctoPack package version:</th>
  <td>
    <props:textProperty name="${keys.octoPackPackageVersion}" className="longField"/>
    <span class="error" id="error_${keys.octoPackPackageVersion}"></span>
    <span class="smallNote">Package version number for NuGet packages created by OctoPack.</span>
  </td>
</tr>
</l:settingsGroup>
