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

package octopus.teamcity.agent.commands;

import octopus.teamcity.common.OctopusConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateReleaseCommandBuilder implements OctopusCommandBuilder {
    private final OctopusConstants constants;
    private final Map<String, String> parameters;

    public CreateReleaseCommandBuilder(Map<String, String> parameters) {
        this.parameters = parameters;
        this.constants = OctopusConstants.Instance;
    }

    public String buildCommand() {
        return buildCommand(false);
    }

    public String buildMaskedCommand() {
        return buildCommand(true);
    }

    private String buildCommand(boolean masked) {
        final String serverUrl = parameters.get(constants.getServerKey());
        final String apiKey = parameters.get(constants.getApiKey());
        final String commandLineArguments = parameters.get(constants.getCommandLineArgumentsKey());
        final String releaseNumber = parameters.get(constants.getReleaseNumberKey());
        final String deployTo = parameters.get(constants.getDeployToKey());
        final String projectName = parameters.get(constants.getProjectNameKey());
        final boolean wait = Boolean.parseBoolean(parameters.get(constants.getWaitForDeployments()));

        StringBuilder builder = new StringBuilder();
        builder.append("create-release").append(" ");
        builder.append("--server=").append(Q(serverUrl)).append(" ");
        builder.append("--apikey=").append(masked ? "<secret>" : Q(apiKey)).append(" ");
        builder.append("--project=").append(Q(projectName)).append(" ");
        builder.append("--version=").append(Q(releaseNumber)).append(" ");
        builder.append("--enableservicemessages").append(" ");

        for (String env : splitEnvironmentNames(deployTo)) {
            builder.append("--deployto=").append(Q(env)).append(" ");
        }

        if (wait && deployTo != null && !deployTo.isEmpty()) {
            builder.append("--waitfordeployment ");
        }

        if (commandLineArguments != null && !commandLineArguments.isEmpty()) {
            builder.append(commandLineArguments);
        }

        return builder.toString();
    }

    private String Q(String value) {
        return "\"" + value + "\"";
    }

    private static List<String> splitEnvironmentNames(String deployTo) {
        List<String> results = new ArrayList<String>();
        String line = deployTo;
        String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String t : tokens) {
            String trimmed = t.trim();
            if (trimmed.length() > 0) {
                results.add(trimmed);
            }
        }

        return results;
    }
}

