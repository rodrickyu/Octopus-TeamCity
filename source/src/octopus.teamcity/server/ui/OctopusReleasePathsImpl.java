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

package octopus.teamcity.server.ui;

import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

public class OctopusReleasePathsImpl implements OctopusReleasePaths {
    private final PluginDescriptor myDescriptor;

    public OctopusReleasePathsImpl(@NotNull final PluginDescriptor descriptor) {
        myDescriptor = descriptor;
    }

    @NotNull
    public String getControllerPath() {
        return myDescriptor.getPluginResourcesPath("feature.html");
    }
}