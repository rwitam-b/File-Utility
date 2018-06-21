package processor;

import common.FileOperations;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class FileCorrector {

    private HashSet<String> formats;
    private File listingDirectory;
    private TextArea outputLogs;
    private String renameType, renameExtension;
    private boolean trimSpaces, removeBlanks, extensionFilterType;

    public FileCorrector(TextArea log, String path, String[] extensions, boolean extensionFilterType, boolean trimSpaces, boolean removeBlanks, char renameType, String renameExtension) throws Exception {
        try {
            this.outputLogs = log;
            this.listingDirectory = new File(path);
            this.formats = new HashSet();
            this.formats = FileOperations.sanitizeFilter(new HashSet(Arrays.asList(extensions)));
            switch (renameType) {
                case 'U':
                    this.renameType = FileOperations.RENAME_UP;
                    break;
                case 'L':
                    this.renameType = FileOperations.RENAME_LOW;
                    break;
                case 'E':
                    this.renameType = FileOperations.RENAME_EXT;
                    break;
                default:
                    this.renameType = "INVALID";
                    throw new Exception();
            }
            this.trimSpaces = trimSpaces;
            this.removeBlanks = removeBlanks;
            this.extensionFilterType = extensionFilterType;
            this.renameExtension = renameExtension;
            Platform.runLater(() -> {
                outputLogs.appendText("Initialization complete!" + FileOperations.NEWLINE);
            });
        } catch (Exception e) {
            throw new Exception("Initialization failed!");
        }
    }

    public void execute() throws Exception {
        boolean dirty = false;
        try {
            Platform.runLater(() -> {
                outputLogs.appendText("Please Wait While Your Files Are Being Collected!" + FileOperations.NEWLINE + FileOperations.NEWLINE);
            });

            // Iterating through files and writing paths to memory
            ArrayList<String> files = FileOperations.listFilesToArray(listingDirectory, FileOperations.ABSOLUTE_PATH, formats, extensionFilterType);

            Platform.runLater(() -> {
                outputLogs.appendText("Processing Files!" + FileOperations.NEWLINE + FileOperations.NEWLINE);
            });

            // Removing spaces from beginning and end, A.K.A "Trimming" the File
            // Removing any blank lines in the file
            if (trimSpaces || removeBlanks) {
                for (String path : files) {
                    try {
                        long fileChanges = FileOperations.trimFileSpaces(path, removeBlanks);
                        if (fileChanges > 0) {
                            dirty = true;
                            Platform.runLater(() -> {
                                outputLogs.appendText("Removed " + fileChanges + " blanks from " + path + FileOperations.NEWLINE);
                            });
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            outputLogs.appendText(e.getMessage() + FileOperations.NEWLINE);
                        });
                    }
                }
                Platform.runLater(() -> {
                    outputLogs.appendText(FileOperations.NEWLINE);
                });
            }

            // Renaming files            
            for (String path : files) {
                try {
                    File f = new File(path);
                    String outName = FileOperations.rename(path, renameType, renameExtension);
                    if (!f.getName().equals(outName)) {
                        dirty = true;
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        outputLogs.appendText("Failed to rename " + path + FileOperations.NEWLINE);
                    });
                }
            }

            Platform.runLater(() -> {
                outputLogs.appendText(FileOperations.NEWLINE);
            });

            if (!dirty) {
                Platform.runLater(() -> {
                    outputLogs.appendText("All Files Are Already In Specified Format!" + FileOperations.NEWLINE);
                });
            } else {
                Platform.runLater(() -> {
                    outputLogs.appendText("File Sanitization Complete!" + FileOperations.NEWLINE);
                });
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
