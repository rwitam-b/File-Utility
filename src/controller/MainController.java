package controller;

import common.FileOperations;
import common.ScreenValidations;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import processor.FileCopy;
import processor.FileCorrector;
import processor.FileListing;
import processor.FileMerge;
import processor.FileSearch;

public class MainController implements Initializable {

    @FXML
    private ToggleButton fileListExtensionToggle, fileCorrectorExtensionToggle, fileMergeExtensionToggle;
    @FXML
    private Button fileCopyButton, fileCorrectorButton, fileListButton, fileSearchButton, fileMergeButton;
    @FXML
    private TextField fileCopyPath, fileCorrectorPath, fileCorrectorExtensions, fileListPath, fileListExtensions, fileSearchPath, fileMergePath, fileMergeExtensions;
    @FXML
    private TextArea fileCopyList, fileCopyLog, fileCorrectorLog, fileListLog, fileSearchList, fileSearchLog, fileMergeLog;
    @FXML
    private ChoiceBox fileListType, fileListSort, fileCorrectorRename, fileMergeType, fileMergeOrder;
    @FXML
    private CheckBox fileListToDisk, fileCorrectorTrim, fileCorrectorBlanks, fileCopyPreserveStructure, fileMergeComment, fileMergeSegregate;
    @FXML
    private Label fileCorrectorRenameExt;
    @FXML
    private RadioButton fileMergeSortAsc, fileMergeSortDesc;

    private String renameExtension = "";
    private String mergeComment = "";

    // Helper Function to clear contents of TextArea objects
    private void clearTextArea(TextArea subject) {
        subject.clear();
    }

    @FXML
    private void fileListButtonClick() {
        try {
            clearTextArea(fileListLog);

            // Getting screen inputs
            String path = fileListPath.getText().trim();
            String extensionFilters[] = fileListExtensions.getText().trim().split(",");
            char listType = fileListType.getValue().toString().charAt(0);
            char sortType = fileListSort.getValue().toString().charAt(0);
            boolean writeToDisk = fileListToDisk.isSelected();
            boolean extensionFilterType = fileListExtensionToggle.isSelected();

            // Validation for inputs
            try {
                ScreenValidations.directoryPathTextField(fileListPath, "Input Directory", true);
                ScreenValidations.extensionFilterTextField(fileListExtensions, "Extension Filters", false);
            } catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Initialization Failed!");
                a.setContentText(e.getMessage());
                a.show();
                throw e;
            }

            // Starting background task of listing files
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                FileListing FL = new FileListing(fileListLog, path, listType, sortType, extensionFilters, extensionFilterType, writeToDisk);
                                FL.execute();
                                Platform.runLater(() -> {
                                    fileListLog.appendText(FileOperations.NEWLINE + FileOperations.NEWLINE + "Done Listing All Files!" + FileOperations.NEWLINE);
                                });
                            } catch (Exception e) {
                                Platform.runLater(() -> {
                                    fileListLog.appendText("Failed to perform operation!" + FileOperations.NEWLINE + e.getMessage() + FileOperations.NEWLINE);
                                });
                            }
                            return null;
                        }
                    };
                }
            };

            service.setOnRunning(event -> {
                fileListButton.setText("Processing");
                fileListButton.setDisable(true);
                fileListPath.setDisable(true);
                fileListExtensions.setDisable(true);
                fileListType.setDisable(true);
                fileListSort.setDisable(true);
                fileListToDisk.setDisable(true);
            });

            service.setOnSucceeded(event -> {
                fileListButton.setText("List Files");
                fileListButton.setDisable(false);
                fileListPath.setDisable(false);
                fileListExtensions.setDisable(false);
                fileListType.setDisable(false);
                fileListSort.setDisable(false);
                fileListToDisk.setDisable(false);
            });
            service.start();

        } catch (Exception e) {
        }
    }

    @FXML
    private void fileCorrectorButtonClick() {
        try {
            clearTextArea(fileCorrectorLog);

            // Getting screen inputs
            String sourcePath = fileCorrectorPath.getText().trim();
            String extensionFilters[] = fileCorrectorExtensions.getText().trim().split(",");
            boolean trimSpaces = fileCorrectorTrim.isSelected();
            boolean removeBlanks = fileCorrectorBlanks.isSelected();
            boolean extensionFilterType = fileCorrectorExtensionToggle.isSelected();
            char renameType = fileCorrectorRename.getValue().toString().charAt(0);

            // Validation for inputs
            try {
                if (!trimSpaces && !removeBlanks && renameType == 'D') {
                    throw new Exception("You have not selected any operation to be performed!");
                }
                ScreenValidations.directoryPathTextField(fileCorrectorPath, "Source Directory", true);
                ScreenValidations.extensionFilterTextField(fileCorrectorExtensions, "Extension Filters", false);
            } catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Initialization Failed!");
                a.setContentText(e.getMessage());
                a.show();
                throw e;
            }

            // Starting background task of correcting files
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                FileCorrector FC = new FileCorrector(fileCorrectorLog, sourcePath, extensionFilters, extensionFilterType, trimSpaces, removeBlanks, renameType, renameExtension);
                                FC.execute();
                                Platform.runLater(() -> {
                                    fileCorrectorLog.appendText(FileOperations.NEWLINE + FileOperations.NEWLINE + "Done Modifying All Files!" + FileOperations.NEWLINE);
                                });
                            } catch (Exception e) {
                                Platform.runLater(() -> {
                                    fileCorrectorLog.appendText("Failed to perform operation!" + FileOperations.NEWLINE + e.getMessage() + FileOperations.NEWLINE);
                                });
                            }
                            return null;
                        }
                    };
                }
            };

            service.setOnRunning(e -> {
                fileCorrectorButton.setText("Processing");
                fileCorrectorButton.setDisable(true);
                fileCorrectorPath.setDisable(true);
                fileCorrectorExtensions.setDisable(true);
                fileCorrectorTrim.setDisable(true);
                fileCorrectorRename.setDisable(true);
                fileCorrectorBlanks.setDisable(true);
            });

            service.setOnSucceeded(e -> {
                fileCorrectorButton.setText("Process Files");
                fileCorrectorButton.setDisable(false);
                fileCorrectorPath.setDisable(false);
                fileCorrectorExtensions.setDisable(false);
                fileCorrectorTrim.setDisable(false);
                fileCorrectorRename.setDisable(false);
                fileCorrectorBlanks.setDisable(false);
            });
            service.start();
        } catch (Exception e) {
        }
    }

    @FXML
    private void fileCopyButtonClick() {
        try {
            clearTextArea(fileCopyLog);

            // Getting screen inputs
            String fileList = fileCopyList.getText();
            String destinationPath = fileCopyPath.getText();
            boolean preserve = fileCopyPreserveStructure.isSelected();

            // Validation for inputs
            try {
                ScreenValidations.directoryPathTextField(fileCopyPath, "Destination Directory", false);
                ScreenValidations.regularTextArea(fileCopyList, "Source File List", true);
                File temp = new File(destinationPath);
                if (!temp.exists() && !temp.mkdirs()) {
                    FileOperations.checkDiskWriteAccess(destinationPath);
                }
            } catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Initialization Failed!");
                a.setContentText(e.getMessage());
                a.show();
                throw e;
            }

            String filePaths[] = fileList.split("\\R+");

            // Starting background task of correcting files
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                FileCopy FC = new FileCopy(fileCopyLog, destinationPath, filePaths, preserve);
                                FC.execute();
                                Platform.runLater(() -> {
                                    fileCopyLog.appendText(FileOperations.NEWLINE + FileOperations.NEWLINE + "Done Copying All Files!" + FileOperations.NEWLINE);
                                });
                            } catch (Exception e) {
                                Platform.runLater(() -> {
                                    fileCopyLog.appendText("Failed to perform operation!" + FileOperations.NEWLINE + e.getMessage() + FileOperations.NEWLINE);
                                });
                            }
                            return null;
                        }

                    };
                }
            };

            service.setOnRunning(event -> {
                fileCopyButton.setText("Copying");
                fileCopyButton.setDisable(true);
                fileCopyList.setDisable(true);
                fileCopyPath.setDisable(true);
                fileCopyPreserveStructure.setDisable(true);
            });

            service.setOnSucceeded(event -> {
                fileCopyButton.setText("Start Copy");
                fileCopyButton.setDisable(false);
                fileCopyList.setDisable(false);
                fileCopyPath.setDisable(false);
                fileCopyPreserveStructure.setDisable(false);
            });
            service.start();

        } catch (Exception e) {
        }
    }

    @FXML
    private void fileSearchButtonClick() {
        try {
            clearTextArea(fileSearchLog);

            // Getting screen inputs
            String fileList = fileSearchList.getText();
            String searchPath = fileSearchPath.getText();

            // Validation for inputs
            try {
                ScreenValidations.directoryPathTextField(fileSearchPath, "Search Directory", true);
                ScreenValidations.regularTextArea(fileSearchList, "Source File List", true);
                FileOperations.checkDiskWriteAccess(searchPath);
            } catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Initialization Failed!");
                a.setContentText(e.getMessage());
                a.show();
                throw e;
            }

            // All Inputs Are Present
            String filePaths[] = fileList.split("\\R+");

            // Starting background task of searching files
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                FileSearch FS = new FileSearch(fileSearchLog, filePaths, searchPath);
                                FS.execute();
                                Platform.runLater(() -> {
                                    fileSearchLog.appendText(FileOperations.NEWLINE + FileOperations.NEWLINE + "Done Copying All Files!" + FileOperations.NEWLINE);
                                });
                            } catch (Exception e) {
                                Platform.runLater(() -> {
                                    fileSearchLog.appendText("Failed to perform operation!" + FileOperations.NEWLINE + e.getMessage() + FileOperations.NEWLINE);
                                });
                            }
                            return null;
                        }
                    };
                }
            };

            service.setOnRunning(e -> {
                fileSearchButton.setText("Searching");
                fileSearchButton.setDisable(true);
                fileSearchList.setDisable(true);
                fileSearchPath.setDisable(true);
            });

            service.setOnSucceeded(e -> {
                fileSearchButton.setText("Search");
                fileSearchButton.setDisable(false);
                fileSearchList.setDisable(false);
                fileSearchPath.setDisable(false);
            });
            service.start();
        } catch (Exception e) {
        }
    }

    @FXML
    private void fileMergeButtonClick() {
        try {
            clearTextArea(fileMergeLog);

            // Getting screen inputs
            String mergePath = fileMergePath.getText().trim();
            String fileExtensions[] = fileMergeExtensions.getText().trim().split("\\R+");
            String mergeType = fileMergeType.getValue().toString();
            String sortOrder = fileMergeSortAsc.isSelected() ? FileOperations.SORT_ASC : fileMergeSortDesc.isSelected() ? FileOperations.SORT_DESC : "";
            String mergeOrder = fileMergeOrder.getValue().toString();
            boolean commentRequired = fileMergeComment.isSelected();
            boolean extensionFilterType = fileMergeExtensionToggle.isSelected();
            boolean seregationRequired = fileMergeSegregate.isSelected();

            // Validation for inputs
            try {
                ScreenValidations.directoryPathTextField(fileMergePath, "Source Directory", true);
                ScreenValidations.extensionFilterTextField(fileMergeExtensions, "Extension Filters", false);
                FileOperations.checkDiskWriteAccess(mergePath);
            } catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Initialization Failed!");
                a.setContentText(e.getMessage());
                a.show();
                throw e;
            }

            // Starting background task of searching files
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            try {
                                FileMerge FM = new FileMerge(mergePath, fileExtensions,extensionFilterType, mergeType, seregationRequired, commentRequired, mergeComment, sortOrder, mergeOrder, fileMergeLog);
                                FM.execute();
                                Platform.runLater(() -> {
                                    fileMergeLog.appendText(FileOperations.NEWLINE + FileOperations.NEWLINE + "Done Merging All Files!" + FileOperations.NEWLINE);
                                });
                            } catch (Exception e) {
                                Platform.runLater(() -> {
                                    fileMergeLog.appendText("Failed to perform operation!" + FileOperations.NEWLINE + e.getMessage() + FileOperations.NEWLINE);
                                });
                            }
                            return null;
                        }

                    };
                }
            };

            service.setOnRunning(e -> {
                fileMergeButton.setText("Processing");
                fileMergeButton.setDisable(true);
                fileMergePath.setDisable(true);
                fileMergeExtensions.setDisable(true);
                fileMergeType.setDisable(true);
            });

            service.setOnSucceeded(e -> {
                fileMergeButton.setText("Merge Files");
                fileMergeButton.setDisable(false);
                fileMergePath.setDisable(false);
                fileMergeExtensions.setDisable(false);
                fileMergeType.setDisable(false);
            });
            service.start();
        } catch (Exception e) {
        }
    }

    private void setExtensionFilter(TextField subject1, ToggleButton subject2) {
        if (subject1.getText().trim().isEmpty()) {
            subject2.setText("No Filters!");
            subject2.setStyle("-fx-background-color: #D3D3D3;");
            subject2.setSelected(true);
            subject2.setDisable(true);
        } else {
            subject2.setDisable(false);
            setExtensionFilterParameters(subject2);
        }
    }

    private void setExtensionFilterParameters(ToggleButton subject) {
        if (subject.isSelected()) {
            subject.setText("Include");
            subject.setStyle("-fx-background-color: #5cb85c;");
        } else {
            subject.setText("Exclude");
            subject.setStyle("-fx-background-color: #d9534f;");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Listing Initialization
        fileListType.setItems(FXCollections.observableArrayList("Absolute Paths", "Filenames"));
        fileListType.setValue("Absolute Paths");
        fileListSort.setItems(FXCollections.observableArrayList("Ascending", "Descending"));
        fileListSort.setValue("Ascending");
        fileListExtensions.setOnKeyReleased(event -> {
            setExtensionFilter(fileListExtensions, fileListExtensionToggle);
        });
        fileListExtensionToggle.setOnAction(event -> {
            setExtensionFilterParameters(fileListExtensionToggle);
        });

        // Modify Initializations
        fileCorrectorRename.setItems(FXCollections.observableArrayList("Don't Rename", "Lower Case", "Upper Case", "Extension Change"));
        fileCorrectorRename.setValue("Don't Rename");
        fileCorrectorRename.setOnAction(event -> {
            if (fileCorrectorRename.getValue().equals("Extension Change")) {
                TextInputDialog d = new TextInputDialog(renameExtension);
                d.setTitle("Input");
                d.setHeaderText("Rename to a different extension");
                d.setContentText("Enter Extension->");
                Optional<String> result = d.showAndWait();
                result.ifPresent(name -> renameExtension = name);
                fileCorrectorRenameExt.setText("Renaming to *." + renameExtension + " !");
            } else {
                renameExtension = "";
                fileCorrectorRenameExt.setText(renameExtension);
            }
        });
        fileCorrectorBlanks.setOnAction(event -> {
            if (fileCorrectorBlanks.isSelected()) {
                fileCorrectorTrim.setSelected(false);
                fileCorrectorTrim.setDisable(true);
            } else {
                fileCorrectorTrim.setDisable(false);
            }
        });
        fileCorrectorExtensions.setOnKeyReleased(event -> {
            setExtensionFilter(fileCorrectorExtensions, fileCorrectorExtensionToggle);
        });
        fileCorrectorExtensionToggle.setOnAction(event -> {
            setExtensionFilterParameters(fileCorrectorExtensionToggle);
        });

        // Merge Initializations
        fileMergeType.setItems(FXCollections.observableArrayList("Merge In Place", "Merge Under Source Directory"));
        fileMergeType.setValue("Merge In Place");
        fileMergeOrder.setItems(FXCollections.observableArrayList("Filename", "Last Modified", "Size"));
        fileMergeOrder.setValue("Filename");
        // Binding the Sort Order radio buttons to a single group for toggling
        final ToggleGroup group = new ToggleGroup();
        fileMergeSortAsc.setToggleGroup(group);
        fileMergeSortDesc.setToggleGroup(group);
        fileMergeComment.setOnAction(event -> {
            if (fileMergeComment.isSelected()) {
                TextInputDialog d = new TextInputDialog(renameExtension);
                d.setTitle("Input");
                d.setHeaderText("Put Filename In Comments");
                d.setContentText("Enter Comment Prefix (As Per File Type Being Merged)->");
                Optional<String> result = d.showAndWait();
                result.ifPresent(name -> mergeComment = name);
                if (mergeComment.trim().isEmpty()) {
                    fileMergeComment.setText("Add Filename Comment");
                } else {
                    fileMergeComment.setText("Filename Comment Prefix : '" + mergeComment + "'");
                }
            } else {
                mergeComment = "";
                fileMergeComment.setText("Add Filename Comment");
            }
        });
        fileMergeExtensions.setOnKeyReleased(event -> {
            setExtensionFilter(fileMergeExtensions, fileMergeExtensionToggle);
        });
        fileMergeExtensionToggle.setOnAction(event -> {
            setExtensionFilterParameters(fileMergeExtensionToggle);
        });
    }
}
