/**
 Copyright 2014 Evan Tatarka

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.buffalo.cse.maybe

import org.gradle.api.JavaVersion
import org.gradle.api.ProjectConfigurationException

//import static me.tatarka.RetrolambdaPlugin.javaVersionToBytecode

/**
 * Created with IntelliJ IDEA.
 * User: evan
 * Date: 8/4/13
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class MaybeExtension {
    String packageName = null;
    String version = null;

    public MaybeExtension() {
    }

    public void setPackageName(String name) {
        packageName = name
    }

    public String getPackageName() {
        return packageName
    }

    public void setVersion(String v) {
        version = v
    }

    public String getVersion() {
        return version
    }
}
