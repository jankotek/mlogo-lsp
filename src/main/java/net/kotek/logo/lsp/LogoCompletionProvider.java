package net.kotek.logo.lsp;

import org.eclipse.lsp4j.CompletionOptions;

import java.util.List;

public class LogoCompletionProvider {

    public static final CompletionOptions PROVIDER =
            new CompletionOptions(false, List.of("."));

}
