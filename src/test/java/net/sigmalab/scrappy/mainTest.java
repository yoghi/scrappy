package net.sigmalab.scrappy;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import java.nio.file.*;
import java.io.*;
import java.util.Set;
import java.util.HashSet;

public class mainTest {

    @Test
    public void testSinglePageExtract() throws Exception {

        String[] args = new String[2];

        File extractInfoDir = createTempDirectory();

        File currentPdf = new File(getClass().getResource("/forum-rs-emilia-romagna.pdf").toURI());

        args[0] = "--src-namefile=" + currentPdf.getAbsolutePath();
        args[1] = "--out-namedir=" + extractInfoDir.getAbsolutePath();

        Main.main(args);

        File[] elencoFiles = extractInfoDir.listFiles();
        Set<String> filePresenti = new HashSet<String>();
        for (final File fileEntry : elencoFiles) {
            filePresenti.add(fileEntry.getName());
            fileEntry.delete();
        }

        assertTrue(elencoFiles.length == 2);

        assertTrue(filePresenti.contains("content.txt"));
        assertTrue(filePresenti.contains("metadati.json"));

        extractInfoDir.delete();
    }

    @Test
    public void testMultiPageExtract() throws Exception {

        String[] args = new String[3];

        File extractInfoDir = createTempDirectory();

        File currentPdf = new File(getClass().getResource("/forum-rs-emilia-romagna.pdf").toURI());

        args[0] = "--src-namefile=" + currentPdf.getAbsolutePath();
        args[1] = "--out-namedir=" + extractInfoDir.getAbsolutePath();
        args[2] = "-p";


        Main.main(args);

        File[] elencoFiles = extractInfoDir.listFiles();
        Set<String> filePresenti = new HashSet<String>();
        for (final File fileEntry : elencoFiles) {
            filePresenti.add(fileEntry.getName());
            fileEntry.delete();
        }

        assertTrue(elencoFiles.length == 4);

        assertTrue(filePresenti.contains("metadati.json"));
        assertTrue(filePresenti.contains("0.txt"));
        assertTrue(filePresenti.contains("1.txt"));
        assertTrue(filePresenti.contains("2.txt"));

        extractInfoDir.delete();
    }


    public static File createTempDirectory() throws IOException {

        final File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

}