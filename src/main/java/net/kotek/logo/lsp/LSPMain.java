package net.kotek.logo.lsp;


import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/** main class to start Logo LSP server */
public class LSPMain {

    public static OutputStream outs;
    private static final Logger LOGGER = Logger.getLogger(LSPMain.class.getName());
    public static void main(String[] args) throws InterruptedException {


        // add some crude debugging
        File fout = new File("/home/jan/logs/out" + System.currentTimeMillis() + ".txt");
        File fins = new File("/home/jan/logs/ins" + System.currentTimeMillis() + ".txt");
        InputStream ins = new DebugInputStream(System.in, fins);
        outs =new DebugOutputStream(System.out, fout);

        LanguageServer server = new LogoLanguageServer() ;
        Launcher<LanguageClient> launcher =
                LSPLauncher.createServerLauncher(server,
                        ins,
                        outs);
        launcher.startListening();
        Thread.sleep(100000);
    }
}
