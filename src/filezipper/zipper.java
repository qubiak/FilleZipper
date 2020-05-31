package filezipper;


import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class zipper extends JFrame {

    public zipper() {
        this.setTitle("Zipper");
        this.setBounds(275, 300, 250, 250);
        this.setJMenuBar(pasekMenu);
        Image iconImage = Toolkit.getDefaultToolkit().getImage("pakowanie.png");
        this.setIconImage(iconImage);

        JMenu menuPlik = pasekMenu.add(new JMenu("Plik"));

        Action akcjaDodawania = new Akcja("Dodaj", "Dodaj nowy wpis do archiwum", "ctrl D", new ImageIcon("dodaj.png"));
        Action akcjaUsuwania = new Akcja("Usuń", "Usuń zaznaczony/zaznaczone wpisy z archiwum", "ctrl U", new ImageIcon("usun.png"));
        Action akcjaZipowania = new Akcja("Zip", "Zipuj", "ctrl Z");
        Action akcjaUnzipowania = new Akcja("Unzip", "Odzipuj", "ctrl O");

        JMenuItem menuOtworz = menuPlik.add(akcjaDodawania);
        JMenuItem menuUsun = menuPlik.add(akcjaUsuwania);
        JMenuItem menuZip = menuPlik.add(akcjaZipowania);
        JMenuItem menuUnzip = menuPlik.add(akcjaUnzipowania);

        bDodaj = new JButton(akcjaDodawania);
        bUsun = new JButton(akcjaUsuwania);
        bZip = new JButton(akcjaZipowania);
        bUnzip = new JButton(akcjaUnzipowania);
        JScrollPane scrollek = new JScrollPane(lista);

        lista.setBorder(BorderFactory.createEtchedBorder());
        GroupLayout layout = new GroupLayout(this.getContentPane());

        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(scrollek, 100, 150, Short.MAX_VALUE)
                        .addContainerGap(0, Short.MAX_VALUE)
                        .addGroup(
                                layout.createParallelGroup().addComponent(bDodaj, 90, 90, 90).addComponent(bUsun, 90, 90, 90).addComponent(bZip, 90, 90, 90).addComponent(bUnzip, 90, 90, 90))
        );

        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addComponent(scrollek, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup().addComponent(bDodaj).addComponent(bUsun).addGap(5, 40, Short.MAX_VALUE).addComponent(bZip).addComponent(bUnzip))
        );

        this.getContentPane().setLayout(layout);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.pack();
    }

    private DefaultListModel modelListy = new DefaultListModel() {
        @Override
        public void addElement(Object obj) {
            lista.add(obj);
            super.addElement(((File) obj).getName());
        }

        @Override
        public Object get(int index) {
            return lista.get(index);
        }

        @Override
        public Object remove(int index) {
            lista.remove(index);
            return super.remove(index);
        }

        ArrayList lista = new ArrayList();
    };
    private JList lista = new JList(modelListy);
    private JButton bDodaj;
    private JButton bUsun;
    private JButton bZip;
    private JButton bUnzip;
    private JMenuBar pasekMenu = new JMenuBar();
    private JFileChooser wybieracz = new JFileChooser();
    private JFileChooser wybieracz2 = new JFileChooser();

    public static void main(String[] args) {
        new zipper().setVisible(true);
    }

    private class Akcja extends AbstractAction {

        public Akcja(String nazwa, String opis, String klawiaturowySkrot) {
            this.putValue(Action.NAME, nazwa);
            this.putValue(Action.SHORT_DESCRIPTION, opis);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(klawiaturowySkrot));
        }

        public Akcja(String nazwa, String opis, String klawiaturowySkrot, Icon ikona) {
            this(nazwa, opis, klawiaturowySkrot);
            this.putValue(Action.SMALL_ICON, ikona);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Dodaj")) {
                dodajWpisyDoArchiwum();
            } else if (e.getActionCommand().equals("Usuń")) {
                usuwanieWpisowZList();
            } else if (e.getActionCommand().equals("Zip")) {
                stworzArchiwumZip();
            } else if (e.getActionCommand().equals("Unzip")) {
                rozpakujArchiwumZip();
            }

        }

        private void dodajWpisyDoArchiwum() {
            wybieracz.setCurrentDirectory(new File(System.getProperty("user.dir")));
            wybieracz.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            wybieracz.setMultiSelectionEnabled(true);

            int tmp = wybieracz.showDialog(rootPane, "Dodaj do archiwum");

            if (tmp == JFileChooser.APPROVE_OPTION) {
                File[] sciezki = wybieracz.getSelectedFiles();

                for (int i = 0; i < sciezki.length; i++) {
                    if (!czyWpisSiePowtarza(sciezki[i].getPath())) {
                        modelListy.addElement(sciezki[i]);
                    }
                }

            }
        }

        private boolean czyWpisSiePowtarza(String testowanyWpis) {
            for (int i = 0; i < modelListy.getSize(); i++) {
                if (((File) modelListy.get(i)).getPath().equals(testowanyWpis)) {
                    return true;
                }
            }

            return false;
        }

        private void usuwanieWpisowZList() {
            int[] tmp = lista.getSelectedIndices();

            for (int i = 0; i < tmp.length; i++) {
                modelListy.remove(tmp[i] - i);
            }
        }

        private void stworzArchiwumZip() {
            wybieracz.setCurrentDirectory(new File(System.getProperty("user.dir")));
            wybieracz.setSelectedFile(new File(System.getProperty("user.dir") + File.separator + "mojanazwa.zip"));
            int tmp = wybieracz.showDialog(rootPane, "Kompresuj");

            if (tmp == JFileChooser.APPROVE_OPTION) {
                byte tmpData[] = new byte[BUFFOR];
                try {
                    ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(wybieracz.getSelectedFile()), BUFFOR));

                    for (int i = 0; i < modelListy.getSize(); i++) {
                        if (!((File) modelListy.get(i)).isDirectory()) {
                            zipuj(zOutS, (File) modelListy.get(i), tmpData, ((File) modelListy.get(i)).getPath());
                        } else {
                            wypiszSciezki((File) modelListy.get(i));

                            for (int j = 0; j < listaSciezek.size(); j++) {
                                zipuj(zOutS, (File) listaSciezek.get(j), tmpData, ((File) modelListy.get(i)).getPath());
                            }

                            listaSciezek.removeAll(listaSciezek);
                        }

                    }

                    zOutS.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        private void zipuj(ZipOutputStream zOutS, File sciezkaPliku, byte[] tmpData, String sciezkaBazowa) throws IOException {
            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(sciezkaPliku), BUFFOR);

            zOutS.putNextEntry(new ZipEntry(sciezkaPliku.getPath().substring(sciezkaBazowa.lastIndexOf(File.separator) + 1)));

            int counter;
            while ((counter = inS.read(tmpData, 0, BUFFOR)) != -1) {
                zOutS.write(tmpData, 0, counter);
            }

            zOutS.closeEntry();

            inS.close();
        }

        public static final int BUFFOR = 1024;

        private void wypiszSciezki(File nazwaSciezki) {
            String[] nazwyPlikowIKatalogow = nazwaSciezki.list();

            for (int i = 0; i < nazwyPlikowIKatalogow.length; i++) {
                File p = new File(nazwaSciezki.getPath(), nazwyPlikowIKatalogow[i]);

                if (p.isFile()) {
                    listaSciezek.add(p);
                }

                if (p.isDirectory()) {
                    wypiszSciezki(new File(p.getPath()));
                }

            }
        }

        ArrayList listaSciezek = new ArrayList();

        private void rozpakujArchiwumZip() {

            wybieracz2.setCurrentDirectory(new File(System.getProperty("user.dir")));
            wybieracz2.setSelectedFile(new File(System.getProperty("user.dir") + File.separator + "mojanazwa.zip"));
            int tmp = wybieracz2.showDialog(rootPane, "Rozpakuj");
            wybieracz2.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            wybieracz2.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("ZIP file", "zip");
            wybieracz2.addChoosableFileFilter(filter);

            File selectedFile = wybieracz2.getSelectedFile();

            if (tmp == JFileChooser.APPROVE_OPTION) {
                File katalog = wybieracz2.getCurrentDirectory();

                ZipEntry wpis = null;

                byte[] tmpData = new byte[1024];
                try {
                    if (!katalog.exists()) {
                        katalog.mkdir();
                    }

                    ZipInputStream zInS = new ZipInputStream(new FileInputStream(selectedFile));

                    while ((wpis = zInS.getNextEntry()) != null) {
                        BufferedOutputStream fOutS = new BufferedOutputStream(new FileOutputStream(katalog + File.separator + wpis.getName()));

                        int counter;
                        while ((counter = zInS.read(tmpData, 0, BUFFOR)) != -1) {
                            fOutS.write(tmpData, 0, BUFFOR);
                        }

                        fOutS.close();
                        zInS.closeEntry();
                    }

                    zInS.close();

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

            }
        }

    }
}