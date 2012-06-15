package com.patbos.gre

class Gre {

    def static void main(def args) {
        def cli = new CliBuilder(usage: "gre [options] commandfile")
        cli.c(argName: 'cert', longOpt: 'cert', args:1, required: false, 'cert')
        cli.H(argName: 'hosts', longOpt: 'hosts', args:1, required: true, 'hosts to execute commands on, comma seperated')
        cli.u(argName: 'user', longOpt: 'user', args:1, required: false, 'user')
        cli.v('Verbose mode')
        cli.h(argName: 'help', longOpt: 'help', required: false, 'display this help and exit')
        def options = cli.parse(args)
        if (options) {
            def cert
            def user

            if (options.arguments() == null || options.arguments().size() == 0 || options.arguments().size() > 1) {
                cli.usage()
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

            try {
                def binding = new Binding()
                binding.setVariable("gre", greRuntime)
                def shell = new GroovyShell(binding)
                Script script = shell.parse(new File(options.arguments().get(0)))
                greRuntime.init(options.H, user, cert, options.v)
                use(GreCategory) {
                    script.run()
                }

            } finally {
                greRuntime.close()
            }
        } else {
            cli.usage()
            System.exit(1)
        }
    }
}
