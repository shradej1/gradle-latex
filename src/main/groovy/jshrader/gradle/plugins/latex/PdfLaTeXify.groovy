/*
 *    Copyright 2015 Joshua Shrader
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package jshrader.gradle.plugins.latex

import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class PdfLaTeXify extends SourceTask {
    @OutputDirectory
    File destinationDir

    static boolean isTopLevelSourceFile(File file) {
        return file.name.endsWith(".tex") && file.readLines().find { it ==~ /^[^%]*\\documentclass.*/ } != null
    }

    @TaskAction
    void process() {
        def entryPoints = source.filter { isTopLevelSourceFile(it) }
        if(entryPoints.empty) {
            logger.warn("No entry points found.  There should be one .tex file declaring a \\documentclass " +
                    "for each document to generate.")
        }

        entryPoints.each { main ->
            logger.info("Compiling $main")
            def procBuilder = new ProcessBuilder(["texify",
                                                  "--pdf",
                                                  "--tex-option=--interaction=nonstopmode",
                                                  "--tex-option=--synctex=-1",
                                                  main.toString()])
                    .directory(destinationDir)

            if (logger.isInfoEnabled()) {
                println "Executing ${procBuilder.command()}"
                procBuilder.inheritIO()
            }

            def proc = procBuilder.start()

            StringBuilder output = new StringBuilder()
            StringBuilder error = new StringBuilder()
            proc.waitForProcessOutput(output, error)

            if (proc.waitFor() != 0) {
                throw new GradleException("Failed to build ${main}.\n  " +
                        "The log file should be located in $destinationDir.\n  " +
                        "Try running with --info to see process output.\n " + error, null)
            }
        }
    }
}