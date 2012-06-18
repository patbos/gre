package com.patbos.gre

class Logger {

    def logStdOut(def host, line) {
        println("$host stdout: $line")
    }

    def logStdErr(def host, line) {
        println("$host stderr: $line")
    }

    def logCommand(def host, command) {
        println("$host command: $command")
    }

    def logCommandStatus(def host, command, int status) {
        println("$host command: $command executed with status $status")
    }

    def logVerbose(def host, message) {
        println("$host verbose: $message")
    }

    def logFromScript(def host, message) {
        println("$host script: $message")
    }

}
