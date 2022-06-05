# Gitlet Design Document
author:

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

###Blob (store in directory) *immutable
Instance variables:
* String contents - Contains contents of file
* hashcode - SHA of file

Methods
* hash() - returns SHA-1 hash of file contents
###Commit (store in directory) *immutable
Constructor
* take as input, message, timestamp and assign instance variables
* Creates a new Treemap. Goes through staging area Treemap and add all hashes/blobs into the treemap specific to commit

Instance variables
* String message - metadata message
* String timestamp - time when commit was created
* treemap - mapping file names(keys) of SHA of file contents(value)
* parent - hashcode of parent commit

Methods
* hash() - returns SHA incorporating, message, timestamp, name, blob in TreeMap

###Staging Area
Instance variables:
toAdd - Treemap containing name-hash entries for each blob to be added to the staging area, and will be used to commit
toRemove - Treemap containing blobs to untrack in the next commit

###Main
Main driver (elaborated in algorithms)

###Repo
init()
* creates init commit (as defined on spec)
* set up directory structure

add()
* initialize file in Blobs folder
* update staging area to include new blobs

commit()
* create new commit and add files of staging area to commit
* add to treemap
* clear staging area
* update branches new head commit, link to parent commit

## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

###Main
* read through input for keywords to decide which method in repo to call
* if add -> call repo add class
* if commit -> call repo commit class


## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

###Blobs folder
* NAME OF EACH FILE: SHA of file 
* CONTENT OF EACH FILE: contents of blob

### Commits folder
* NAME OF EACH FILE: SHA of commit
* CONTENTS OF EACH FILE: serialized commit object (created by Utils)

###Staging Area (serialized tree map) file
* maps key (string) --> value (string)
  * name of file --> SHA1 of file

###Branches (serialized tree map) file
* maps key (string) --> value(string)
  * name of branch --> SHA1 of most recent commit
* current branch (file)
  * contents: name of current branch 

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

![](Gitlet -1.png)