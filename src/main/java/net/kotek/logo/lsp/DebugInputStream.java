package net.kotek.logo.lsp;

import java.io.*;
import java.nio.file.Path;

public class DebugInputStream extends InputStream {

    private final OutputStream out;
    private final InputStream ins;

    public DebugInputStream(InputStream ins, File saveToFile) {

        try {
            this.ins = ins;
            out = new FileOutputStream(saveToFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read() throws IOException {
        int i = ins.read();
        out.write(i);
        return i;
    }


    @Override
    public void close() throws IOException {
        ins.close();
        out.close();
    }

}
