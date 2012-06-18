package com.patbos.gre

import org.apache.commons.cli.Option

class Gre {

    def static void main(def args) {
        def cli = new CliBuilder(usage: "gre [options] scriptfile")
        cli.k(argName: 'key', longOpt: 'key', args:1, required: false, 'SSH key to use when connection')
        cli.H(argName: 'host', longOpt: 'host', args:1, required: false, 'host to execute commands on')
        cli.a(argName: 'argument', longOpt: 'argument', args: Option.UNLIMITED_VALUES, required: false, 'arguments passed to script')
        cli.p(argName: 'port', longOpt: 'port', args:1, required: false, 'port')
        cli.u(argName: 'user', longOpt: 'user', args:1, required: false, 'user')
        cli.v(argName: 'verbose', longOpt: 'verbose', 'Verbose mode')
        cli.h(argName: 'help', longOpt: 'help', required: false, 'display this help and exit')
        cli.version(argName: 'version', longOpt: 'version', required: false, 'display version and exit')

        def options = cli.parse(args)
        if (options) {
            def key
            def user
            def port
            def arguments
            if (options.h) {
                cli.usage()
                System.exit(0)
            }

            if (options.p) {
                //TODO fix validation
                port = Integer.parseInt(options.p)
            } else {
                port = 22;
            }

            if (options.version) {
                //TODO fix me
                def version = Gre.class.package.implementationVersion
                println("GRE version: $version")
                def java = System.getProperty("java.version")
                println("Java Version: $java")
                System.exit(0)
            }


            if (!options.H) {
                println("error: Missing required option: H")
                cli.usage()
                System.exit(1)
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


            def greRuntime = new GreRuntime()
            def error = true

            try {
                def binding = new Binding()
                binding.setVariable("gre", greRuntime)
                def shell = new GroovyShell(binding)
                Script script = shell.parse(scriptFile)
                greRuntime.init(options.H, port, user, key, options.v)
                use(GreCategory) {
                    script.run(scriptFile, arguments)
                }
                error = false
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

        } else {
            System.exit(1)
        }
    }
}
