package com.bme.mlogo;

import com.bme.logo.Environment;
import com.bme.logo.Primitives;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MLogoTest {

    @Test
    public void testAndCompareOutputs() throws IOException {
        File testDataDir = new File("./test");
        assertTrue(testDataDir.exists() && testDataDir.isDirectory(), "test data dir");

        for(File outF : testDataDir.listFiles() ){
            if(!outF.getName().endsWith(".logo"))
                continue;

            Path expectedOutputFile = Path.of(outF.getPath().replaceAll("logo$", "out"));
            Path expectedErrFile = Path.of(outF.getPath().replaceAll("logo$", "err"));


            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MLogo mlogo = new MLogo();
            mlogo.out = new PrintStream(out);

            Environment e = Primitives.kernel();
            mlogo.primitiveIO(e, false);

//            TurtleGraphics t = new TurtleGraphics(e);
            mlogo.runFile(e, outF.getPath(), null);

            //TODO catch failed exit code?!

            String testOutput = out.toString("UTF-8");
            Path eOutF = Files.exists(expectedOutputFile)? expectedOutputFile: expectedErrFile;
            String expectedOutput = Files.readString(eOutF);
            assertEquals(expectedOutput, testOutput, outF.getName());

        }

    }
}
