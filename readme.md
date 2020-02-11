# Installation

The compilation of analyzer requires a JDK 11 and Eclipse (tested with 2019-12).

Install Ivy Eclipse plugin (Help -> Install new software and then add https://ant.apache.org/ivy/ivyde/download.cgi, select everything and install) and ANTLR (required to compile and execute project java-antlr-parser, Help -> Eclipse Marketplace and then install ANTLR 4 IDE).

Import the projects in Eclipse, for each ivy.xml file in the main directory of a project: right click on the file -> Add ivy library and then Finish). If Eclipse does not find some library, try to right click on ivy.xml, then Ivy -> Resolve.