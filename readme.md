# Installation

The compilation of analyzer requires a JDK 11 and Eclipse (tested with 2019-12).

Install Ivy Eclipse plugin (Help -> Install new software and then add http://www.apache.org/dist/ant/ivyde/updatesite, select everything and install) and ANTLR (required to compile and execute project java-antlr-parser, Help -> Eclipse Marketplace and then install ANTLR 4 IDE).

Import the projects in Eclipse, for each ivy.xml file in the main directory of a project: right click on the file -> Add ivy library and then Finish). If Eclipse does not find some library, try to right click on ivy.xml, then Ivy -> Resolve.

# Running the analysis

Class JavaRunner in project java-parser runs the analysis on snippets of Java code. It is needed to import and compile projects core and java-parser in order to run the analysis.

The program under analysis has to be a snippet of Java code between curly brackets.

```java
{
	int i;
	String s;
	i = 1;
	while( i < 100)
		i = i * 2;
	if(i>0)
		return i;
	else return 2;
}
```

The parser does not support exceptions, objects, and method calls.

There are two distinct ways to run the analysis.

##JavaCLI

JavaCLI allows the user to specify the different parameters of the analysis (source code file, abstract domain, checker) from the command line.

```
usage: JavaCLI
 -a,--xmloptions <analysis options>   Output xml file of the analysis options
 -c,--checker <checker>               Property checked after the analysis on the abstract results
 -cfg,--controlflowgraph <cfg file>   Control flow graph output dot file
 -d,--domain <abstract domain>        Abstract domain for the analysis
 -h,--help                            Print this help
 -i,--input <input file>              Input file
 -o,--output <output file>            Output dot file containing detailed abstract analysis results
 -r,--xmlresults <analysis results>   Output xml file of the analysis results
```

Parameters -c, -d, and -i are mandatory.

At the end of the analysis, JavaCLI produces a set of warnings that are printed in the console.

##JavaXmlExecutor

JavaXmlExecutor executes the analysis with the options specified in a xml file containing the state of an AnalysisOptions instance.

```
usage: JavaXmlExecutor
 -h,--help                            Print this help
 -s,--spec <xml specification file>   XML file containing the of the analysis
```

All the options of the analysis have to be contained in the given xml file.

#AnalysisOptions

Class it.unive.dais.staticanalyzer.api.AnalysisOptions allows the user to specify all the options of the analysis in a class or xml file instead of passing them through JuliaCLI.

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<analysisOptions checker="AssertChecker" domain="Apron:Box" input="code.java" xmlanalysisresultfile="results.xml"/>
```
For instance, the xml file above specifies an analysis of file code.java with the Interval domain (Apron:Box), applying checker AssertChecker, and to dump the analysis results to results.xml.


#AnalysisResult

Class it.unive.dais.staticanalyzer.api.AnalysisResult stores the options of the analysis as well as the results (that is, a set of warning). Like class AnalysisOptions, this can be represented as an instance of the class or an xml file.

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<analysisResult>
    <options cfg="./results/cfg.dot" checker="AssertChecker" domain="Apron:Box" input="core.java""/>
    <warnings>
        <warning column="83" line="8">
            <message>This assert statement might not hold</message>
        </warning>
    </warnings>
</analysisResult>
```
For instance, the xml file above reports the results of an analysis with the options described in the previous section (Interval domain, AssertChecker, and code of file code.java), and that produced a warning at line 8 column 83.

#Apron

The implementation contains class Apron. This relies on Apron v. 0.9.12. The Java interface consists of 2 jar files (in core\lib), and it needs to access the binaries of Apron. Therefore it is necessary to compile Apron (see https://github.com/antoinemine/apron) on the machine one wants to run the analysis, and pass the path to the installation directory (e.g., /usr/local/lib) through the environment variable LD_LIBRARY_PATH. 