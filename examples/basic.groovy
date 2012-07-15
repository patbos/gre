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
}