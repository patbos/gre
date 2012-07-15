package com.patbos.gre

import com.jcraft.jsch.Session

class ScpUtil {

    private static final byte LINE_FEED = 0x0a

    def static put(def user, def host, Logger log, Session session, File file, def destination) {
        def channel = session.openChannel("exec");
        channel.setCommand("scp -t -p $destination");
        OutputStream output = channel.getOutputStream()
        InputStream input = channel.getInputStream()
        FileInputStream fis = null
        try {
            channel.connect()
            waitForAck(input)
            log.logVerbose(user, host, "Transfering file $file to $host:$destination")
            fis = new FileInputStream(file)
            def length = file.length()
            long lastmodified = file.lastModified() / 1000

            output.write("T $lastmodified 0 $lastmodified 0\n".toString().bytes)
            output.flush()
            waitForAck(input)


            output.write("C0644 $length $file.name\n".toString().bytes)
            output.flush()
            waitForAck(input)

            int percentTransmitted = 0
            long totalLength = 0

            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) {
                    break;
                }
                output.write(buf, 0, len);
                totalLength += len
                percentTransmitted = log.logProgress((int) totalLength / length * 100, percentTransmitted)
            }
            output.flush();
            sendAck(output);
            waitForAck(input);
        } finally {
            if (output != null) {
                output.close()
            }
            if (input != null) {
                input.close()
            }
            if (fis != null) {
                fis.close()
            }
            if (channel != null) {
                channel.disconnect()
            }

        }

    }


    def static get(def user, def host, Logger log, Session session, def source, File destination) {
        def channel = session.openChannel("exec");
        channel.setCommand("scp -f $source");
        OutputStream output = channel.getOutputStream()
        InputStream input = channel.getInputStream()
        try {
            channel.connect()
            sendAck(output)

            while (true) {
                // C0644 filesize filename - header for a regular file
                // T time 0 time 0\n - present if perserve time.
                // D directory - this is the header for a directory.
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                while (true) {
                    int read = input.read();
                    if (read < 0) {
                        return;
                    }
                    if ((byte) read == LINE_FEED) {
                        break;
                    }
                    stream.write(read);
                }
                String serverResponse = stream.toString("UTF-8");
                if (serverResponse.charAt(0) == 'C') {
                    parseAndFetchFile(serverResponse, destination, output, input);
                } else if (serverResponse.charAt(0) == 'D') {
                    destination = parseAndCreateDirectory(serverResponse,
                            destination);
                    sendAck(output);
                } else if (serverResponse.charAt(0) == 'E') {
                    destination = destination.getParentFile();
                    sendAck(output);
                } else if (serverResponse.charAt(0) == '\01'
                        || serverResponse.charAt(0) == '\02') {
                    // this indicates an error.
                    throw new IOException(serverResponse.substring(1));
                }
            }


        } finally {
            if (output != null) {
                output.close()
            }
            if (input != null) {
                input.close()
            }
            if (channel != null) {
                channel.disconnect()
            }
        }


    }

    def static waitForAck(InputStream input) throws IOException {
        int b = input.read()
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,

        if (b == -1) {
            // didn't receive any response
            throw new IOException("No response from server")
        } else if (b != 0) {
            StringBuffer sb = new StringBuffer();

            int c = input.read();
            while (c > 0 && c != '\n') {
                sb.append((char) c)
                c = input.read()
            }
            if (b == 1) {
                throw new IOException("server indicated an error: $sb")
            } else if (b == 2) {
                throw new IOException("server indicated a fatal error: $sb")
            } else {
                throw new IOException("unknown response, code $b message: $sb")
            }
        }


    }

    def static sendAck(OutputStream out) {
        byte[] buf = new byte[1]
        buf[0] = 0
        out.write(buf)
        out.flush()
    }

    /*

   http://kickjava.com/src/org/apache/tools/ant/taskdefs/optional/ssh/ScpToMessage.java.htm


   import static org.fusesource.jansi.Ansi.*


for (int i = 1; i < 101; i++)  {

   System.out.print( ansi().eraseLine(Erase.BACKWARD).cursorLeft(4) .render("$i %"))
   Thread.currentThread().sleep(100);
}
System.out.println()
    */
}