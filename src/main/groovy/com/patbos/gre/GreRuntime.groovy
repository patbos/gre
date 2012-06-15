package com.patbos.gre

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.ChannelExec

class GreRuntime {


    JSch ssh
    Session session
    def host
    def verbose

    def init(def host, username, cert, verbose) {
        this.host = host
        this.verbose = verbose
        ssh = new JSch()

        session = ssh.getSession(username, host)
        ssh.addIdentity(cert)

        ssh.setHostKeyRepository(new NullHostKeyRepository())
        if (verbose) {
            println("Connecting to $username@$host with cert $cert")
        }

        session.connect()
        if (verbose) {
            println("Connected")
        }


    }

    def exec(String command) {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec")
        channelExec.setCommand(command)
        InputStream input = channelExec.getInputStream()
        channelExec.setErrStream(System.err)

        channelExec.connect()

        try {
            byte[] tmp = new byte[1024]
            while (true) {
                while (input.available() > 0) {
                    int i = input.read(tmp, 0, 1024)
                    if (i < 0)
                        break
                    System.out.print(new String(tmp, 0, i))
                }
                if (channelExec.isClosed()) {
                    if (verbose)
                        System.out.println("exit-status: " + channelExec.getExitStatus())
                    break
                }
                try {
                    Thread.sleep(100)
                } catch (Exception ee) {

                }
            }
        } finally {
            channelExec.disconnect()
        }
    }

    def close() {
        if (verbose) {
            println("Disconnecting")
        }
        if (session)
            session.disconnect()
        if (verbose) {
            println("Disconnected")
        }
    }

}
