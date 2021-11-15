# Description
The program is using an AIXM 5.1 BasicMessage comprising of BASELINEs as input to generate an output file, again an AIXM 5.1 BasicMessage, comprising of all the input BASELINEs with an increased correction number and an end of validity equal to the new effective date. In addition, all the input BASELINEs will be duplicated with an increased sequence number, the new effective date as validTime begin position and an optional annotation.

# Usage
```bash
aixm-update-gen [OPTIONS] <EFFECTIVE-DATE> <INPUT-FILE> <OUTPUT-FILE>
```
```
Options:
  -r, --remark TEXT      This text will be placed in the annotation element.
  -c, --omit-correction  This instructs the program to not create the
                         correction timeslices.
  -h, --help             Show this message and exit

Arguments:
  <EFFECTIVE-DATE>  The new effective date, e.g. "2022-12-24T00:00:00Z".
  <INPUT-FILE>      An AIXM 5.1 Basic Message file as input.
  <OUTPUT-FILE>     The output file.
```
Example:
```bash
aixm-update-gen --remark "new slice" "2022-12-24T00:00:00Z" input.xml output.xml
```

# System Requirements
Any operating system which is capable to run the Java Runtime Environment is supported.

JRE version 8 is required to execute this program.

Please note that it must either be the java command in the PATH variable or the environment variable JAVA_HOME must point to the JRE installation directory.