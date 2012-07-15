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
@Grab(group='org.yaml', module='snakeyaml', version='1.10')
import org.yaml.snakeyaml.*

def execute() {
    Yaml yaml = new Yaml();
    def map = yaml.load(exec("facter --yaml").stdOut)
    greResult.put("swapsize", map.get("swapsize"));
    greResult.put("uptime", map.get("uptime_days"));

}

def post() {
    greResult.each { key, value ->
        println("$key $value")

    }
}```

    gre -H localhost -p 22 script.groovy


Build
===
    gradle jar