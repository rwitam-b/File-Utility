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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FileUtils;

public class FileMerge {

    private File mergeDirectory;
    private boolean commentRequired, segregationRequired, extensionFilterType;
    private String mergeType, commentPrefix, sortOrder, mergeOrder;
    private TextArea outputLogs;
    private HashSet<String> filter;
    private String outputFilenameSuffix;

    public FileMerge(String mergeDirectory, String[] filter, boolean extensionFilterType, String mergeType, boolean segregationRequired, boolean commentRequired, String commentPrefix, String sortOrder, String mergeOrder, TextArea outputLogs) throws Exception {
        try {
            this.mergeDirectory = new File(mergeDirectory);
            this.segregationRequired = segregationRequired;
            this.commentRequired = commentRequired;
            this.mergeType = mergeType;
            this.commentPrefix = commentPrefix;
            this.sortOrder = sortOrder;
            this.mergeOrder = mergeOrder;
            this.outputLogs = outputLogs;
            this.filter = new HashSet();
            this.filter = FileOperations.sanitizeFilter(new HashSet(Arrays.asList(filter)));
            this.extensionFilterType = extensionFilterType;
            this.outputFilenameSuffix = new SimpleDateFormat("'_'yyyy_MM_dd_HH_mm'.txt'").format(new Date());
            Platform.runLater(() -> {
                outputLogs.appendText("Initialization complete!" + FileOperations.NEWLINE);
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Initialization Failed!");
        }
    }

    private Comparator<String> getFileComparator(String sortOrder, String sortParameter) {
        SimpleIntegerProperty sortOrderFactor = new SimpleIntegerProperty();
        if (sortOrder.equals(FileOperations.SORT_ASC)) {
            sortOrderFactor.set(1);
        } else if (sortOrder.equals(FileOperations.SORT_DESC)) {
            sortOrderFactor.set(-1);
        }
        System.out.println("in get comparator with order of " + sortOrderFactor.get());
        if (sortParameter.equals("Filename")) {
            return ((str1, str2) -> {
                return sortOrderFactor.get() * (new File(str1).getName().toLowerCase().compareTo(new File(str2).getName().toLowerCase()));
            });
        } else if (sortParameter.equals("Last Modified")) {
            return ((str1, str2) -> {
                return sortOrderFactor.get() * Long.signum(new File(str1).lastModified() - new File(str2).lastModified());
            });
        } else if (sortParameter.equals("Size")) {
            return ((str1, str2) -> {
                return sortOrderFactor.get() * Long.signum(new File(str1).length() - new File(str2).length());
            });
        }
        return null;
    }

    private void mergeUnderSource() throws Exception {
        try {
            Platform.runLater(() -> {
                outputLogs.appendText("Please Wait While Your Files Are Being Collected!" + FileOperations.NEWLINE + FileOperations.NEWLINE);
            });

            if (segregationRequired) {
                // Listing out all files extension wise
                HashMap<String, ArrayList<String>> files = FileOperations.listFilesByTypes(mergeDirectory, FileOperations.ABSOLUTE_PATH, filter, extensionFilterType);

                for (String format : files.keySet()) {
                    // Iterating through each file-format
                    Platform.runLater(() -> {
                        outputLogs.appendText("Merging " + format + "'s...." + FileOperations.NEWLINE);
                    });

                    // Sorting files in merge order
                    Collections.sort(files.get(format), this.getFileComparator(sortOrder, mergeOrder));

                    String mergeFilename = "merge_" + format + outputFilenameSuffix;
                    File out = new File(mergeDirectory, mergeFilename);

                    for (String path : files.get(format)) {
                        // Iterating through each format's files
                        File f = new File(path);
                        List<String> fileContents = FileUtils.readLines(f, "UTF-8");
                        if (commentRequired) {
                            fileContents.add(0, commentPrefix + "Contents of " + f.getCanonicalPath());
                        }
                        fileContents.add(FileOperations.NEWLINE);
                        FileUtils.writeLines(out, fileContents, FileOperations.NEWLINE, true);
                    }
                }
            } else {
                // Listing out all files
                ArrayList<String> files = FileOperations.listFilesToArray(mergeDirectory, FileOperations.ABSOLUTE_PATH, filter, extensionFilterType);

                // Sorting files in merge order
                Collections.sort(files, this.getFileComparator(sortOrder, mergeOrder));

                String mergeFilename = "merge_all" + outputFilenameSuffix;
                File out = new File(mergeDirectory, mergeFilename);

                for (String file : files) {
                    // Iterating through each file
                    File f = new File(file);
                    List<String> fileContents = FileUtils.readLines(f, "UTF-8");
                    if (commentRequired) {
                        fileContents.add(0, commentPrefix + "Contents of " + f.getCanonicalPath());
                    }
                    fileContents.add(FileOperations.NEWLINE);
                    FileUtils.writeLines(out, fileContents, FileOperations.NEWLINE, true);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void mergeUnderParents() throws Exception {
        try {
            Platform.runLater(() -> {
                outputLogs.appendText("Please Wait While Your Files Are Being Collected!" + FileOperations.NEWLINE + FileOperations.NEWLINE);
            });
            if (segregationRequired) {
                HashMap<String, HashMap<String, ArrayList<String>>> filesToMerge = new HashMap();
                Files.walkFileTree(Paths.get(mergeDirectory.getCanonicalPath()), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        File f = file.toFile();
                        String parentDirectory = f.getParent();
                        if (FileOperations.isAcceptableFormat(filter, f.getName(), extensionFilterType)) {
                            String extension = FileOperations.getExtension(f.getName());
                            if (!filesToMerge.containsKey(extension)) {
                                filesToMerge.put(extension, new HashMap());
                            }
                            if (!filesToMerge.get(extension).containsKey(parentDirectory)) {
                                filesToMerge.get(extension).put(parentDirectory, new ArrayList());
                            }
                            filesToMerge.get(extension).get(parentDirectory).add(file.toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                });

                // Iterating through each file extension
                for (Entry<String, HashMap<String, ArrayList<String>>> entry : filesToMerge.entrySet()) {
                    String extension = entry.getKey();
                    HashMap<String, ArrayList<String>> directoryMap = entry.getValue();

                    // Iterating through each directory, for files of specified extension
                    for (Entry<String, ArrayList<String>> entry2 : directoryMap.entrySet()) {
                        String directory = entry2.getKey();
                        ArrayList<String> files = entry2.getValue();

                        // Sorting files in merge order
                        Collections.sort(files, getFileComparator(sortOrder, mergeOrder));

                        // Merging files
                        for (String file : files) {
                            File f = new File(file);
                            List<String> fileContents = FileUtils.readLines(f, "UTF-8");
                            if (commentRequired) {
                                fileContents.add(0, commentPrefix + "Contents of " + f.getCanonicalPath());
                            }
                            fileContents.add(FileOperations.NEWLINE);
                            FileUtils.writeLines(new File(directory, "merge_" + extension + outputFilenameSuffix), fileContents, FileOperations.NEWLINE, true);
                        }
                    }
                }
            } else {
                HashMap<String, ArrayList<String>> filesToMerge = new HashMap();
                Files.walkFileTree(Paths.get(mergeDirectory.getCanonicalPath()), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        File f = file.toFile();
                        String parentDirectory = f.getParent();
                        if (FileOperations.isAcceptableFormat(filter, f.getName(), extensionFilterType)) {
                            if (!filesToMerge.containsKey(parentDirectory)) {
                                filesToMerge.put(parentDirectory, new ArrayList());
                            }
                            filesToMerge.get(parentDirectory).add(file.toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                });

                // Iterating through each file in a directory
                for (Entry<String, ArrayList<String>> entry : filesToMerge.entrySet()) {
                    String directory = entry.getKey();
                    ArrayList<String> files = entry.getValue();

                    // Sorting files in merge order
                    Collections.sort(files, getFileComparator(sortOrder, mergeOrder));

                    // Merging files
                    for (String file : files) {
                        File f = new File(file);
                        List<String> fileContents = FileUtils.readLines(f, "UTF-8");
                        if (commentRequired) {
                            fileContents.add(0, commentPrefix + "Contents of " + f.getCanonicalPath());
                        }
                        fileContents.add(FileOperations.NEWLINE);
                        FileUtils.writeLines(new File(directory, "merge_all" + outputFilenameSuffix), fileContents, FileOperations.NEWLINE, true);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void execute() throws Exception {
        try {
            if (mergeType.equals("Merge In Place")) {
                this.mergeUnderParents();
            } else if (mergeType.equals("Merge Under Source Directory")) {
                this.mergeUnderSource();
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
