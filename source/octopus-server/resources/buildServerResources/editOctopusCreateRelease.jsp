<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="keys" class="octopus.teamcity.common.OctopusConstants" />
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>


<l:settingsGroup title="Octopus Connection">
<tr>
  <th>Octopus URL:<l:star/></th>
  <td>
    <props:textProperty name="${keys.serverKey}" className="longField"/>
    <span class="error" id="error_${keys.serverKey}"></span>
    <span class="smallNote">Specify Octopus web portal URL</span>
  </td>
</tr>
<tr>
  <th>API key:<l:star/></th>
  <td>
    <props:passwordProperty name="${keys.apiKey}" className="longField"/>
    <span class="error" id="error_${keys.apiKey}"></span>
    <span class="smallNote">Specify Octopus API key. You can get this from your user page in the Octopus web portal.</span>
  </td>
</tr>
</l:settingsGroup>


<l:settingsGroup title="Release">
<tr>
  <th>Project:<l:star/></th>
  <td>
    <props:textProperty name="${keys.projectNameKey}" className="longField"/>
    <span class="error" id="error_${keys.projectNameKey}"></span>
    <span class="smallNote">Enter the name of the Octopus project to create a release for</span>
  </td>
</tr>
<tr>
  <th>Release number:</th>
  <td>
    <props:textProperty name="${keys.releaseNumberKey}" className="longField"/>
    <span class="error" id="error_${keys.releaseNumberKey}"></span>
    <span class="smallNote">The number to use for this release, e.g., <code>1.0.%build.number%</code>.</span>
  </td>
</tr>
</l:settingsGroup>


<l:settingsGroup title="Deployment">
<tr>
  <th>Deploy to:</th>
  <td>
    <props:textProperty name="${keys.deployToKey}" className="longField"/>
    <span class="error" id="error_${keys.deployToKey}"></span>
    <span class="smallNote">Comma separated list of environments to deploy to. Leave empty to create a release without deploying it.</span>
  </td>
</tr>
<tr>
  <th>Wait for deployment to complete:</th>
  <td>
    <props:checkboxProperty name="${keys.waitForDeployments}" />
    <span class="error" id="error_${keys.waitForDeployments}"></span>
    <span class="smallNote">If checked, the build process will only succeed if the deployment is successful.</span>
  </td>
</tr>
</l:settingsGroup>


<l:settingsGroup title="Advanced">
<tr>
  <th>Additional command line arguments:</th>
  <td>
    <props:textProperty name="${keys.commandLineArgumentsKey}" className="longField"/>
    <span class="error" id="error_${keys.commandLineArgumentsKey}"></span>
    <span class="smallNote">Additional arguments to be passed to <a href="https://github.com/OctopusDeploy/Octopus-Tools">Octo.exe</a></span>
  </td>
</tr>
</l:settingsGroup>