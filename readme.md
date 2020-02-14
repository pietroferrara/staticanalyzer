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

The parameters of JavaRunner are the following ones:

```
 -cfg,--controlflowgraph <cfg file>   Control flow graph output dot file
 -d,--domain <abstract domain>        Abstract domain for the analysis
 -h,--domain                          Print this help
 -i,--input <input file>              Input file
 -o,--output <output file>            Output dot file containing analysis esults
```

At the end of the analysis, JavaRunner produces a dot file (specified by option -o) with the control flow graph of the analyzed program as well as entry and exit states attached at each program point. In addition, it dumps the control flow graph of the program if option -cfg is specified.

#Apron

The implementation contains class Apron. This relies on Apron v. 0.9.12. The Java interface consists of 2 jar files (in core\lib), and it needs to access the binaries of Apron. Therefore it is necessary to compile Apron (see https://github.com/antoinemine/apron) on the machine one wants to run the analysis, and pass the path to the installation directory (e.g., /usr/local/lib) through the environment variable LD_LIBRARY_PATH. 