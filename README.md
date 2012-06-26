GRE
===
Groovy Remote Execution executes commands to remote servers using SSH
GRE uses default SSH with authentication key, but passwords are supported if same password can be used on all servers. 

Introduction
===
Make sure that it is possible to login to remote server via ssh without giving password.
Otherwise do this:
    ssh-keygen -t rsa
to copy the keys to server
    ssh-copy-id -i ~/.ssh/id_rsa.pub user@machinename-or-ipaddress

script.groovy
```groovy
exec("ls -la")
```

    gre -H localhost -p 22 script.groovy


Build
===
    gradle jar