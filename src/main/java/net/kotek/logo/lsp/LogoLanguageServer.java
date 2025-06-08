package net.kotek.logo.lsp;

import com.bme.mlogo.MLogo;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

public class LogoLanguageServer implements org.eclipse.lsp4j.services.LanguageServer, LanguageClientAware {

    private final MLogo mlogo = new MLogo();
    private final TextDocumentService textDocumentService = new LogoTextDocumentService(mlogo);
    private final WorkspaceService workspaceService = new LogoWorkspaceService(this);

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        ServerCapabilities sc = new ServerCapabilities();
//        sc.setTextDocumentSync(TextDocumentSyncKind.Full);
//        sc.setCompletionProvider(LogoCompletionProvider.PROVIDER);

        sc.setDefinitionProvider(true);
        sc.setDocumentHighlightProvider(true);

        ServerInfo si = new ServerInfo();
        si.setName("LogoLSP");
        si.setVersion("1.0");

        return CompletableFuture.completedFuture(new InitializeResult(sc, si));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0); //TODO system.exit
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }



    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        client.showMessage(new MessageParams(MessageType.Info,"connected and started"));
    }
}
