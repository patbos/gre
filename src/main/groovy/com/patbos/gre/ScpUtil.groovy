package com.patbos.gre

import com.jcraft.jsch.Session

class ScpUtil {

    def static put(Session session, File file, def destination) {
        def channel = session.openChannel("exec");
        channel.setCommand("scp -t -p $destination");
        OutputStream output = channel.getOutputStream()
        InputStream input = channel.getInputStream()
        FileInputStream fis = null
        try {
            channel.connect()
            waitForAck(input)
            fis = new FileInputStream(file)
            def length = file.length()
            output.write("C0644 $length $file.name\n".bytes)
            output.flush()
            waitForAck(input)

            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) {
                    break;
                }
                output.write(buf, 0, len);
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


}