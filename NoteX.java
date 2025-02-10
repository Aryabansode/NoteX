import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteX extends Frame implements ActionListener, TextListener {

    private TextArea textArea;
    private File currentFile = null;
    private boolean isModified = false;
    private boolean darkModeEnabled = false;

    // Default (light mode) colors
    private Color lightBg = Color.white;
    private Color lightFg = Color.black;
    // Dark mode colors
    private Color darkBg = Color.DARK_GRAY;
    private Color darkFg = Color.WHITE;

    public NoteX() {
        // Set up the Frame
        super("NoteX");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Set custom icon (Logo for NoteX Editor)
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image icon = toolkit.getImage("D:/Arya/Arya23/NoteX.jpg"); // Ensure the file path is correct or replace with
                                           // actual image file
        setIconImage(icon);

        // Create the text area
        textArea = new TextArea();
        textArea.addTextListener(this);
        add(textArea, BorderLayout.CENTER);

        // Create Menu Bar
        MenuBar menuBar = new MenuBar();

        // --- File Menu ---
        Menu fileMenu = new Menu("File");
        addMenuItem(fileMenu, "New");
        addMenuItem(fileMenu, "Open");
        addMenuItem(fileMenu, "Save");
        addMenuItem(fileMenu, "Save As");
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit");
        menuBar.add(fileMenu);

        // --- Edit Menu ---
        Menu editMenu = new Menu("Edit");
        addMenuItem(editMenu, "Cut");
        addMenuItem(editMenu, "Copy");
        addMenuItem(editMenu, "Paste");
        addMenuItem(editMenu, "Select All");
        addMenuItem(editMenu, "Time/Date");
        menuBar.add(editMenu);

        // --- Format Menu ---
        Menu formatMenu = new Menu("Format");
        addMenuItem(formatMenu, "Choose Font");
        menuBar.add(formatMenu);

        // --- View Menu ---
        Menu viewMenu = new Menu("View");
        addMenuItem(viewMenu, "Toggle Dark Mode");
        menuBar.add(viewMenu);

        setMenuBar(menuBar);

        // Add window listener to handle closing the window
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (checkSave()) {
                    dispose();
                }
            }
        });

        setVisible(true);
    }

    // Helper method to add a menu item and register the action listener.
    private void addMenuItem(Menu menu, String label) {
        MenuItem item = new MenuItem(label);
        item.addActionListener(this);
        menu.add(item);
    }

    // Check if current document is modified. If yes, prompt the user to save
    // changes.
    private boolean checkSave() {
        if (!isModified) {
            return true;
        }
        int option = showConfirmDialog("The document has been modified. Do you want to save changes?");
        if (option == 1) { // Yes
            return saveFile();
        } else if (option == 2) { // No
            return true;
        }
        // Cancelled
        return false;
    }

    // A simple modal confirmation dialog that returns 1 for Yes, 2 for No, 0 for
    // Cancel.
    private int showConfirmDialog(String message) {
        Dialog d = new Dialog(this, "Confirm", true);
        d.setLayout(new BorderLayout());
        Label msg = new Label(message);
        d.add(msg, BorderLayout.CENTER);

        Panel btnPanel = new Panel();
        btnPanel.setLayout(new FlowLayout());
        Button yes = new Button("Yes");
        Button no = new Button("No");
        Button cancel = new Button("Cancel");
        btnPanel.add(yes);
        btnPanel.add(no);
        btnPanel.add(cancel);
        d.add(btnPanel, BorderLayout.SOUTH);

        final int[] result = new int[1]; // 1 = Yes, 2 = No, 0 = Cancel

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String cmd = ae.getActionCommand();
                if (cmd.equals("Yes")) {
                    result[0] = 1;
                } else if (cmd.equals("No")) {
                    result[0] = 2;
                } else {
                    result[0] = 0;
                }
                d.dispose();
            }
        };
        yes.addActionListener(al);
        no.addActionListener(al);
        cancel.addActionListener(al);

        d.setSize(350, 120);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        return result[0];
    }

    // Save current text to file. Returns true if saved successfully.
    private boolean saveFile() {
        if (currentFile == null) {
            return saveFileAs();
        }
        try (FileWriter writer = new FileWriter(currentFile)) {
            writer.write(textArea.getText());
            isModified = false;
            setTitle("NoteX - " + currentFile.getName());
            return true;
        } catch (IOException e) {
            showErrorDialog("Error saving file: " + e.getMessage());
            return false;
        }
    }

    // Save As dialog.
    private boolean saveFileAs() {
        FileDialog fd = new FileDialog(this, "Save As", FileDialog.SAVE);
        fd.setVisible(true);
        String directory = fd.getDirectory();
        String file = fd.getFile();
        if (directory == null || file == null) {
            return false; // cancelled
        }
        currentFile = new File(directory, file);
        return saveFile();
    }

    // Open file dialog.
    private void openFile() {
        if (!checkSave()) {
            return;
        }
        FileDialog fd = new FileDialog(this, "Open", FileDialog.LOAD);
        fd.setVisible(true);
        String directory = fd.getDirectory();
        String file = fd.getFile();
        if (directory == null || file == null) {
            return; // cancelled
        }
        currentFile = new File(directory, file);
        try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            textArea.setText(sb.toString());
            isModified = false;
            setTitle("NoteX - " + currentFile.getName());
        } catch (IOException e) {
            showErrorDialog("Error opening file: " + e.getMessage());
        }
    }

    // Show an error dialog
    private void showErrorDialog(String message) {
        Dialog d = new Dialog(this, "Error", true);
        d.setLayout(new BorderLayout());
        Label msg = new Label(message);
        d.add(msg, BorderLayout.CENTER);
        Button ok = new Button("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                d.dispose();
            }
        });
        Panel panel = new Panel();
        panel.add(ok);
        d.add(panel, BorderLayout.SOUTH);
        d.setSize(300, 100);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    // Toggle between dark mode and light mode.
    private void toggleDarkMode() {
        darkModeEnabled = !darkModeEnabled;
        if (darkModeEnabled) {
            textArea.setBackground(darkBg);
            textArea.setForeground(darkFg);
        } else {
            textArea.setBackground(lightBg);
            textArea.setForeground(lightFg);
        }
    }

    // Custom implementation for clipboard copy operation.
    private void doCopy() {
        String selected = textArea.getSelectedText();
        if (selected != null && !selected.isEmpty()) {
            StringSelection data = new StringSelection(selected);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(data, data);
        }
    }

    // Custom implementation for clipboard cut operation.
    private void doCut() {
        int start = textArea.getSelectionStart();
        int end = textArea.getSelectionEnd();
        if (start != end) {
            doCopy();
            textArea.replaceRange("", start, end);
        }
    }

    // Custom implementation for clipboard paste operation.
    private void doPaste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String pasteData = (String) contents.getTransferData(DataFlavor.stringFlavor);
                int pos = textArea.getCaretPosition();
                textArea.insert(pasteData, pos);
            } catch (Exception ex) {
                showErrorDialog("Error pasting from clipboard: " + ex.getMessage());
            }
        }
    }

    // Insert current date/time at caret position.
    private void insertDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a MM/dd/yyyy");
        String dateTime = sdf.format(new Date());
        int pos = textArea.getCaretPosition();
        textArea.insert(dateTime, pos);
    }

    // Display a simple Font chooser dialog and set the selected font.
    private void chooseFont() {
        Dialog fontDialog = new Dialog(this, "Choose Font", true);
        fontDialog.setLayout(new BorderLayout());
        fontDialog.setSize(400, 300);
        fontDialog.setLocationRelativeTo(this);

        // Panel for font settings
        Panel panel = new Panel(new GridLayout(4, 2, 5, 5));

        // Font Family
        Label lblFamily = new Label("Font Family:");
        Choice familyChoice = new Choice();
        // Get available font families
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String font : fonts) {
            familyChoice.add(font);
        }
        panel.add(lblFamily);
        panel.add(familyChoice);

        // Font Style
        Label lblStyle = new Label("Font Style:");
        Choice styleChoice = new Choice();
        styleChoice.add("Plain");
        styleChoice.add("Bold");
        styleChoice.add("Italic");
        styleChoice.add("Bold Italic");
        panel.add(lblStyle);
        panel.add(styleChoice);

        // Font Size
        Label lblSize = new Label("Font Size:");
        TextField sizeField = new TextField("12", 5);
        panel.add(lblSize);
        panel.add(sizeField);

        // Preview
        Label lblPreview = new Label("Preview:");
        Label previewLabel = new Label("Sample Text");
        panel.add(lblPreview);
        panel.add(previewLabel);

        fontDialog.add(panel, BorderLayout.CENTER);

        // Panel for OK/Cancel buttons
        Panel btnPanel = new Panel(new FlowLayout());
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");
        btnPanel.add(okButton);
        btnPanel.add(cancelButton);
        fontDialog.add(btnPanel, BorderLayout.SOUTH);

        // Listeners to update the preview when selections change
        ItemListener updatePreview = new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                updatePreviewFont(familyChoice, styleChoice, sizeField, previewLabel);
            }
        };
        familyChoice.addItemListener(updatePreview);
        styleChoice.addItemListener(updatePreview);
        sizeField.addTextListener(new TextListener() {
            public void textValueChanged(TextEvent te) {
                updatePreviewFont(familyChoice, styleChoice, sizeField, previewLabel);
            }
        });
        // Update preview initially
        updatePreviewFont(familyChoice, styleChoice, sizeField, previewLabel);

        final Font[] selectedFont = new Font[1]; // To capture the font chosen

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    String family = familyChoice.getSelectedItem();
                    int style = getFontStyle(styleChoice.getSelectedItem());
                    int size = Integer.parseInt(sizeField.getText().trim());
                    selectedFont[0] = new Font(family, style, size);
                    fontDialog.dispose();
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid font size.");
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                selectedFont[0] = null;
                fontDialog.dispose();
            }
        });

        fontDialog.setVisible(true);

        if (selectedFont[0] != null) {
            textArea.setFont(selectedFont[0]);
        }
    }

    // Helper method to update preview label font.
    private void updatePreviewFont(Choice familyChoice, Choice styleChoice, TextField sizeField, Label previewLabel) {
        try {
            String family = familyChoice.getSelectedItem();
            int style = getFontStyle(styleChoice.getSelectedItem());
            int size = Integer.parseInt(sizeField.getText().trim());
            Font font = new Font(family, style, size);
            previewLabel.setFont(font);
        } catch (Exception e) {
            // Ignore parsing errors until valid
        }
    }

    // Convert style string to Font style constant.
    private int getFontStyle(String styleStr) {
        if (styleStr.equalsIgnoreCase("Bold")) {
            return Font.BOLD;
        } else if (styleStr.equalsIgnoreCase("Italic")) {
            return Font.ITALIC;
        } else if (styleStr.equalsIgnoreCase("Bold Italic")) {
            return Font.BOLD | Font.ITALIC;
        }
        return Font.PLAIN;
    }

    // --- Handle Menu Actions ---
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();

        switch (cmd) {
            case "New":
                if (checkSave()) {
                    textArea.setText("");
                    currentFile = null;
                    isModified = false;
                    setTitle("Notepad Clone");
                }
                break;
            case "Open":
                openFile();
                break;
            case "Save":
                saveFile();
                break;
            case "Save As":
                saveFileAs();
                break;
            case "Exit":
                if (checkSave()) {
                    dispose();
                }
                break;
            case "Cut":
                doCut();
                break;
            case "Copy":
                doCopy();
                break;
            case "Paste":
                doPaste();
                break;
            case "Select All":
                textArea.selectAll();
                break;
            case "Time/Date":
                insertDateTime();
                break;
            case "Choose Font":
                chooseFont();
                break;
            case "Toggle Dark Mode":
                toggleDarkMode();
                break;
            default:
                break;
        }
    }

    // --- Text Listener ---
    public void textValueChanged(TextEvent te) {
        isModified = true;
    }

    // --- Main ---
    public static void main(String[] args) {
        // Run on the AWT event-dispatching thread.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NoteX();
            }
        });
    }
}