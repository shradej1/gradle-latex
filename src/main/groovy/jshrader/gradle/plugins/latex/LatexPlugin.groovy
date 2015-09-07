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

import jshrader.gradle.plugins.document.DocumentBinary
import org.gradle.model.Defaults
import org.gradle.model.ModelMap
import org.gradle.model.Path
import org.gradle.model.RuleSource
import org.gradle.platform.base.LanguageType
import org.gradle.platform.base.LanguageTypeBuilder

class LatexPlugin extends RuleSource {
    @LanguageType
    void declareLatexLanguage(LanguageTypeBuilder<LatexSourceSet> builder) {
        builder.setLanguageName("LaTeX")
        builder.defaultImplementation(DefaultLatexSourceSet)
    }

    @Defaults
    void createLatexPdfCompilerTasks(ModelMap<DocumentBinary> binaries, @Path("buildDir") File buildDir) {
        binaries.beforeEach { binary ->
            inputs.withType(LatexSourceSet.class) { latexSourceSet ->
                taskName = binary.name + name.capitalize() + "PdfLaTeXify"
                outputDir = new File(buildDir, "${binary.name}/src/${name}")
                binary.tasks.create(latexSourceSet.taskName, PdfLaTeXify) {
                    source = latexSourceSet.source
                    destinationDir = latexSourceSet.outputDir
                }
            }
        }
    }
}
