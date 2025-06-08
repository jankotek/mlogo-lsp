package net.kotek.logo.lsp;

import com.bme.mlogo.MLogo;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogoTextDocumentServiceTest {


    MLogo mlogo = new MLogo();
    LogoTextDocumentService serv = new LogoTextDocumentService(mlogo);

    @Test
    public void parseExamples() throws IOException {
        File examples = new File("examples");
        assertTrue(examples.isDirectory() );

        for(File f:examples.listFiles()){
            checkFile(f);
        }
    }

    @Test
    public void parseTestData() throws IOException {
        File examples = new File("test");
        assertTrue(examples.isDirectory() );

        for(File f:examples.listFiles()){
            if(!f.getName().endsWith("logo") || f.getName().contains("Syntax"))
                continue;
            checkFile(f);
        }
    }

    private void checkFile(File f) throws IOException {
        System.out.println(f);

        String content = Files.readString(f.toPath());
        List s = serv.parseLogoHighlight(content);
        assertTrue(s.size()>0, f.getName());
    }
}