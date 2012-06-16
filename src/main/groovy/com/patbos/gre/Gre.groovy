package com.patbos.gre

class Gre {

    def static void main(def args) {
        def cli = new CliBuilder(usage: "gre [options] scriptfile")
        cli.c(argName: 'cert', longOpt: 'cert', args:1, required: false, 'cert')
        cli.H(argName: 'hosts', longOpt: 'hosts', args:1, required: false, 'hosts to execute commands on, comma seperated')
        cli.u(argName: 'user', longOpt: 'user', args:1, required: false, 'user')
        cli.v('Verbose mode')
        cli.h(argName: 'help', longOpt: 'help', required: false, 'display this help and exit')
        cli.version(argName: 'version', longOpt: 'version', required: false, 'display version and exit')
        def options = cli.parse(args)
        if (options) {
            def cert
            def user
            if (options.h) {
                cli.usage()
                System.exit(0)
            }

            if (options.version) {
                def version = Gre.class.package.implementationVersion
                println("Gre version $version")
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

            if (options.c) {
                cert = options.c
            } else {
                cert = System.getProperty("user.home") + "/.ssh/id_rsa"
            }

            if (!new File(cert).exists()) {
                println("Could not find cert file: $cert")
                System.exit(1)
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
                greRuntime.init(options.H, user, cert, options.v)
                use(GreCategory) {
                    script.run()
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
