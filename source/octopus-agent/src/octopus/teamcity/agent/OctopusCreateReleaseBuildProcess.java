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
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.runner.LoggingProcessListener;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import octopus.teamcity.agent.commands.CreateReleaseCommandBuilder;
import octopus.teamcity.agent.commands.OctopusCommandBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class OctopusCreateReleaseBuildProcess implements BuildProcess {
    private final AgentRunningBuild runningBuild;
    private final BuildRunnerContext context;
    private Process process;
    private File extractedTo;
    private OutputReaderThread standardError;
    private OutputReaderThread standardOutput;
    private boolean isFinished;
    private BuildProgressLogger logger;

    public OctopusCreateReleaseBuildProcess(@NotNull AgentRunningBuild runningBuild, @NotNull BuildRunnerContext context) {
        this.runningBuild = runningBuild;
        this.context = context;
    }

    public void start() throws RunBuildException {
        extractOctoExe();

        OctopusCommandBuilder arguments = new CreateReleaseCommandBuilder(context.getRunnerParameters());
        startOcto(arguments);
    }

    private void extractOctoExe() throws RunBuildException {
        final File tempDirectory = runningBuild.getBuildTempDirectory();
        try {
            extractedTo = new File(tempDirectory, "octo-temp");
            if (extractedTo.exists())
                return;

            if (!extractedTo.mkdirs())
                throw new RuntimeException("Unable to create temp output directory " + extractedTo);

            EmbeddedResourceExtractor extractor = new EmbeddedResourceExtractor();
            extractor.extractTo(extractedTo.getAbsolutePath());
        } catch (IOException e) {
            final String message = "Unable to create temporary file in " + tempDirectory + " for Octopus: " + e.getMessage();
            Logger.getInstance(getClass().getName()).error(message, e);
            throw new RunBuildException(message);
        }
    }

    private void startOcto(final OctopusCommandBuilder command) throws RunBuildException {
        String userVisibleCommand = command.buildMaskedCommand();
        String realCommand = command.buildCommand();

        logger = runningBuild.getBuildLogger();
        logger.activityStarted("Octopus Deploy", DefaultMessagesInfo.BLOCK_TYPE_INDENTATION);
        logger.message("Running command:   octo.exe " + userVisibleCommand);
        logger.progressMessage("Creating Octopus Deploy release");

        try {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec("\"" + new File(extractedTo, "octo.exe").getAbsolutePath() + "\" " + realCommand, null, context.getWorkingDirectory());

            final LoggingProcessListener listener = new LoggingProcessListener(logger);

            standardError = new OutputReaderThread(process.getErrorStream(), new OutputWriter() {
                public void write(String text) {
                    listener.onErrorOutput(text);
                }
            });
            standardOutput = new OutputReaderThread(process.getInputStream(), new OutputWriter() {
                public void write(String text) {
                    listener.onStandardOutput(text);
                }
            });

            standardError.start();
            standardOutput.start();
        } catch (IOException e) {
            final String message = "Error from Octo.exe: " + e.getMessage();
            Logger.getInstance(getClass().getName()).error(message, e);
            throw new RunBuildException(message);
        }
    }

    public boolean isInterrupted() {
        return false;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void interrupt() {
        if (process != null) {
            process.destroy();
        }
    }

    @NotNull
    public BuildFinishedStatus waitFor() throws RunBuildException {
        int exitCode;

        try {
            exitCode = process.waitFor();

            standardError.join();
            standardOutput.join();

            logger.message("Octo.exe exit code: " + exitCode);
            logger.activityFinished("Octopus Deploy", DefaultMessagesInfo.BLOCK_TYPE_INDENTATION);

            isFinished = true;
        }
        catch (InterruptedException e) {
            isFinished = true;
            final String message = "Unable to wait for Octo.exe: " + e.getMessage();
            Logger.getInstance(getClass().getName()).error(message, e);
            throw new RunBuildException(message);
        }

        if (exitCode == 0)
            return BuildFinishedStatus.FINISHED_SUCCESS;

        runningBuild.getBuildLogger().progressFinished();

        String message = "Unable to create Octopus Deploy release. Please check the build log for details on the error.";

        if (runningBuild.getFailBuildOnExitCode()) {
            runningBuild.getBuildLogger().buildFailureDescription(message);
            return BuildFinishedStatus.FINISHED_FAILED;
        } else {
            runningBuild.getBuildLogger().error(message);
            return BuildFinishedStatus.FINISHED_SUCCESS;
        }
    }

    private interface OutputWriter {
        void write(String text);
    }

    private class OutputReaderThread extends Thread {
        private final InputStream is;
        private final OutputWriter output;

        OutputReaderThread(InputStream is, OutputWriter output) {
            this.is = is;
            this.output = output;
        }

        public void run() {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            try {
                while ((line = br.readLine()) != null) {
                    output.write(line.replaceAll("[\\r\\n]", ""));
                }
            } catch (IOException e) {
                output.write("ERROR: " + e.getMessage());
            }
        }
    }
}
