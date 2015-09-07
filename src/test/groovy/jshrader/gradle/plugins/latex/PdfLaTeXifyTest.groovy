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

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class PdfLaTeXifyTest extends Specification {
    @Rule public final TemporaryFolder tempFolder = new TemporaryFolder()
    File testFile

    def "only .tex files with \\documentclass recognized as top-level"(String filename,
                                                                       String contents,
                                                                       boolean pass) {
        given:
        testFile = tempFolder.newFile(filename)
        testFile << contents

        expect:
        PdfLaTeXify.isTopLevelSourceFile(testFile) == pass

        where:
        filename       | contents           || pass
        'non-tex.text' | "\\documentclass"  || false
        'test.tex'     | "%\\documentclass" || false
        'test.tex'     | "\\section"        || false
        'test.tex'     | "\\documentclass"  || true
    }
}
