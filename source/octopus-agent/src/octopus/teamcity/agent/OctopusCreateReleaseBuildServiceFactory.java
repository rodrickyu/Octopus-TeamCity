package octopus.teamcity.agent;/*
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


import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.log.Loggers;
import octopus.teamcity.common.OctopusConstants;
import org.jetbrains.annotations.NotNull;

public class OctopusCreateReleaseBuildServiceFactory implements CommandLineBuildServiceFactory, AgentBuildRunnerInfo {
    private static final Logger LOG = Loggers.SERVER;

    private final ArtifactsWatcher myArtifactsWatcher;
    private InspectionReporter myInspectionsReporter;

    public OctopusCreateReleaseBuildServiceFactory(@NotNull final ArtifactsWatcher artifactsWatcher,
                                                   @NotNull final InspectionReporter inspectionsReporter) {
        myArtifactsWatcher = artifactsWatcher;
        myInspectionsReporter = inspectionsReporter;
    }

    @NotNull
    public String getType() {
        return OctopusConstants.CREATE_RELEASE_RUNNER_TYPE;
    }

    public boolean canRun(@NotNull final BuildAgentConfiguration agentConfiguration) {
        if (!agentConfiguration.getSystemInfo().isWindows()) {
            LOG.debug(getType() + " runner is supported only under Windows platform");
            return false;
        }
        return true;
    }


    @NotNull
    public CommandLineBuildService createService() {
        return new OctopusCreateReleaseBuildService();
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return this;
    }
}
