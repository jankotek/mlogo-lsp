package net.kotek.logo.lsp;

import com.bme.mlogo.MLogo;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
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
            System.out.println(f);
            List s = serv.parseLogoHighlight(f.getPath());

            assertTrue(s.size()>1);
        }
    }

    @Test
    public void parseTestData() throws IOException {
        File examples = new File("test");
        assertTrue(examples.isDirectory() );

        for(File f:examples.listFiles()){
            if(!f.getName().endsWith("logo") || f.getName().contains("Syntax"))
                continue;
            System.out.println(f);
            List s = serv.parseLogoHighlight(f.getPath());

            assertTrue(s.size()>0, f.getName());
        }
    }
}