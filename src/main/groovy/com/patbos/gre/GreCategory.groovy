package com.patbos.gre

class GreCategory {

    def static exec(Script script, GString command) {
        exec(script, command.toString())
    }

    def static exec(Script script, String command) {
        def greRuntime = script.binding.getVariable("gre")
        greRuntime.exec(command)
    }


}
