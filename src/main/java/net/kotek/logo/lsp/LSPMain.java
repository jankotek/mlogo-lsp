package net.kotek.logo.lsp;


import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.*;
import java.util.logging.Logger;

/** main class to start Logo LSP server */
public class LSPMain {

    public static OutputStream outs = System.out;
    private static final Logger LOGGER = Logger.getLogger(LSPMain.class.getName());

    static PrintStream log;
    public static void main(String[] args) throws InterruptedException {

        InputStream ins = System.in;

        if(args!=null && args.length==1) {
            String f = args[0];
            File ff = new File(f);
            if(!ff.exists() || !ff.isDirectory()){
                System.err.println("Debug dir error");
                System.exit(-1);
            }
            // add some crude debugging
            long time = System.currentTimeMillis();
            File fout = new File(f+"/out" + time + ".txt");
            File fins = new File(f+"/ins" + time + ".txt");
            ins = new DebugInputStream(System.in, fins);
            outs = new DebugOutputStream(System.out, fout);

            try {
                log = new PrintStream(new FileOutputStream(f+"/log" + time + ".txt"));
            } catch (FileNotFoundException e) {
            };
        }

        LanguageServer server = new LogoLanguageServer() ;
        Launcher<LanguageClient> launcher =
                LSPLauncher.createServerLauncher(server,
                        ins,
                        outs);
        launcher.startListening();
        Thread.sleep(100000);
    }
}
