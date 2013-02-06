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

package octopus.teamcity.server;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.util.EventDispatcher;
import octopus.teamcity.agent.OctoResult;
import octopus.teamcity.agent.OctoRunner;
import octopus.teamcity.agent.commands.CreateReleaseCommandBuilder;
import octopus.teamcity.agent.commands.ListEnvironmentsCommandBuilder;
import octopus.teamcity.server.ui.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class BuildStatusListener2 {
    private final OctoRunner octo;

    public BuildStatusListener(@NotNull final EventDispatcher<BuildServerListener> listener, @NotNull final OctoRunner octo) {
        this.octo = octo;

        listener.addListener(new BuildServerAdapter() {
            @Override
            public void beforeBuildFinish(SRunningBuild build) {
                build.addBuildMessage(DefaultMessagesInfo.createTextMessage("Reading Octopus Deploy parameters..."));
                Collection<FeatureInstance> parameters = readParameters(build);
                testSettings(parameters, build);
            }

            @Override
            public void buildFinished(SRunningBuild build) {
                Collection<FeatureInstance> parameters = readParameters(build);
                createRelease(parameters, build);
            }
        });
    }

    private Collection<FeatureInstance> readParameters(final SRunningBuild build) {
        Collection<FeatureInstance> results = new ArrayList<FeatureInstance>();

        SBuildType bt = build.getBuildType();
        if (bt == null) {
            return results;
        }

        for (SBuildFeatureDescriptor feature : bt.getBuildFeatures()) {
            if (!feature.getType().equals(OctopusReleaseFeature.FEATURE_TYPE))
                continue;

            final OctopusReleaseConstants constants = new OctopusReleaseConstants();

            Map<String, String> par = feature.getParameters();

            FeatureInstance parameters = new FeatureInstance();
            parameters.setApiKey(par.get(constants.getApiKey()));
            parameters.setServerUrl(par.get(constants.getServerKey()));
            parameters.setProjectName(par.get(constants.getProjectNameKey()));
            parameters.setDeployTo(par.get(constants.getDeployToKey()));
            results.add(parameters);
        }

        return results;
    }

    private void testSettings(Collection<FeatureInstance> parameters, SRunningBuild build) {
        for (FeatureInstance feature : parameters) {
            build.addBuildMessage(DefaultMessagesInfo.createTextMessage("After the build completes an Octopus Deploy release will be created. If there is a problem creating the release, it will appear in the TeamCity teamcity-server.log file. Testing that your Octopus Deploy connection settings work by calling the list-environments command."));

            ListEnvironmentsCommandBuilder command = new ListEnvironmentsCommandBuilder();
            command.setApiKey(feature.getApiKey());
            command.setServer(feature.getServerUrl());

            try {
                OctoResult result = octo.execute(command);
                build.addBuildMessage(DefaultMessagesInfo.createTextMessage(result.getOutput()));
                build.addBuildMessage(DefaultMessagesInfo.createTextMessage("Octo.exe exit code: " + result.getExitCode()));

                if (result.getExitCode() != 0) {
                    build.addBuildMessage(DefaultMessagesInfo.createBuildFailure("Octo.exe returned a non-zero exit code"));
                }
            } catch (Throwable e) {
                build.addBuildMessage(DefaultMessagesInfo.createBuildFailure("There was a problem when testing the Octopus Deploy connection: " + e.getLocalizedMessage()));

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                Loggers.SERVER.error(e);
                Loggers.SERVER.error(e.getLocalizedMessage());
                Loggers.SERVER.error(sw.toString());
            }
        }
    }

    private void createRelease(@NotNull final Collection<FeatureInstance> parameters, @NotNull final SRunningBuild build) {
        for (FeatureInstance feature : parameters) {
            Loggers.SERVER.info("Creating an Octopus Deploy release for project " + feature.getProjectName() + " on server " + feature.getServerUrl());

            CreateReleaseCommandBuilder command = new CreateReleaseCommandBuilder();
            command.setApiKey(feature.getApiKey());
            command.setServer(feature.getServerUrl());

            try {
                OctoResult result = octo.execute(command);

                Loggers.SERVER.info("Octo.exe exit code: " + result.getExitCode());

                if (result.getExitCode() != 0) {
                    Loggers.SERVER.error("Octo.exe returned a non-zero exit code");
                    Loggers.SERVER.error("Octo.exe output follows");
                    Loggers.SERVER.error(result.getOutput());
                }else {
                    Loggers.SERVER.info("Octopus Deploy release created successfully");
                }
            } catch (Throwable e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                Loggers.SERVER.error(e);
                Loggers.SERVER.error(e.getLocalizedMessage());
                Loggers.SERVER.error(sw.toString());
            }
        }
    }

    private class FeatureInstance {
        private String serverUrl;
        private String apiKey;
        private String projectName;
        private String deployTo;

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getDeployTo() {
            return deployTo;
        }

        public void setDeployTo(String deployTo) {
            this.deployTo = deployTo;
        }
    }
}