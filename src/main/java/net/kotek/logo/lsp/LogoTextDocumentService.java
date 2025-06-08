package net.kotek.logo.lsp;

import com.bme.logo.LAtom;
import com.bme.logo.LList;
import com.bme.logo.LWord;
import com.bme.logo.Parser;
import com.bme.mlogo.MLogo;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class LogoTextDocumentService  implements TextDocumentService {

    private final MLogo mlogo;

    private final Map<String, String> docContent = new ConcurrentHashMap<String, String>();

    public LogoTextDocumentService(MLogo mlogo) {
        this.mlogo = mlogo;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        docContent.put(params.getTextDocument().getUri(), params.getTextDocument().getText());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        //only possible with full sync
        //TODO incremental sync
        String content = params.getContentChanges().getLast().getText();
        docContent.put(params.getTextDocument().getUri(), content);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        docContent.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        docContent.put(params.getTextDocument().getUri(), params.getText());
    }


    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
            String content = docContent.get(params.getTextDocument().getUri());
            if(content == null)
                return CompletableFuture.completedFuture(Collections.emptyList());

            CompletableFuture<List<? extends DocumentHighlight>> ret = new CompletableFuture<>();

            //TODO common exec pool?
            ForkJoinPool.commonPool().execute(() -> {
                try{
                    ret.complete(parseLogoHighlight(content));
                } catch (Exception e) {
                    ret.completeExceptionally(e);
                }
            });
            return ret;
    }

    List<DocumentHighlight> parseLogoHighlight(String content) throws IOException {
        content = normaliseLines(content);

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

    private static String normaliseLines(String content) {
        //normalize new lines, parser only takes \n delimiters
        return content.lines().collect(Collectors.joining("\n"));
    }


    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        String uri = params.getTextDocument().getUri();
        String content = docContent.get(uri);
        if (uri == null)
            return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));


        content = normaliseLines(content);
        LList code = Parser.parse(content);
        LList flat = code.flatten();

        LWord found = null;
        //find atom at current position
        int linePos = params.getPosition().getLine();
        int charPos = params.getPosition().getCharacter();
        for (int i = 0; i < flat.size(); i++) {
            LAtom a = flat.item(i);
            if (!(a instanceof LWord))
                continue;
            LWord lw = (LWord) a;
            if (lw.posStart == null || lw.posEnd == null)
                continue;

            if (lw.posStart.getLine() != linePos)
                continue;

            if (lw.posStart.getCharacter() <= charPos && charPos <= lw.posEnd.getCharacter()) {
                found = lw;
                break;
            }
        }

        if (found == null) {
            return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
        }

        //we got something with the same position, traverse tree and find definition...
        String name = found.value;
        for (int i = 0; i < flat.size(); i++) {
            LAtom a = flat.item(i);
            if (!(a instanceof LWord))
                continue;
            LWord lw = (LWord) a;
            if (lw.posStart == null || lw.posEnd == null)
                continue;

            if(lw.type==LWord.Type.Name && name.equals(lw.value)){
                //found matching definition
                Location loc = new Location();
                loc.setUri(uri); //always in the same file
                loc.setRange(new Range(lw.posStart, lw.posEnd));
                return CompletableFuture.completedFuture(Either.forLeft(List.of(loc)));
            }
        }

        return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
    }
}
