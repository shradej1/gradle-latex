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

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification

class LatexPluginSpec extends Specification {
    @Rule final TemporaryFolder projectDir = new TemporaryFolder()
    File buildFile
    File singleMain
    File multiMain

    def setup() {
        buildFile = projectDir.newFile('build.gradle')
        configureSingleSrcDir()
        configureMultiSrcDir()

        // TODO: kludge to get SUT into the classpath of the build.
        def pluginClasspathResource = new File("build/createClasspathManifest/plugin-classpath.txt")
        if (!pluginClasspathResource.exists()) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        def pluginClasspath = pluginClasspathResource.readLines()
                .collect { it.replace('\\', '\\\\') } // escape backslashes in Windows paths
                .collect { "'$it'" }
                .join(", ")

        // Add the logic under test to the test build, apply the plugin, and configure the model
        buildFile << """
buildscript {
    dependencies {
        classpath files($pluginClasspath)
    }
}

import jshrader.gradle.plugins.latex.LatexPlugin
import jshrader.gradle.plugins.latex.LatexSourceSet
import jshrader.gradle.plugins.document.DocumentComponent
import jshrader.gradle.plugins.document.DocumentPlugin

apply plugin: jshrader.gradle.plugins.latex.LatexPlugin
apply plugin: jshrader.gradle.plugins.document.DocumentPlugin

model {
    components {
        single(DocumentComponent) {
            sources {
                latex(LatexSourceSet) {}
            }
        }

        multi(DocumentComponent) {
            sources {
                latex(LatexSourceSet) {}
            }
        }
    }
}
"""
    }

    private void configureSingleSrcDir() {
        File singleSrcDir = projectDir.newFolder('src', 'single', 'latex')

        singleMain = new File(singleSrcDir, 'main.tex')
        singleMain.createNewFile()
        singleMain.text = """
\\documentclass[12pt]{article}
\\begin{document}
\\section{Hello}
This is the gradle \\LaTeX plugin
\\section{World}
\\end{document}
"""
    }

    private void configureMultiSrcDir() {
        File multiSrcDir = projectDir.newFolder('src', 'multi', 'latex')
        multiMain = new File(multiSrcDir, 'main.tex')
        multiMain.createNewFile()
        multiMain.text = """
\\documentclass[12pt]{article}
\\begin{document}
\\section{Hello}
\\include{section1}
\\section{World}
\\end{document}
"""
        File section1 = new File(multiSrcDir, 'section1.tex')
        section1.createNewFile()
        section1.text = """
I'm in section1"""
    }

    def "tasks exist"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('singleBinary', 'multiBinary')
                .build()

        then:
        result.task(":singleBinary")
        result.task(":multiBinary")
    }

    @Ignore // TODO: figure out how to wire the component / binary / sourceset so compileSingleBinary is the main task
    def "compile task executes pdf texify"() {
        when:
        def result = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments('singleBinary')
        .build()

        then:
        result.task(':singleBinary')
        result.task(':compileSingleBinary')
        result.task(':singleBinaryLatexPdfLaTeXify')
    }

    def "produce pdf file"(String taskName, String outdir) {
        when:
        def result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments(taskName, '--info')
                .build()

        then:
        result.task(":$taskName").outcome == TaskOutcome.SUCCESS
        File outputDir = new File(projectDir.root, "build/$outdir/src/latex")
        outputDir.isDirectory()
        outputDir.listFiles().find { file -> file.name.endsWith('.pdf') } != null

        where:
        taskName | outdir
        'singleBinaryLatexPdfLaTeXify' | 'singleBinary'
        'multiBinaryLatexPdfLaTeXify' | 'multiBinary'
    }

    def "warn if no top-level file exists"() {
        when:
        assert singleMain.renameTo(new File(singleMain.parentFile, 'main.no-tex'))

        def result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('singleBinaryLatexPdfLaTeXify')
                .build()

        then:
        result.standardOutput.contains('No entry points found.')
    }

    def "fail and provide informative message if texify fails"() {
        when:
        singleMain.text = "\\documentclass\n\\unknownLatexCommand{}"

        def result = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('singleBinaryLatexPdfLaTeXify')
                .buildAndFail()

        then:
        result.standardError.contains('Failed to build')
        def matcher = result.standardError =~ /The log file should be located in (.*)\./
        matcher.find()
        def logDir = new File(matcher.group(1))
        logDir.isDirectory()
        logDir.listFiles().findAll { file -> file.name.endsWith('.log') }.size() != 0
    }

    @Ignore
    // TODO: integrate bibtex, and insure bibtex and texify are run the correct number of times to resolve citations
    def "resolve all citations"() {

    }
}
