package net.kotek.logo.lsp;

import com.bme.logo.LAtom;
import com.bme.logo.LList;
import com.bme.logo.LWord;
import com.bme.logo.Parser;
import com.bme.mlogo.MLogo;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LogoTextDocumentService  implements TextDocumentService {
    public LogoTextDocumentService(LogoLanguageServer logoLanguageServer) {
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {

    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {

    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {

    }


    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
        String fUri = params.getTextDocument().getUri().replace("file:",""); //FIXME url
        //TODO security check for local files only

        try {
            List<DocumentHighlight> ret = parseLogoHighlight(fUri);

            //TODO async exec...
            return CompletableFuture.completedFuture(ret);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    static List<DocumentHighlight> parseLogoHighlight(String fUri) throws IOException {
        String content = MLogo.loadFile(fUri);
        LList code = Parser.parse(content);

        LList flat = code.flatten();

        List<DocumentHighlight> ret = new ArrayList<>();
        //get everything with position
        for(int i=0;i<flat.size();i++){
            LAtom atom = flat.item(i);
            if(!(atom instanceof LWord))
                continue;
            LWord word = (LWord) atom;
            if(word.posStart==null || word.posEnd==null)
                continue;

            DocumentHighlightKind kind = DocumentHighlightKind.Text;
            DocumentHighlight h = new DocumentHighlight(new Range(word.posStart, word.posEnd), kind );
            ret.add(h);

        }
        return ret;
    }
}
