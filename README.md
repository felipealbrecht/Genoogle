Genoogle
========

Fundamental Information
-----------------------
Genoogle is software for similar DNA sequences searching developed by Felipe Albrecht: [home page](http://www.pih.bio.br), [ email contact](felipe.albrecht@gmail.com).

Genoogle uses indexing and parallel processing techniques and it is developed in Java.
Genoogle is free and open source.
The name Genoogle comes from Genes + Google, the final world domination plan is to develop a software to locate genes likes Google is to locate information in the Web and Genoogle does *not* have *any* affiliation with Google Inc and I hope its name will not cause problem.

It is a Beta Version of Genoogle, it means: it lacks some features and have some know and a lot of unknow bugs. 
So, I hope that the users (YOU!) will inform me about bugs and features which you will like to have.

If you really want to develop something in to Genoogle, contact me.

Features
--------

### Actual Features:
 * Fast similar sequences searching.
 * Really good sensibility.
 * Text mode interfaces.
 * Web Services Interface.
 * Very Simple Web interface, but support for JSP.
 * Good memory requirements. (For a 4 gigabytes data bank, it is necessary not more than 4 gigabytes of RAM memory).
 * Working (and tested) at Windows and Linux.
 * Data banks with more than 8 gigabytes. 
 * Console and batch interfaces.

### Missing and planned features:
 * Better web interface.
 * RNA indexing and searching sequences.

### Missing and not (for so soon) planned features:
 * Proteins indexing and searching. (It will be a big work to implement it, but it is possible)
 * Clusters implementation. (May be my Ph.D. project)

Intallation
-----------

### Requirements
To run Genoogle it is necessary:
 * JRE >= 1.6 and the environment variable JAVA_HOME should inform where the JRE is, by example:  ```JAVA_HOME="/usr/lib/jvm/java-6-sun"```
 * Ram Memory: The Genoogle memory requirement is approximately 80% of the data bank size more approximately 40Mbs for Java run time.

### Installation process
 * Download the package here (TBA)
 * Unpack
 * Copy the fasta files data banks into the fasta/files folder.
 * Configure the conf/genoogle.xml file and insert the copied files at the `<genoogle:split-databanks>` section as new `<genoogle:databank>`:
 * ```xml
<genoogle:split-databanks name="RefSeq" path="files/fasta" mask="111010010100110111" number-of-sub-databanks="1" sub-sequence-length="11">
       <genoogle:databank name="Cow"       path="cow.rna.fna"       />
       <genoogle:databank name="Frog"      path="frog.rna.fna"      />
       <!-- ... more files -->
     </genoogle:split-databanks>
     ```
 * Run the format_db.sh script.
 * Wait while the data bank is formatted and the inverted index processed.
 * Execute :
    * run_web.sh, for webservices, web page and col), 
    * or run_standalone_web.sh, for web page that will access Genoogle by webservice,
    * or run_console.sh, for console only interface.
 * Have fun!

Searching
---------
Genoogle has two interfaces: a **very** simple web page, text mode console, and WebServices interface.

To do the search using the web page is very simples. Open the address *localhost:8080* at your browser and put the query sequence in the input box and click *Search sequence* button. Wait and the results will be shown. The console interface is much better!

To use the WebServices, please check their wiki.

### Console interface

The console interface has the following commands:
 * search <data bank> <input file> <output file> <parameters>: does the search.
 * list : lists the data banks.
 * parameters : shows the search parameters and their values.
 * set <parameter>=<value> : set the parameter value.
 * gc : executes the java garbage collection.
 * prev or l : executes the last command.
 * batch <batch file> : runs the commands listed in this batch file.
 * exit : finish Genoogle execution.

The search parameters are:
 * MaxSubSequenceDistance : maximum index entries distance to be considered in the same HSPs.
 * SequencesExtendDropoff : drop off for sequence extension.
 * MaxHitsResults : maximum quantity of returned results.
 * QuerySplitQuantity : how many slices the input query will be divided. 
 * MinQuerySliceLength : minimum size of each input query slice.
 * MaxThreadsIndexSearch : quantity of threads which will be used to index search. ( Should be MaxThreadsIndexSearch <= QuerySplitQuantity * 2).
 * MaxThreadsExtendAlign : quantity of threads which will be used to extend and align the HSPs.
 * MatchScore : score when has a match at the alignment.
 * MismatchScore : score when has a mismatch at the alignment.
 
An example of search is shown bellow: 

```search Genomes_RefSeq BA000002 result_file QuerySplitQuantity=2 MaxThreadsIndexSearch=2 MaxHitsResults=20```

This search, make a search at the Genomes_RefSeq databank, using as input the file BA000002 and the results will be saved at "result_file.xml" file.
The input query will be split in to 2 parts and will be used 2 threads to do the search of the input query sub-sequences at the inverted index.
At the end will be returned to the user, the 20 better scores.


-----


Dependencies
------------

Genoogle uses:
 * [Dom4j](http://www.dom4j.org/) for XML parsing.
 * [JUnit4](http://www.junit.org/) for unit testing.
 * [Easy Mock](http://easymock.org/) and [CgLib](http://cglib.sourceforge.net/) for Mock creation at the JUnits.
 * [Google Collections](http://code.google.com/p/google-collections/)
 * [Protocol Buffers](http://code.google.com/intl/pt-BR/apis/protocolbuffers/) for Data bank and index serialization.
 * [Log4J](http://logging.apache.org/log4j/) for loggin.
 * [Jetty](http://www.mortbay.org/) for embedded web server.
 * [Jax-WS](https://jax-ws.dev.java.net/) for WebServices implementation.

All these libraries are in the directory [https://github.com/felipealbrecht/Genoogle/tree/master/lib](https://github.com/felipealbrecht/Genoogle/tree/master/lib)


