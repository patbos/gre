package com.patbos.gre

class GreCategory {

    def static exec(Script script, GString command) {
        exec(script, command.toString())
    }

    def static exec(Script script, String command) {
        def greRuntime = script.binding.getVariable("gre")
        greRuntime.exec(command)
    }

    def static exec(Script script, String command, boolean throwException) {
        def greRuntime = script.binding.getVariable("gre")
        greRuntime.exec(command, throwException, true, false, null)
    }


    def static exec(Script script, String command, boolean throwException, boolean log) {
        def greRuntime = script.binding.getVariable("gre")
        greRuntime.exec(command, throwException, log, false, null)
    }

    def static sudoExec(Script script, String command, String password) {
        def greRuntime = script.binding.getVariable("gre")
        greRuntime.sudoExec(command, password)
    }


    def static put(Script script, File file, String destination) {
        def greRuntime = script.binding.getVariable("gre")
        greRuntime.put(file, destination)
    }

    def static println(Script script, Object output) {
        def greRuntime = script.binding.getVariable("gre")
        greRuntime.logScript(output)
    }

}
