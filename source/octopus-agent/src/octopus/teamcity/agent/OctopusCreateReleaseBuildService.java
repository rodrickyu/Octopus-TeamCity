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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.artifacts.ArtifactDependencyInfo;
import octopus.teamcity.common.OctopusConstants;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OctopusCreateReleaseBuildService extends BuildServiceAdapter {

    private File myOutputDirectory;

    // When our task starts, extract Octo.exe
    @Override
    public void afterInitialized() throws RunBuildException {
        super.afterInitialized();

        try {
            myOutputDirectory = new File(getBuildTempDirectory(), "octo-temp");
            if (myOutputDirectory.exists())
                return;

            if (!myOutputDirectory.mkdirs())
                throw new RuntimeException("Unable to create temp output directory " + myOutputDirectory);

            EmbeddedResourceExtractor extractor = new EmbeddedResourceExtractor();
            extractor.extractTo(myOutputDirectory.getAbsolutePath());
        } catch (IOException e) {
            final String message = "Unable to create temporary file in " + getBuildTempDirectory() + " for Octopus: " + e.getMessage();
            Logger.getInstance(getClass().getName()).error(message, e);
            throw new RunBuildException(message);
        }
    }

    @Override
    public void beforeProcessStarted() throws RunBuildException {
        getLogger().progressMessage("Running Octopus Deploy");
    }

    // Now invoke Octo.exe
    @NotNull
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        Map<String, String> parameters = getRunnerParameters();

        final String apiKey = parameters.get(OctopusConstants.Instance.getApiKey());
        final String serverUrl = parameters.get(OctopusConstants.Instance.getServerKey());
        final String deployTo = parameters.get(OctopusConstants.Instance.getDeployToKey());
        final String projectName = parameters.get(OctopusConstants.Instance.getProjectNameKey());
        final String releaseNumber = parameters.get(OctopusConstants.Instance.getReleaseNumberKey());
        final String commandLineArguments = parameters.get(OctopusConstants.Instance.getCommandLineArgumentsKey());

        getLogger().progressMessage("Octopus arguments:");
        getLogger().progressMessage("  Deploy to: " + deployTo);
        getLogger().progressMessage("  Project name: " + projectName);
        getLogger().progressMessage("  Release number: " + releaseNumber);
        getLogger().progressMessage("  Command line arguments: " + commandLineArguments);

        return new ProgramCommandLine() {
            @NotNull
            public String getExecutablePath() throws RunBuildException {
                return new File(myOutputDirectory, "Octo.exe").getAbsolutePath();
            }

            @NotNull
            public String getWorkingDirectory() throws RunBuildException {
                return myOutputDirectory.getAbsolutePath();
            }

            @NotNull
            public List<String> getArguments() throws RunBuildException {
                List<String> items = new ArrayList<String>();
                items.add("create-release");
                items.add("--server=\"" + serverUrl + "\"");
                items.add("--apikey=\"" + apiKey + "\"");
                items.add("--project=\"" + projectName + "\"");
                items.add("--deployto=\"" + deployTo + "\"");
                items.add("--version=\"" + releaseNumber + "\"");
                return items;
            }

            @NotNull
            public Map<String, String> getEnvironment() throws RunBuildException {
                return getBuildParameters().getEnvironmentVariables();
            }
        };
    }
}
