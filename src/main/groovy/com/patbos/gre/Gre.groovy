package com.patbos.gre

import org.apache.commons.cli.Option
import org.fusesource.jansi.AnsiConsole

class Gre {

    def static void main(def args) {
        AnsiConsole.systemInstall();
        def cli = new CliBuilder(usage: "gre [options] scriptfile")
        cli.k(argName: 'key', longOpt: 'key', args:1, required: false, 'SSH key to use when connection')
        cli.H(argName: 'host', longOpt: 'host', args:Option.UNLIMITED_VALUES, valueSeparator: ',' as char, required: false, 'hosts to execute commands on')
        cli.a(argName: 'arg', longOpt: 'arg', args: 1, required: false, 'arguments passed to script')
        cli.p(argName: 'port', longOpt: 'port', args:1, required: false, 'port')
        cli.u(argName: 'user', longOpt: 'user', args:1, required: false, 'user')
        cli.pw(argName: 'password', longOpt: 'user', required: false, 'password')
        cli.v(argName: 'verbose', longOpt: 'verbose', 'Verbose mode')
        cli.vv(argName: 'veryverbose', longOpt: 'veryverbose', 'Very verbose mode')
        cli.h(argName: 'help', longOpt: 'help', required: false, 'display this help and exit')
        cli.version(argName: 'version', longOpt: 'version', required: false, 'display version and exit')
        cli.post(argName: 'postscript', longOpt: 'postscript', args:1, required: false, 'postscript')
        cli.hostfile(argName: 'hostfile', longOpt: 'hostfile', args:1, required: false, 'hostfile')
        cli.d(argName: 'debug', longOpt: 'debug', required: false, 'Produce execution debug output')
        cli.nc(argName: 'nc', longOpt: 'no-color', required: false, 'Do not use color in the console output.')

        def log = new Logger();

        def options = cli.parse(args)
        if (options) {
            def key
            def user
            def port
            def arguments
            def password
            def postScriptFile = null

            if (options.v) {
                log.verbose = true
            }

            if (options.d) {
                log.debug = true
            }

            if (options.nc) {
                log.noColor = true
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

            if (!options.H && !options.hostfile) {
                println("error: Missing required option: H or hostfile")
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
            } else {
                hosts = options.Hs
            }


            def result = new HashMap<String, Map>()

            hosts.each { hostname ->
                def greRuntime = new GreRuntime()
                def error = true

                try {
                    def hostResult = new HashMap()
                    def binding = new Binding()
                    binding.setVariable("gre", greRuntime)
                    binding.setVariable("greResult", hostResult)
                    def shell = new GroovyShell(binding)
                    Script script = shell.parse(scriptFile)
                    greRuntime.init(log, hostname, port, user, key,password)
                    use(GreCategory) {
                        script.run(scriptFile, arguments)
                    }
                    error = false
                    result.put(hostname, hostResult)
                } catch (UnknownHostException e) {
                    println("Unknown host: $options.H")
                } catch (MissingPropertyException e) {
                    println("Script error: $e.message")
                } catch (MissingMethodException e) {
                    println("Script error: $e.message")
                } catch (AuthenticationException e) {
                    println("Authentication failed")
                } catch (ExecutionException e) {
                    println("Error executing script: $e.message")
                } catch (Exception e) {
                    println("Unknown Error")
                    e.printStackTrace()
                } finally {
                    greRuntime.close()
                }
                if (error){
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
}
