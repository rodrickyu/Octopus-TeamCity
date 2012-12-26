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

package octopus.teamcity.octo;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import octopus.teamcity.octo.commands.OctopusCommandBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class OctoRunner {
    private final Object sync = new Object();
    private String pathToOctoExe;

    public OctoRunner() {
    }

    public OctoResult execute(OctopusCommandBuilder builder) throws IOException, InterruptedException {
        synchronized (sync) {
            if (pathToOctoExe == null) {
                initialize();
            }
        }

        String fullCommand = "\"" + pathToOctoExe + "\" " + builder.buildCommand();
        return runProcess(fullCommand);
    }

    private void initialize() throws IOException {
        EmbeddedResourceExtractor extractor = new EmbeddedResourceExtractor();
        extractor.extractTo("C:\\Temp");

        pathToOctoExe = "C:\\Temp\\Octo.exe";
    }

    private OctoResult runProcess(String command) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);

        OutputReaderThread standardError = new OutputReaderThread(process.getErrorStream(), output);
        OutputReaderThread standardOutput = new OutputReaderThread(process.getInputStream(), output);

        standardError.start();
        standardOutput.start();

        int exitCode = process.waitFor();

        standardError.join();
        standardOutput.join();

        return new OctoResult(output.toString(), exitCode);
    }

    private class OutputReaderThread extends Thread {
        private final InputStream is;
        private final StringBuilder output;

        OutputReaderThread(InputStream is, StringBuilder output) {
            this.is = is;
            this.output = output;
        }

        public void run() {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            try {
                while ((line = br.readLine()) != null) {
                    synchronized (output) {
                        output.append(line).append("\n");
                    }
                }
            } catch (IOException e) {
                synchronized (output) {
                    output.append("Error: ").append(e.toString()).append("\n");
                }
            }
        }
    }

}

