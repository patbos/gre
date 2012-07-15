package com.patbos.gre

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException

class GreRuntime {


    def ssh
    def session
    def host
    def user
    def log

    def init(def log, host, port, username, key, password, int timeout) {
        this.host = host
        user = username
        ssh = new JSch()
        this.log = log;


        session = ssh.getSession(username, host, port)
        if (password) {
            log.logVerbose(username, host, "Connecting to $username@$host with password")
            session.setPassword(password)
        } else {
            log.logVerbose(username, host, "Connecting to $username@$host with key $key")
            ssh.addIdentity(key)
        }

        ssh.setHostKeyRepository(new NullHostKeyRepository())

        try {
            session.connect(timeout * 1000)
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
        log.logVerbose(username, host, "Connected")


    }

    def exec(def command) {
        exec(command, true, true, false, null)
    }

    def sudoExec(def command, def password) {
        exec(command, true, false, true, password)
    }

    def exec(def command, boolean throwError, boolean logCommand, boolean sudo, def password) {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec")
        if (sudo) {
            channelExec.setCommand("sudo -S -p '' $command")
        } else {
            channelExec.setCommand(command)
        }

        InputStream input = channelExec.getInputStream()
        InputStream error = channelExec.getErrStream()
        OutputStream output = channelExec.getOutputStream()


        channelExec.connect()


        log.logCommand(user, host, command)
        def stdOut = new ArrayList<String>()
        def stdErr = new ArrayList<String>()
        def statusCode = -1;
        try {
            if (sudo) {
                output.write((password + "\n").bytes)
                output.flush()
            }
            while (true) {
                while (error.available() > 0) {
                    error.eachLine { line ->
                        stdErr.add(line)
                        if (logCommand)
                            log.logStdErr(user, host, line)
                    }
                }
                while (input.available() > 0) {
                    input.eachLine { line ->
                        stdOut.add(line)
                        if (logCommand)
                            log.logStdOut(user, host, line)
                    }
                }

                if (channelExec.isClosed()) {
                    statusCode = channelExec.getExitStatus();
                    log.logCommandStatus(user, host, command, statusCode)
                    if (throwError && statusCode != 0) {
                        throw new ExecutionException("Error executing '$command'")
                    }

                    break
                }
                try {
                    Thread.sleep(100)
                } catch (Exception ee) {

                }
            }
        } finally {
            input.close()
            error.close()
            output.close()
            channelExec.disconnect()
        }
        return new ExecResult(stdErr: stdErr, stdOut: stdOut, exitCode: statusCode)

    }

    def put(File file, def destination) {
        ScpUtil.put(user, host, log, session, file, destination)
    }

    def get(def remote, File local) {
        def command = "scp -f $remote"
        def channel = session.openChannel("exec")
        channel.setCommand(command)
        OutputStream outStream = channel.getOutputStream()
        InputStream inStream = channel.getInputStream()
        FileOutputStream fos = null
        try {
            channel.connect()


            byte[] buf = new byte[1024]

            // send '\0'
            buf[0] = 0; outStream.write(buf, 0, 1)
            outStream.flush()

            while (true) {
                int c = checkAck(inStream)
                if (c != 'C') {
                    break;
                }

                // read '0644 '
                inStream.read(buf, 0, 5);

                long fileSize = 0L;
                while (true) {
                    if (inStream.read(buf, 0, 1) < 0) {
                        // error
                        break
                    }
                    if (buf[0] == ' ') break;
                    fileSize = fileSize * 10L + (long) (buf[0] - '0');
                }

                String file = null;
                for (int i = 0; ; i++) {
                    inStream.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

                // send '\0'
                buf[0] = 0; outStream.write(buf, 0, 1)
                outStream.flush()

                // read a content of lfile
                fos = new FileOutputStream(local)
                int foo;
                while (true) {
                    if (buf.length < fileSize) foo = buf.length
                    else foo = (int) fileSize
                    foo = inStream.read(buf, 0, foo)
                    if (foo < 0) {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    fileSize -= foo;
                    if (fileSize == 0L) break;
                }

                if (checkAck(inStream) != 0) {
                    throw new IOException("Failure transfering file")
                }

                // send '\0'
                buf[0] = 0; outStream.write(buf, 0, 1);
                outStream.flush();
            }


        } finally {
            if (outStream != null) {
                outStream.close()
            }
            if (inStream != null) {
                inStream.close()
            }
            if (fos != null) {
                fos.close()
            }
            if (channel != null) {
                channel.disconnect()
            }
        }
    }

    def logScript(def message) {
        log.logFromScript(user, host, message)
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
        log.logVerbose(user, host, "Disconnecting")
        if (session)
            session.disconnect()
        log.logVerbose(user, host, "Disconnected")
    }

}