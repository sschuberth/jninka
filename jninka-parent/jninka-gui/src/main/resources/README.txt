JNinka
======

Source code license identification tool


Introduction
------------

JNinka is license identification tool that identifies the license(s)
under which a source file is made available.

This tool is based on the Ninka project, Authored by Daniel M. German
and Yuki Manabe

Details at:
<http://ninka.turingmachine.org/>


License
-------
<pre>
JNinka is licensed under the Affero GPL v3+.
 
Copyright (C) 2012  White Source ltd.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses<a/>.
</pre>

Requirements
------------

JRE >= 1.6


Usage
-----
* Command line: 
```
java -jar jninka.jar <source-folder> <output-file>
```

  **For example**: 
```
java -jar jninka.jar ~/my-project/src ~/my-project/scan.xml
```	

* UI wrapper application:

	Execute jninka-gui-x.jar by double click or java -jar
	
Documentation
-------------
Project documentation can be found on <http://docs.whitesourcesoftware.com/display/docs/JNinka/>
Project information can be found on <http://whitesource.github.com/jninka/>
