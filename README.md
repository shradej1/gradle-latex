# gradle-latex
A gradle plugin to build LaTeX documents

## About
This plugin can currently only produce PDF files (texify --pdf), and has only been tested
on Windows 10 using MiKTeX 2.9 and Gradle 2.7-rc-2.

This project is largely a work-in-progress and represents my first foray into writing
plugins using the new Gradle Model and plugin TestKit.  Initial implementation and `DocumentPlugin`
are largely based on the `customModel/languageType` sample build available from the Gradle distribution.

## Usage Example
Apply `LatexPlugin` and `DocumentPlugin`, define the model elements,

    apply plugin: jshrader.gradle.plugins.latex.LatexPlugin
    apply plugin: jshrader.gradle.plugins.document.DocumentPlugin

    model {
        components {
            doc(DocumentComponent) {
                sources {
                    latex(LatexSourceSet){
                    }
                }
            }
        }
    }

and add your `.tex` files.

<pre>
    build.gradle
    src/
    |-- doc
        `-- latex
            |-- main.tex
            `-- other.tex
</pre>

All `.tex` files that contain a (non-commented) `\documentclass` command will be processed by `texify --pdf`, and output
will be generated in the project `buildDir` directory.

Declaring a `doc` component generates `docBinary`, `compileDocBinary`, and `docBinaryLatexPdfLaTeXify` tasks.
Ideally, running `docBinary` would create `main.pdf` in the output directory.  Currently, however, you must run
`docBinaryLatexPdfLaTeXify`...  As I said...  a work in progress.

