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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OctopusCreateReleaseBuildService extends BuildServiceAdapter {
    @NotNull
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        return new ProgramCommandLine() {
            @NotNull
            public String getExecutablePath() throws RunBuildException {
                return "ping";
            }

            @NotNull
            public String getWorkingDirectory() throws RunBuildException {
                return "C:\\Windows";
            }

            @NotNull
            public List<String> getArguments() throws RunBuildException {
                List<String> items = new ArrayList<String>();
                items.add("localhost");
                return items;
            }

            @NotNull
            public Map<String, String> getEnvironment() throws RunBuildException {
                return new HashMap<String, String>();
            }
        };
    }
}
