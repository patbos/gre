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

    def logStdOut(def host, line) {
        AnsiConsole.out.println("$host stdout: $line")
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
        if (verbose)
            println("$host verbose: $message")
    }

    def logFromScript(def host, message) {
        println("$host script: $message")
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

}
