package com.patbos.gre

import org.apache.commons.cli.Option
import org.fusesource.jansi.AnsiConsole
import org.codehaus.groovy.control.CompilationFailedException

class Gre {

    def static void main(def args) {
        def cli = new CliBuilder(usage: "gre [options] commandscript")
        cli.k(argName: 'key', longOpt: 'key', args: 1, required: false, 'SSH key to use when connection')
        cli.H(argName: 'host', longOpt: 'host', args: Option.UNLIMITED_VALUES, valueSeparator: ',' as char, required: false, 'hosts to execute commands on')
        cli.a(argName: 'arg', longOpt: 'arg', args: 1, required: false, 'arguments passed to scripts')
        cli.p(argName: 'port', longOpt: 'port', args: 1, required: false, 'port SSH port to use when connection to hosts')
        cli.u(argName: 'user', longOpt: 'user', args: 1, required: false, 'user for logging in to hosts')
        cli.pw(argName: 'password', longOpt: 'user', required: false, 'password prompt for password')
        cli.v(argName: 'verbose', longOpt: 'verbose', 'Verbose mode')
        cli.h(argName: 'help', longOpt: 'help', required: false, 'display this help and exit')
        cli.version(argName: 'version', longOpt: 'version', required: false, 'display version and exit')
        cli.post(argName: 'postscript', longOpt: 'postscript', args: 1, required: false, 'postscript to be executed when all hosts has executed script')
        cli.pre(argName: 'prescript', longOpt: 'prescript', args: 1, required: false, 'prescript to be executed first to return a list of server to execute command script')
        cli.hostfile(argName: 'hostfile', longOpt: 'hostfile', args: 1, required: false, 'hostfile file containing a hostname on each row')
        cli.d(argName: 'debug', longOpt: 'debug', required: false, 'Produce execution debug output')
        cli.nc(argName: 'nc', longOpt: 'no-color', required: false, 'Do not use color in the console output.')
        cli.T(argName: 'timeout', longOpt: 'timeout', args: 1, required: false, 'Connection timeout in seconds')

        def log = new Logger();

        def options = cli.parse(args)
        if (options) {
            def key
            def user
            def port
            def arguments
            def password
            def postScriptFile = null
            int timeout = 20


            if (options.v) {
                log.verbose = true
            }

            if (options.d) {
                log.debug = true
            }

            if (options.T) {
                try {
                    timeout = Integer.parseInt(options.T)
                    log.logDebug("Setting timeout to $timeout seconds")
                } catch (NumberFormatException e) {
                    println("error: Could not understand option: T value $options.T")
                    cli.usage()
                    System.exit(1)
                }
            }


            if (options.nc) {
                log.noColor = true
            } else {
                AnsiConsole.systemInstall();
            }

            if (options.h) {
                cli.usage()
                System.exit(0)
            }

            if (options.post) {
                postScriptFile = new File(options.post)
                if (!postScriptFile.exists()) {
                    println("Could not find post script file: $postScriptFile")
                    System.exit(1)
                }
            }

            if (options.p) {
                try {
                    port = Integer.parseInt(options.p)
                } catch (NumberFormatException e) {
                    println("error: Could not understand option: p value $options.p")
                    cli.usage()
                    System.exit(1)
                }
            } else {
                port = 22;
            }

            if (options.version) {
                def version = Gre.class.package.implementationVersion
                println("GRE version: $version")
                def java = System.getProperty("java.version")
                println("Java Version: $java")
                System.exit(0)
            }

            if (options.arguments() == null || options.arguments().size() == 0 || options.arguments().size() > 1) {
                println("error: Missing argument scriptfile")
                cli.usage()
                System.exit(1)
            }

            def scriptFile = new File(options.arguments().get(0))
            if (!scriptFile.exists() || !scriptFile.isFile()) {
                println("Could not find script file: $scriptFile")
                System.exit(1)
            }

            if (options.k) {
                key = options.k
            } else {
                key = System.getProperty("user.home") + "/.ssh/id_rsa"
            }

            if (!new File(key).exists()) {
                println("Could not find key file: $key")
                System.exit(1)
            }

            if (options.a) {
                arguments = String.valueOf(options.a).split(" ")
            }

            if (options.u) {
                user = options.u
            } else {
                user = System.getProperty("user.name")
            }

            if (options.pw) {
                password = new String(System.console().readPassword("%s", "Password:") as char[])
            }

            if (!options.H && !options.hostfile && !options.pre) {
                println("error: Missing required option: H, hostfile or pre")
                cli.usage()
                System.exit(1)
            }


            def hosts

            if (options.hostfile) {
                def hostfile = new File(options.hostfile)
                if (!hostfile.exists()) {
                    println("error: Could not read hostfile: $hostfile")
                    System.exit(1)
                }

                hosts = hostfile.readLines();
            }
            if (options.H) {
                hosts = options.Hs
            }

            if (options.pre) {
                def preScriptFile = new File(options.pre)
                if (!preScriptFile.exists()) {
                    println("error: Could not read prescript: $preScriptFile")
                    System.exit(1)
                }
                Binding binding = new Binding()
                binding.setProperty("args", arguments)
                def shell = new GroovyShell(binding)
                def script = shell.parse(preScriptFile)
                hosts = script.run()
            }



            def result = new HashMap<String, Map>()

            hosts.each { hostname ->
                def greRuntime = new GreRuntime()
                def error = true
                def scriptClass = null

                try {
                    def hostResult = new HashMap()
                    def binding = new Binding()
                    binding.setVariable("gre", greRuntime)
                    binding.setVariable("greResult", hostResult)
                    def shell = new GroovyShell(binding)
                    Script script
                    try {
                        log.logDebug("About to parse script")
                        script = shell.parse(scriptFile)
                        log.logDebug("Script parsed")
                    } catch (IOException e) {
                        log.logError(user, hostname, "Error reading $scriptFile")
                        System.exit(1)
                    } catch (CompilationFailedException e) {
                        log.logError(user, hostname, "Complilation failed $e")
                        System.exit(1)
                    }
                    scriptClass = script.class.name
                    log.logDebug(user, hostname, "Script class is $scriptClass")
                    greRuntime.init(log, hostname, port, user, key, password, timeout)
                    use(GreCategory) {
                        try {
                            script.run(scriptFile, arguments)
                        } catch (ExecutionException e) {
                            throw e;
                        } catch (Exception e) {
                            def element = getLocation(log, e, scriptClass)
                            if (element) {
                                log.logError(user, hostname, "Error executing script $scriptFile at line $element.lineNumber:  $e.message")
                            } else {
                                log.logError(user, hostname, "Error executing script: $e.message")
                            }
                            error = true
                        }
                    }
                    error = false
                    result.put(hostname, hostResult)
                } catch (UnknownHostException e) {
                    log.logError(user, hostname, "Unknown host: $options.H")
                } catch (MissingPropertyException e) {
                    log.logError(user, hostname, "Script error: $e.message")
                } catch (MissingMethodException e) {
                    log.logError(user, hostname, "Script error: $e.message")
                } catch (AuthenticationException e) {
                    log.logError(user, hostname, "Authentication failed")
                } catch (ExecutionException e) {
                    def element = getLocation(log, e, scriptClass)
                    if (element) {
                        log.logError(user, hostname, "Error executing script $scriptFile at line $element.lineNumber:  $e.message")
                    } else {
                        log.logError(user, hostname, "Error executing script: $e.message")
                    }
                } catch (Exception e) {
                    log.logError(user, hostname, "Unknown Error")
                    e.printStackTrace()
                } finally {
                    greRuntime.close()
                }
                if (error) {
                    System.exit(1)
                }
            }
            if (postScriptFile) {
                def binding = new Binding()
                binding.setVariable("greResult", result)
                def shell = new GroovyShell(binding)
                Script script = shell.parse(postScriptFile)
                script.run()
            }
        } else {
            System.exit(1)
        }
    }


    def static getLocation(def log, Throwable e, def className) {
        e.stackTrace.find { StackTraceElement element ->
            element.className.equals(className)
        }
    }
}
