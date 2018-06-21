package processor;

import common.FileOperations;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FileUtils;

public class FileSearch {

    private String searchPath;
    private TextArea outputLogs;
    private boolean found;
    private HashSet<String> fromFilenames;
    private HashMap<String, String> fromFiles;
    private HashMap<String, ArrayList<String>> toFiles;
    private ArrayList<String> output;

    public FileSearch(TextArea fileSearchLog, String[] filePaths, String searchPath) throws Exception {
        try {
            this.searchPath = searchPath;
            this.outputLogs = fileSearchLog;
            this.found = false;
            this.output = new ArrayList();

            this.fromFilenames = new HashSet();
            this.fromFiles = new HashMap();
            this.toFiles = new HashMap();
            for (String temp : filePaths) {
                try {
                    File f = new File(temp);
                    if (f.exists()) {
                        this.fromFilenames.add(f.getName().toLowerCase());
                        this.fromFiles.put(f.getName().toLowerCase(), f.getCanonicalPath());
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    this.outputLogs.appendText("Ignoring " + temp + FileOperations.NEWLINE);
                    Thread.currentThread().sleep(100);
                }
            }

            Platform.runLater(() -> {
                this.outputLogs.appendText("Initialization complete!" + FileOperations.NEWLINE);
            });
        } catch (Exception e) {
            throw new Exception("Initialization Failed!");
        }
    }

    public void execute() throws Exception {
        String outputFilename = new SimpleDateFormat("'search_'yyyy_MM_dd_HH_mm'.txt'").format(new Date());
        // Opening up the directories        
        try {
            File SD = new File(searchPath);
            File out = new File(searchPath, outputFilename);

            Platform.runLater(() -> {
                outputLogs.appendText("Please Wait While Your Files Are Being Searched!" + FileOperations.NEWLINE + FileOperations.NEWLINE);
            });

            // Running recursive file iterator & populating memory with found files
            Files.walkFileTree(Paths.get(SD.getCanonicalPath()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String filename = file.toFile().getName().toLowerCase();
                    if (fromFilenames.contains(filename)) {
                        found = true;
                        if (toFiles.get(filename) == null) {
                            toFiles.put(filename, new ArrayList());
                        }
                        toFiles.get(filename).add(file.toFile().getCanonicalPath());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });

            // Checking for files found
            for (String filename : fromFilenames) {
                output.add("Source -> " + fromFiles.get(filename));
                if (toFiles.get(filename) == null) {
                    output.add("Target -> Not Found");
                } else {
                    output.add("Target ->" + FileOperations.NEWLINE);
                    for (String path : toFiles.get(filename)) {
                        output.add(path + FileOperations.NEWLINE);
                    }
                }
                output.add(FileOperations.NEWLINE);
            }

            if (found) {
                // Writing logs to file
                FileUtils.writeLines(out, "UTF-8", output, FileOperations.NEWLINE, false);
                Platform.runLater(() -> {
                    outputLogs.appendText("Please check output file for details!" + FileOperations.NEWLINE);
                    outputLogs.appendText("Output File: " + out.getAbsolutePath() + FileOperations.NEWLINE);
                });
            } else {
                out.delete();
                Platform.runLater(() -> {
                    outputLogs.appendText("None of the files were found!" + FileOperations.NEWLINE);
                });
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
