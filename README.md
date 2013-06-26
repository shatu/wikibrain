wikAPIdia
=====
An API for accessing and organizing Wikipedia's language dumps. Specifically we:
* Support multiple languages in parallel.
* Resolve all redirected links to the page they point to.
* Document all links out from a given Wikipedia page.
* Document all of the members of a specific Wikipedia category.
* Overlay concepts that are cross-lingual and links between concepts.

This API is meant to be used for anyone wishing to manipulate Wikipedia's vast information.

Software and Hardware Recommendations
-----------
* Maven (required)
* Bash (required)
* One of the following <ul> <li>md5sum</li> <li>md5</li> <li>sum</li></ul>
* Hardware stuff (to be filled out)

User Instructions
-----------
* Run the download script.
* Run the dumploader.sh file. The parameters should be formatted to match the following:

```bash
[-c .conf] [-it] [file ...]
```
```bash
-c     Sets configuration file. Include that file .conf
-i     Creates all indexes after loading
-t     Drops and recreates all tables
```
	
* Run the redirectloader.sh file. The pareameters should be formatted to match the following:

```bash
[-c .conf] [-it] [-l lang ...]
```
```bash
-c     Sets configuration file. Include that file .conf
-i     Creates all indexes after loading
-l     Set language for redirects to be resolved in. Enter as lang codes with spaces between them
-t     Drops and recreates all tables
```
 	
* Run the wikitextdumploader.sh file. The pareameters should be formatted to match the following:

```bash
[-c .conf] [-it] [-l lang ...]
```
```bash
-c     Sets configuration file. Include that file .conf
-i     Creates all indexes after loading
-l     Set languages to be processed. Also sets ills (Inter Language Links). Enter as lang codes with spaces between them.
-t     Drops and recreates all tables
```
* Run conceptmapper.sh file. The parameters should be formatted to match the following:

```bash
[-c .conf] [-it] [-l lang ...] [-n algorithm]
```
```bash
-c	Sets configuration file. Include that file .conf
-i	reates all indexes after loading
-l	Set languages to be processed. Enter as lang codes with spaces between them
-t	Drops and recreates all tables
-n	Selects the algorithm to use to map concepts
```

* Run universallinkloader.sh. The parameters should be formatted to match the following:

```bash
[-c .conf] [-it] [-l lang ...] [-n algorithm]
```
```bash
-c	Sets configuration file. Include that file .conf
-i	reates all indexes after loading
-l	Set languages to be processed. Enter as lang codes with spaces between them
-t	Drops and recreates all tables
-n	Selects the algorithm to use to map concepts
```
 
A Basic Outline of the Process 
-----------
* Download scripts...
* Load the Dump as XML <ul><li>Convert Dump into RawPages </li> <li>Convert RawPages into LocalPages <ul>
					<li>Mark Redirects to be dealt with after this process</li> </ul></li>
			</ul></li>
* Resolve Redirects <ul><li>Load into Redirect Table, fully resolved </li></ul></li>
* WikiTextParser does the following <ul><li>load links into table with src/dest IDs </li> <li>load categories with the source article as a category member</li></ul></li>
* Load Concepts
* Load Concept Links
</ol>
