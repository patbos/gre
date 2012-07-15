def execute() {
    exec("cat /proc/cpuinfo |grep processor|wc -l")
}