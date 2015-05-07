import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class ModManager extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel titleLabel;
    private JScrollPane contentScroll;

    private JTable table;
    private File file;
    private ObjectMapper mapper;
    private ModList modList;

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
            modList.modList.get(i).enbabled = (Boolean)table.getValueAt(i, 0);
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
        boolean exists = false;

        String executionPath = System.getProperty("user.dir");
        this.file = new File(executionPath+"/"+"mods.json");

        try {
            if(!file.exists()) this.file.createNewFile();

            //Create the mapper and read in the mods.json file.
            mapper = new ObjectMapper();
            this.modList =  mapper.readValue(this.file, ModList.class);

            //Open the mods folder or create one.
            File modMasterDir = new File(executionPath + "/" + "mods");
            if(!modMasterDir.exists()) modMasterDir.mkdir();

            //For each directory inside the mods folder, read the info.json if it exists.
            for(File modDir : modMasterDir.listFiles()){
                if(!modDir.isDirectory())
                    continue;

                //Try to get the info.json.
                File infoFile = new File(modDir+"/info.json");
                if(!infoFile.exists()) continue;

                //Read in the info.json
                ModInfo modInfo = mapper.readValue(infoFile, ModInfo.class);

                //Check if it already exists.
                for(Mod mod : modList.modList){
                    if(mod.modName.equals(modInfo.name)){
                        exists = true;
                        mod.exists = true;
                        break;
                    }
                }

                //If it didn't exist, add it!.
                if(!exists)
                    modList.modList.add(new Mod(modInfo.name, false, true));

                //Reset the flag.
                exists = false;
            }

            //Check over all the mods. If they do not exist/were not recently added, remove from the mod list.
            for(int i=0;i<modList.modList.size();i++){
                Mod mod = modList.modList.get(i);
                if(!mod.exists){
                    modList.modList.remove(i);
                    i--;
                }
            }

            Object[][] data = new Object[modList.modList.size()][3];

            for(int i=0; i<modList.modList.size();i++){
                Mod mod = modList.modList.get(i);
                data[i][0] = mod.enbabled;
                data[i][1] = mod.modName;
                data[i][2] = "Version: 0.1";
            }

            String[] columnNames = {"Enabled", "Name", "Version"};

            this.table = new JTable(new CheckboxTable(data, columnNames));

            //list.setVisible(true);
            //box.setPreferredSize(new Dimension(200, 400));

            contentScroll.setViewportView(table);
            pack();

            contentScroll.revalidate();
            contentScroll.repaint();
            System.out.println("Such as");

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

    public static class ModList{
        @JsonProperty("modList")
        public ArrayList<Mod> modList;

        public ModList(){

        }

        public ModList(ArrayList<Mod> modList){
            this.modList = modList;
        }
    }

    public static class Mod{
        @JsonProperty("modName")
        public String modName;
        @JsonProperty("enabled")
        public boolean enbabled;
        @JsonIgnore
        public boolean exists = false;

        public Mod() {
        }

        public Mod(String modName, boolean enbabled) {
            this.modName = modName;
            this.enbabled = enbabled;
        }

        public Mod(String modName, boolean enbabled, boolean exists) {
            this.modName = modName;
            this.enbabled = enbabled;
            this.exists = exists;
        }
    }

    public static class ModInfo{
        @JsonProperty("name")
        public String name;

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
