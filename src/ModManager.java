import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModManager extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel titleLabel;
    private JScrollPane contentScroll;

    private JTable table;
    private File file;
    private ObjectMapper mapper;
    private List<Mod> modList = new ArrayList<Mod>();

    public ModManager() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        fillContents();
    }

    private void onOK() {
        for(int i=0;i<table.getRowCount();i++){
            modList.get(i).enabled = (Boolean)table.getValueAt(i, 0);
        }

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, modList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void fillContents(){
        boolean exists = false, error = false;

        String executionPath = System.getProperty("user.dir");
        this.file = new File(executionPath+"/"+"mods.json");

        try {
            if(!file.exists()) this.file.createNewFile();
            mapper = new ObjectMapper(); //Create the mapper and read in the mods.json file.

            try {
                this.modList = Arrays.asList(mapper.readValue(this.file, Mod[].class));

                //Open the mods folder or create one.
                File modMasterDir = new File(executionPath + "/" + "mods");
                if(!modMasterDir.exists()) modMasterDir.mkdir();

                //For each directory inside the mods folder, read the info.json if it exists.
                for(File modDir : modMasterDir.listFiles()){
                    if(!modDir.isDirectory()) continue;

                    //Try to get the info.json.
                    File infoFile = new File(modDir+"/info.json");
                    if(!infoFile.exists()) continue;

                    //Read in the info.json
                    ModInfo modInfo = mapper.readValue(infoFile, ModInfo.class);

                    //Check if it already exists.
                    for(Mod mod : modList)
                        if(mod.modName.equals(modInfo.name)){
                            exists = true;
                            mod.exists = true;
                            mod.version = modInfo.version;
                            mod.description = modInfo.description;
                            break;
                        }
                    //If it didn't exist, add it!.
                    if(!exists) modList.add(new Mod(modInfo.name, false, modInfo.version, modInfo.description, true));
                    //Reset the flag.
                    exists = false;
                }

                //Check over all the mods. If they do not exist/were not recently added, remove from the mod list.
                for(int i=0;i<modList.size();i++){
                    Mod mod = modList.get(i);
                    if(!mod.exists){
                        modList.remove(i);
                        i--;
                    }
                }
            }catch(JsonMappingException e){
                e.printStackTrace();
                error = true;
            }

            String[] columnNames;
            Object[][] data;

            //If there was an error, display an error.
            if(error){
                data = new Object[1][1];
                data[0][0] = "Error reading mods.json";
                columnNames = new String[1];
                columnNames[0] = "";

            //Otherwise, continue normally.
            }else{
                int size = modList.size() > 0 ? modList.size() : 1;
                data = new Object[size][3];
                columnNames = new String[3];

                data[0][0] = data[0][1] = data[0][2] = "No mods found";

                //For each Mod in the list, assign the data.
                for(int i=0; i<modList.size();i++){
                    Mod mod = modList.get(i);
                    data[i][0] = mod.enabled;
                    data[i][1] = mod.modName;
                    data[i][2] = mod.version;
                }

                //Column names!
                columnNames[0] = "Enabled";
                columnNames[1] = "Name";
                columnNames[2] = "Version";
            }

            this.table = new JTable(new CheckboxTable(data, columnNames));
            contentScroll.setViewportView(table);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ModManager dialog = new ModManager();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

    }

    public static class Mod{
        @JsonProperty("modName")
        public String modName;
        @JsonProperty("enabled")
        public boolean enabled;

        @JsonIgnore
        public String version = "";
        @JsonIgnore
        public String description = "";
        @JsonIgnore
        public boolean exists = false;

        public Mod() {
        }

        public Mod(String modName, boolean enabled, String version, String description, boolean exists) {
            this.modName = modName;
            this.enabled = enabled;
            this.version = version;
            this.description = description;
            this.exists = exists;
        }
    }

    public static class ModInfo{
        @JsonProperty("name")
        public String name="";
        @JsonProperty("version")
        public String version="";
        @JsonProperty("description")
        public String description="";

        public ModInfo(){
        }

    }

    class CheckboxTable extends AbstractTableModel {

        Object rowData[][] = { { "1", Boolean.TRUE }, { "2", Boolean.TRUE }, { "3", Boolean.FALSE },
                { "4", Boolean.TRUE }, { "5", Boolean.FALSE }, };

        String columnNames[] = { "English", "Boolean" };

        public CheckboxTable(Object[][] data, String[] columnNames){
            this.rowData = data;
            this.columnNames = columnNames;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public int getRowCount() {
            return rowData.length;
        }

        public Object getValueAt(int row, int column) {
            return rowData[row][column];
        }

        public Class getColumnClass(int column) {
            return (getValueAt(0, column).getClass());
        }

        public void setValueAt(Object value, int row, int column) {
            rowData[row][column] = value;
        }

        public boolean isCellEditable(int row, int column) {
            return (column == 0);
        }
    }
}
