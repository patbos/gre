package com.patbos.gre

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException

class GreRuntime {


    def ssh
    def session
    def host
    def user
    def verbose
    def log = new Logger()

    def init(def host, port, username, key, verbose) {
        this.host = host
        this.verbose = verbose
        user = username
        ssh = new JSch()

        session = ssh.getSession(username, host, port)
        ssh.addIdentity(key)

        ssh.setHostKeyRepository(new NullHostKeyRepository())
        if (verbose) {
            log.logVerbose(host, "Connecting to $username@$host with key $key")
        }

        try {
            session.connect(2000)
        } catch (JSchException e) {
            if (e.cause instanceof UnknownHostException) {
                throw e.cause
            }

            if (e.message.startsWith("java.net.UnknownHostException")) {
                throw new UnknownHostException("$host")
            }
            if (e.message.startsWith("Auth fail")) {
                throw new AuthenticationException()
            }

            throw e
        }
        if (verbose) {
            log.logVerbose(host, "Connected")
        }


    }

    def exec(def command) {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec")
        channelExec.setCommand(command)
        InputStream input = channelExec.getInputStream()
        InputStream error = channelExec.getErrStream();
        channelExec.connect()
        log.logCommand(host, command)
        try {
            while (true) {
                while (input.available() > 0) {
                    input.eachLine { line ->
                        log.logStdOut(host, line)
                    }
                }
                while (error.available() > 0) {
                    error.eachLine { line ->
                        log.logStdErr(host, line)
                    }
                }

                if (channelExec.isClosed()) {
                    if (verbose)
                        log.logCommandStatus(host, command, channelExec.getExitStatus())
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

    def put(File file, def destination) {
        def command = "scp -p -t $destination"
        def channel = session.openChannel("exec")
        channel.setCommand(command)
        OutputStream outStream = channel.getOutputStream()
        InputStream inStream = channel.getInputStream()
        try {
            log.logVerbose(host, "Transfering file $file to $user@$host:$destination")
            channel.connect();

            if (checkAck(inStream) != 0) {
                throw new IOException("Failure tring to transfer file")
            }

            command = "T " + (file.lastModified() / 1000) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += (" " + (file.lastModified() / 1000) + " 0\n");
            outStream.write(command.getBytes()); outStream.flush();
            if (checkAck(inStream) != 0) {
                throw new IOException("Failure tring to transfer file")
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = file.length();
            command = "C0644 " + filesize + " ";
            if (destination.lastIndexOf('/') > 0) {
                command += destination.substring(destination.lastIndexOf('/') + 1);
            }
            else {
                command += destination;
            }
            command += "\n";
            outStream.write(command.getBytes()); outStream.flush();
            if (checkAck(inStream) != 0) {
                throw new IOException("Failure tring to transfer file")
            }

            // send a content of lfile
            def fis = new FileInputStream(file);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                outStream.write(buf, 0, len); //out.flush();
            }
            fis.close();
            // send '\0'
            buf[0] = 0;
            outStream.write(buf, 0, 1);
            outStream.flush();
            if (checkAck(inStream) != 0) {
                throw new IOException("Failure tring to transfer file")
            }
            log.logVerbose(host, "Transfered file $file to $user@$host:$destination successfully")
        } finally {
            if (outStream != null) {
                outStream.close()
            }
            if (channel != null) {
                channel.disconnect();
            }
        }


    }

    /**
     *
     * b may be 0 for success,
     *          1 for error,
     *          2 for fatal error,
     *          -1
     */
    int checkAck(InputStream inStream) throws IOException {
        int b = inStream.read();
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = inStream.read()) != '\n') {
                sb.append((char) c);
            }
        }
        return b;
    }


    def close() {
        if (verbose) {
            log.logVerbose(host, "Disconnecting")
        }
        if (session)
            session.disconnect()
        if (verbose) {
            log.logVerbose(host, "Disconnected")
        }
    }

}
