package net.sigmalab.scrappy;


import org.apache.pdfbox.util.PDFTextStripper;
import org.json.simple.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.commons.cli.*;
import java.nio.file.*;
import java.io.*;
import org.apache.tika.*;
import org.apache.tika.language.*;
import org.joda.time.*;
import org.joda.time.format.*;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.awt.print.PageFormat;


class Main {
    public static void main(String[] args) {

        // create Options object
        Options options = new Options();

        // add h option
        options.addOption("h", false, "help display");

        // add d option
        options.addOption("d", false, "debug display");

        // add p option
        options.addOption("p", false, "extract single page to file");

        options.addOption( OptionBuilder.withLongOpt( "src-namefile" )
                           .withDescription( "pdf source file" )
                           .hasArg()
                           .isRequired()
                           .withArgName("NAMEFILE")
                           .create() );

        options.addOption( OptionBuilder.withLongOpt( "out-namedir" )
                           .withDescription( "dir out information" )
                           .hasArg()
                           .withArgName("NAMEDIR")
                           .isRequired()
                           .create() );

        String srcFileName = null;
        String outDirName = null;

        // create the parser - http://commons.apache.org/proper/commons-cli/usage.html
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;

        try {

            // parse the command line arguments
            cmd = parser.parse( options, args );

        } catch ( ParseException exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );

            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "scrappy", options );
            return;
        }

        if ( cmd.hasOption("h") ) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "scrappy", options );
            return;
        }

        // get c option value
        srcFileName = cmd.getOptionValue("src-namefile");
        outDirName = cmd.getOptionValue("out-namedir");

        Path path = Paths.get(srcFileName);
        if ( Files.notExists(path) ) {
            System.err.println( "Invalid not exist src-namefile " + srcFileName);
            return;
        }

        Path pathOut = Paths.get(outDirName);
        if ( !Files.isDirectory(pathOut) ) {
            System.err.println( "Invalid not exist out-namedir " + outDirName);
            return;
        }

        PDDocument doc = null;

        LanguageIdentifier.initProfiles();

        try {

            /*http://pdfbox.apache.org/docs/1.8.3/javadocs/org/apache/pdfbox/pdmodel/PDDocument.html*/
            doc = PDDocument.load(srcFileName);

            JSONObject objDocument = new JSONObject();

            // Caratteristiche
            int pageCount = doc.getNumberOfPages();
            objDocument.put("PageCount", pageCount);

            PDDocumentInformation infoDoc = doc.getDocumentInformation();
            final Set<String> metadataKeys = infoDoc.getMetadataKeys();
            for (String key : metadataKeys) {
                final String value = infoDoc.getCustomMetadataValue(key);
                objDocument.put(key, value);
            }

            java.util.Calendar creationDate = infoDoc.getCreationDate();
            DateTime creationDateTime = new DateTime(creationDate);
            DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
            objDocument.put("CreationDateFormatted", fmt.print(creationDateTime));

            PageFormat pageFormat = doc.getPageFormat(0);
            objDocument.put("PageFormatWidth", pageFormat.getWidth());
            objDocument.put("PageFormatHeight", pageFormat.getHeight());

            if ( pageFormat.getOrientation() == PageFormat.LANDSCAPE ) {
                objDocument.put("Orientation", "LANDSCAPE");
            }

            if ( pageFormat.getOrientation() == PageFormat.PORTRAIT ) {
                objDocument.put("Orientation", "PORTRAIT");
            }

            String mimeType = new Tika().detect(srcFileName);
            objDocument.put("content_type", mimeType);

            Set<String> languages = new HashSet<String>();
            if ( cmd.hasOption("p")  ) {
                for (int i = 0; i < pageCount; i++) {
                    PDFTextStripper stripper = new PDFTextStripper("UTF-8");
                    int page = i + 1;
                    stripper.setStartPage(page);
                    stripper.setEndPage(page);

                    final String text = stripper.getText(doc);

                    LanguageIdentifier langIdentifier = new LanguageIdentifier(text);
                    String language = langIdentifier.getLanguage();
                    languages.add(language);

                    PrintWriter writer = new PrintWriter(outDirName + File.separator + i + ".txt", "UTF-8");
                    writer.println(text);
                    writer.close();
                }
            } else {
                PDFTextStripper stripper = new PDFTextStripper("UTF-8");
                final String text = stripper.getText(doc);
                LanguageIdentifier langIdentifier = new LanguageIdentifier(text);
                String language = langIdentifier.getLanguage();
                languages.add(language);
                PrintWriter writer = new PrintWriter(outDirName + File.separator + "content.txt", "UTF-8");
                writer.println(text);
                writer.close();
            }

            JSONArray listLanguage = new JSONArray();
            for(String l : languages) {
                listLanguage.add(l);
            }
            objDocument.put("languages",listLanguage);

            PrintWriter writer = new PrintWriter(outDirName + File.separator + "metadati.json", "UTF-8");
            writer.println(objDocument);
            writer.close();

        } catch (IOException exp) {
            System.err.println( "Open file failed.  Reason: " + exp.getMessage() );
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException exp) {
                    System.err.println( "Close file failed.  Reason: " + exp.getMessage() );
                }
            }
        }
    }

}