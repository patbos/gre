GRE
===
Groovy Remote Execution executes commands to remote servers using SSH

Introduction
===

Sample scriptfile

script.groovy
```groovy
exec("ls -la")
```

    gre -H localhost -p 22 script.groovy


Build
===
    gradle jar