package com.thelitblock.texteditor;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TerminalSetup {
    private static TextArea terminalOutput;
    private static TextField commandInput;
    private static int historyIndex = 0;
    private static final List<String> commandHistory = new ArrayList<>();

    public static void setupTerminal(TextArea terminalOutput, TextField commandInput) {
        TerminalSetup.terminalOutput = terminalOutput;
        TerminalSetup.commandInput = commandInput;

        terminalOutput.setEditable(false);
        terminalOutput.setWrapText(true);

        commandInput.setPromptText("Enter command and press Enter");

        commandInput.setOnAction(event -> {
            String command = commandInput.getText();
            executeCommand(command);
            commandHistory.add(command);
            historyIndex = commandHistory.size();
        });

        commandInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                if (historyIndex > 0) {
                    historyIndex--;
                    commandInput.setText(commandHistory.get(historyIndex));
                    commandInput.positionCaret(commandInput.getText().length());
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (historyIndex < commandHistory.size() - 1) {
                    historyIndex++;
                    commandInput.setText(commandHistory.get(historyIndex));
                    commandInput.positionCaret(commandInput.getText().length());
                }
                else {
                    historyIndex = commandHistory.size();
                    commandInput.clear();
                }
            }
        });
    }

    public static VBox createTerminalPane(TextArea terminalOutput, TextField commandInput) {
        setupTerminal(terminalOutput, commandInput);

        VBox terminalBox = new VBox(10);
        terminalBox.getChildren().addAll(terminalOutput, commandInput);
        VBox.setVgrow(terminalOutput, Priority.ALWAYS);
        return terminalBox;
    }

    private static void executeCommand(String command) {
        terminalOutput.appendText("> " + command + "\n");
        try {
            ProcessBuilder builder;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", command);
            }
            else {
                builder = new ProcessBuilder("bash", "-c", command);
            }

            Process process = builder.start();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String s;
            while ((s = stdInput.readLine()) != null) {
                terminalOutput.appendText(s + "\n");
            }
            while ((s = stdError.readLine()) != null) {
                terminalOutput.appendText("ERROR: " + s + "\n");
            }
        }
        catch (IOException e) {
            terminalOutput.appendText("ERROR: " + e.getMessage() + "\n");
        }
        commandInput.clear();
    }
}
