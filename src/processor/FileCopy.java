package processor;

import common.FileOperations;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FileUtils;

public class FileCopy {

    private ArrayList<String> fileList;
    private String sourcePath, destinationPath;
    private TextArea outputLogs;
    private boolean preserve;

    public FileCopy(TextArea log, String destinationPath, String[] fileList, boolean preserve) throws Exception {
        try {
            this.destinationPath = destinationPath;
            this.outputLogs = log;
            this.preserve = preserve;

            // Putting the sanitized list of files in an ArrayList
            this.fileList = new ArrayList();
            for (String temp : fileList) {
                try {
                    File f = new File(temp);
                    if (f.exists()) {
                        this.fileList.add(FileOperations.sanitizePath(temp));
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    outputLogs.appendText("Ignoring " + temp + FileOperations.NEWLINE);
                    Thread.currentThread().sleep(100);
                }
            }

            // Sort the file paths according to name
            Collections.sort(this.fileList);

            if (this.fileList.size() > 0) {
                // Calculating the longest common prefix from the first and last file paths -> Source Directory Path
                this.sourcePath = getCommonPrefix(this.fileList.get(0), this.fileList.get(this.fileList.size() - 1));

                // Sanitizing paths            
                this.destinationPath = FileOperations.sanitizePath(this.destinationPath);
                this.sourcePath = FileOperations.sanitizePath(this.sourcePath);
            }

            Platform.runLater(() -> {
                outputLogs.appendText(FileOperations.NEWLINE + "Initialization complete!" + FileOperations.NEWLINE);
            });
        } catch (Exception e) {
            throw new Exception("Initialization Failed!");
        }
    }

    private String getCommonPrefix(String str1, String str2) {
        str1 = FileOperations.sanitizePath(str1);
        str2 = FileOperations.sanitizePath(str2);
        int pos = 0;
        while (pos < str1.length() && pos < str2.length() && str1.charAt(pos) == str2.charAt(pos)) {
            pos++;
        }
        return str1.substring(0, pos);
    }

    public void execute() {
        try {

            // Checking whether any files are present to copy
            if (fileList.isEmpty()) {
                throw new Exception("No Files To Copy!");
            }

            // Checking whether the source and destination folders are different
            if (sourcePath.equalsIgnoreCase(destinationPath)) {
                throw new Exception("Source And Destination Paths Are The Same!");
            }

            // Checking whether user needs to preserve directory structure in the copy process
            if (preserve && this.sourcePath.isEmpty()) {
                throw new Exception("No common directory structure found within the input file list!");
            }

            if (preserve) {
                Platform.runLater(() -> {
                    outputLogs.appendText("Starting File Copy ->" + FileOperations.NEWLINE);
                    outputLogs.appendText("Origin: " + this.sourcePath + FileOperations.NEWLINE + "Target: " + this.destinationPath + FileOperations.NEWLINE + FileOperations.NEWLINE);
                });
            } else {
                Platform.runLater(() -> {
                    outputLogs.appendText("Starting File Copy ->" + FileOperations.NEWLINE);
                    outputLogs.appendText("Target: " + this.destinationPath + FileOperations.NEWLINE + FileOperations.NEWLINE);
                });
            }

            File outDirectory = new File(destinationPath);
            for (String filename : fileList) {
                try {

                    // File copy along with some basic validations
                    File f1 = new File(filename);
                    File f2 = new File(f1.getCanonicalPath().replace(sourcePath, destinationPath));
                    if (preserve) {
                        if (f1.isDirectory()) {
                            if (!f2.mkdirs()) {
                                throw new Exception("Failed in creating directory " + FileOperations.sanitizePath(f2.getAbsolutePath()) + FileOperations.NEWLINE);
                            }
                        } else if (f1.isFile()) {
                            FileUtils.copyFile(f1, f2);
                        }
                    } else {
                        FileUtils.copyFileToDirectory(f1, outDirectory);
                    }
                } catch (Exception e) {
                    try {
                        outputLogs.appendText(e.getMessage() + FileOperations.NEWLINE);
                        Thread.currentThread().sleep(100);
                    } catch (Exception e2) {
                    }
                }
            }

            Platform.runLater(() -> {
                outputLogs.appendText(FileOperations.NEWLINE + "All files have been processed!" + FileOperations.NEWLINE);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                outputLogs.appendText("Failed In Copy!" + FileOperations.NEWLINE + e.getMessage() + FileOperations.NEWLINE);
            });
        }
    }
}
