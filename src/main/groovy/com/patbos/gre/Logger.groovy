package com.patbos.gre

import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.Ansi

class Logger {

    boolean verbose = false
    boolean debug = false
    boolean noColor = false

    Logger() {
        AnsiConsole.systemInstall();
    }

    def logStdOut(def user, host, line) {
        AnsiConsole.out.println("[$user@$host]  stdout: $line")
    }

    def logStdErr(def user, host, line) {
        println("[$user@$host]  stderr: $line")
    }

    def logCommand(def user, host, command) {
        println("[$user@$host] command: $command")
    }

    def logCommandStatus(def user, host, command, int status) {
        println("[$user@$host]  status: $command executed with status $status")
    }

    def logVerbose(def user, host, message) {
        if (verbose)
            println("[$user@$host] verbose: $message")
    }

    def logFromScript(def user, host, message) {
        println("[$user@$host] script: $message")
    }

    def logProgress(int percent, oldPercent) {
        if (!noColor) {
            if (percent != oldPercent) {
                //println("$percent %")
                Ansi an = Ansi.ansi().cursorLeft(4);
                if (percent < 10) {
                    an = an.a(" ")
                }
                an = an.a(percent).a(" ").a("%")

                AnsiConsole.out().print(an)
                AnsiConsole.out().flush()
                if (percent == 100) {
                    AnsiConsole.out().println()
                }
            }
        }
        return percent
    }

    def logDebug(def user, host, message) {
        if (debug)
            println("[$user@$host]  debug: $message")
    }

    def logDebug(def message) {
        if (debug)
            println("debug: $message")
    }

    def logError(def user, host, message) {
        println("[$user@$host]   error: $message")
    }


}
