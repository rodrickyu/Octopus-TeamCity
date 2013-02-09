/*
 * Copyright 2000-2012 Octopus Deploy Pty. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package octopus.teamcity.agent;

import jetbrains.buildServer.agent.*;
import octopus.teamcity.common.OctopusConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OctopusDeployReleaseBuildProcess extends OctopusBuildProcess {
   public OctopusDeployReleaseBuildProcess(@NotNull AgentRunningBuild runningBuild, @NotNull BuildRunnerContext context) {
       super(runningBuild, context);
    }

    @Override
    protected String getLogMessage() {
        return "Promoting Octopus Deploy release";
    }

    @Override
    protected OctopusCommandBuilder createCommand() {
        final Map<String, String> parameters = getContext().getRunnerParameters();
        final OctopusConstants constants = OctopusConstants.Instance;

        return new OctopusCommandBuilder() {
            @Override
            protected String buildCommand(boolean masked) {
                final String serverUrl = parameters.get(constants.getServerKey());
                final String apiKey = parameters.get(constants.getApiKey());
                final String commandLineArguments = parameters.get(constants.getCommandLineArgumentsKey());
                final String releaseNumber = parameters.get(constants.getReleaseNumberKey());
                final String deployTo = parameters.get(constants.getDeployToKey());
                final String projectName = parameters.get(constants.getProjectNameKey());
                final boolean wait = Boolean.parseBoolean(parameters.get(constants.getWaitForDeployments()));

                StringBuilder builder = new StringBuilder();
                builder.append("deploy-release").append(" ");
                builder.append("--server=").append(Quote(serverUrl)).append(" ");
                builder.append("--apikey=").append(masked ? "SECRET" : Quote(apiKey)).append(" ");
                builder.append("--project=").append(Quote(projectName)).append(" ");
                builder.append("--enableservicemessages").append(" ");

                if (releaseNumber != null && !releaseNumber.isEmpty()) {
                    builder.append("--version=").append(Quote(releaseNumber)).append(" ");
                }

                for (String env : splitCommaSeparatedValues(deployTo)) {
                    builder.append("--deployto=").append(Quote(env)).append(" ");
                }

                if (wait && deployTo != null && !deployTo.isEmpty()) {
                    builder.append("--waitfordeployment ");
                }

                if (commandLineArguments != null && !commandLineArguments.isEmpty()) {
                    builder.append(commandLineArguments);
                }

                return builder.toString();
            }
        };
    }
}
