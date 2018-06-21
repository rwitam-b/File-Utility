package processor;

import common.FileOperations;
import static common.FileOperations.SORT_ASC;
import static common.FileOperations.SORT_DESC;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FileUtils;

public class FileListing {

    private File listingDirectory;
    private String listType, sortType;
    private TextArea outputLogs;
    private HashSet<String> filter;
    private boolean writeToFile, extensionFilterType;

    public FileListing(TextArea log, String path, char listType, char sortType, String[] extensions, boolean extensionFilterType, boolean writeToFile) throws Exception {
        try {
            this.listingDirectory = new File(path);
            switch (listType) {
                case 'A':
                    this.listType = FileOperations.ABSOLUTE_PATH;
                    break;
                case 'F':
                    this.listType = FileOperations.FILE_NAME;
                    break;
                default:
                    this.listType = String.valueOf(listType);
            }
            switch (sortType) {
                case 'A':
                    this.sortType = FileOperations.SORT_ASC;
                    break;
                case 'D':
                    this.sortType = FileOperations.SORT_DESC;
                    break;
                default:
                    this.sortType = String.valueOf(listType);
            }
            this.outputLogs = log;
            this.writeToFile = writeToFile;
            this.extensionFilterType = extensionFilterType;

            this.filter = FileOperations.sanitizeFilter(new HashSet(Arrays.asList(extensions)));

            Platform.runLater(() -> {
                outputLogs.appendText("Initialization complete!" + FileOperations.NEWLINE);
            });
        } catch (Exception e) {
            throw new Exception("Initialization Failed!");
        }
    }

    public void execute() throws Exception {
        String filename = new SimpleDateFormat("'list_'yyyy_MM_dd_HH_mm'.txt'").format(new Date());
        try {

            // Disk Permission Checks
            if (writeToFile) {
                FileOperations.checkDiskWriteAccess(listingDirectory.getCanonicalPath());
            }

            Platform.runLater(() -> {
                outputLogs.appendText("Please Wait While Your Files Are Being Collected!" + FileOperations.NEWLINE + FileOperations.NEWLINE);
            });

            // Iterating through files and writing paths to temporary file
            ArrayList<String> results = FileOperations.listFilesToArray(listingDirectory, listType, filter, extensionFilterType);            

            if (results.size() > 0) {
                // Sorting filelist based on "Sorting Order"
                if (sortType.equals(SORT_ASC)) {
                    Collections.sort(results, (str1, str2) -> {
                        return str1.trim().toLowerCase().compareTo(str2.trim().toLowerCase());
                    });
                } else if (sortType.equals(SORT_DESC)) {
                    Collections.sort(results, (str1, str2) -> {
                        return str2.trim().toLowerCase().compareTo(str1.trim().toLowerCase());
                    });
                }

                if (writeToFile) {
                    File out = new File(listingDirectory.getCanonicalPath(), filename);
                    FileUtils.writeLines(out, "UTF-8", results, FileOperations.NEWLINE, false);
                    Platform.runLater(() -> {
                        outputLogs.appendText("Filelist has been written to " + out.getAbsolutePath() + FileOperations.NEWLINE);
                    });
                } else {

                    // In case of huge file list, and "Write To File" not selected, will write to file anyways          
                    // My tests showed that 20K lines was a good bet for an upper limit for the GUI Display
                    int printFactor = 20000;
                    if (results.size() > printFactor) {
                        try {
                            Platform.runLater(() -> {
                                // Pop an alert to the user asking for a choice of operation
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Performace Warning!");
                                alert.setHeaderText(results.size() + " files detected!");
                                alert.setContentText("Writing all data to the screen might crash the application!" + FileOperations.NEWLINE + "The filelist is being written to a file instead.");
                                alert.show();
                            });

                            // Write output to a file
                            File out = new File(listingDirectory.getCanonicalPath(), filename);
                            FileUtils.writeLines(out, "UTF-8", results, FileOperations.NEWLINE, false);

                            Platform.runLater(() -> {
                                outputLogs.appendText("Filelist has been written to " + out.getAbsolutePath() + FileOperations.NEWLINE);
                            });
                        } catch (Exception e) {
                            throw new Exception("Failed while writing list to file!" + FileOperations.NEWLINE);
                        }

                    } else {
                        try {
                            StringBuilder logVariable = new StringBuilder("");
                            results.forEach((str) -> {
                                logVariable.append(str).append(FileOperations.NEWLINE);
                            });
                            Platform.runLater(() -> {
                                outputLogs.appendText(logVariable.toString());
                            });
                        } catch (Exception e) {
                            throw new Exception("Failed while writing list to GUI!" + FileOperations.NEWLINE);
                        }
                    }
                }
            } else {

                // No files are found matching the search criteria                
                if (filter.size() > 0) {
                    Platform.runLater(() -> {
                        outputLogs.appendText("No Files Found With The Extensions " + Arrays.toString(filter.toArray()) + " !" + FileOperations.NEWLINE);
                    });
                } else {
                    Platform.runLater(() -> {
                        outputLogs.appendText("No Files Found!" + FileOperations.NEWLINE);
                    });
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
