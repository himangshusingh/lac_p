

# Luc_P
```It is a software that searches and indexed a given set of input files```

```It is based on Lucene API```

### Prerequisites
```
Java 11 or higher 
```


### About

- this program searches for a word or a phrase in the given input files
- it uses the LUCENE API version 9.7 for most of its part
- it first creates a directory called `indexedFiles`
- then it searches that index to find the given pattern

### Run the Program

- Download the ```jar``` package from the [release](https://github.com/reyan1357/luc_p/releases/tag/beta) section
- Keep the files to be searches in a folder and note the path of the file
- Follow the given commands to execute the program 

### Commands

- to know all the possible commands

```
java -jar .\luc_p.jar --help
```

- To create __Index Files__

``` 
java -jar .\luc_p.jar --write <Path of the inputfiles>
```
Example:
```
java -jar .\luc_p.jar --write D:\files\inputFiles
```

- To search from an existing indexed file

``` 
java -jar .\luc_p.jar --search <search word> <Path of the indexed Files>
```
Example:
```
java -jar .\luc_p.jar --search Milk D:\files\inputFiles\indexedFiles
```
- This is display all the matches found for the word with respect to the search word `Milk`
- It will display all the names of files where the word has occured


Note:
```
When the files are indexed using this software it will print the path to the folder where indexed files are present
Copy that path and paste it while it while searching 
```

