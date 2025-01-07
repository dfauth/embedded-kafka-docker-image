package io.github.dfauth.apicurio.serde;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class ThreadedProxy {

    private static Executor executor = ForkJoinPool.commonPool();

    public static void main(String[] args) throws IOException {
        int port;
        String[] dest;
        if(args.length > 1) {
            port = Integer.parseInt(args[0]);
            dest = args[1].split(":");
        } else {
            port = 8091;
            dest = new String[]{"localhost", "8090"};
        }
        var ss = new ServerSocket(port);
        while(true) {
            var srcSock = ss.accept();
            var destSock = new Socket(dest[0], Integer.parseInt(dest[1]));
            executor.execute(runnable("incoming", srcSock.getInputStream(), destSock.getOutputStream()));
            executor.execute(runnable("outgoing", destSock.getInputStream(), srcSock.getOutputStream()));
        }
    }

    private static Runnable runnable(String label, InputStream istream, OutputStream ostream) {
        return () -> {
            try {
                int len = 0;
                var buffer = new byte[1024];
                while((len = istream.read(buffer, 0, buffer.length)) > 0) {
                    ostream.write(buffer,0, len);
                    log.info("{}: {}",label,new String(buffer, 0, len));
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
//                throw new RuntimeException(e);
            } finally {

            }
        };
    }
}
