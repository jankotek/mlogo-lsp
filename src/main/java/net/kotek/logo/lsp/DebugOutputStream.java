package net.kotek.logo.lsp;

import java.io.*;

public class DebugOutputStream extends OutputStream {

    private final OutputStream out;
    private final OutputStream fout;

    public DebugOutputStream(OutputStream out, File saveToFile) {
        this.out = out;
        try {
            this.fout = new FileOutputStream(saveToFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        fout.write(b);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
        fout.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
        fout.close();
    }
}
