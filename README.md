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

Also available at:
<https://github.com/dmgerman/ninka/>

Downloads
---------
You can download JNinka from the [online documentation](http://docs.whitesourcesoftware.com/display/docs/JNinka).


License
-------
The project is licensed under the Apache 2.0 license.
<pre>
Copyright (C) 2014 WhiteSource Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
