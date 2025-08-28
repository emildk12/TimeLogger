import javax.swing.*;
import java.awt.event.*;
import java.awt.Font;
import java.awt.LayoutManager;
import javax.swing.event.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.print.*;
import java.util.ArrayList;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.plaf.LayerUI;
import java.awt.Color;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.awt.geom.Ellipse2D;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.geom.Path2D;
import java.awt.geom.Line2D;

import java.awt.*;

import javax.swing.text.StyleConstants;
import javax.swing.text.Style;

// javac -Xlint:unchecked -cp ".;jxl.jar" Employees.java Gui.java Excel.java
// java -cp ".;jxl.jar" Gui 0

// jar -cfm TimeLogger.jar Manifest.txt *.class

// Må ha ansatte.txt på format: [ansatt];[Fodselsnummer]\n
// Kan spesifisere output directory i en fil ut_mappe.txt på format: [path]
// feks: C:\Java\

public class Gui {
    JFrame frame;
    final int WIDTH = 290;
    final int HEIGHT = 400;
    static final String TITLE = "TimeLogger";
    static final Font font = new Font("Arial", Font.PLAIN, 20);;

    JPanel panel;
    GridBagConstraints constraints;

    JLabel labelFrom;
    JLabel labelTo;
    JLabel labelEmployees;

    JTextField textFieldFrom;
    JTextField textFieldTo;

    JTable table;

    JButton confrimButton;

    JScrollPane listScroller;
    JList list;

    static String[] arg;

    public static void main(String[] args) throws Exception {
        arg = args;
        int code = Employees.readEmployees();
        Gui gui = new Gui();
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                gui.createUI();
            }
        });
        if (code != 0) {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    gui.createErrorMessage("ansatte.txt ikke funnet, vennligst lag en fil med format:\n[ansatte];[Fodselsnummer]");
                }
            });
        }
    }

    private void createUI() {
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(new GridBagLayout());

        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.LINE_START;

        int inset = 10;
        constraints.insets = new Insets(0, inset, 0, 0);

        labelFrom = createLabel("Fra", 0, 0);
        labelTo = createLabel("Til", 1, 0);
        labelEmployees = createLabel("Ansatte", 0, 2);

        textFieldFrom = createTextField("8:00", 0, 1);
        textFieldTo = createTextField("16:00", 1, 1);

        final CheckmarkLayerUI layerUI = new CheckmarkLayerUI();
        JPanel checkmarkPanel = new JPanel();
        JLayer<JPanel> jlayer = new JLayer<JPanel>(checkmarkPanel, layerUI);
        constraints.gridx = 1;
        constraints.gridy = 4;
        jlayer.setPreferredSize(new Dimension(40, 25));
        jlayer.setMinimumSize(new Dimension(40, 25));
        panel.add(jlayer, constraints);

        confrimButton = new JButton("Bekreft");
        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(confrimButton, constraints);
        confrimButton.addActionListener(new MyActionListener(layerUI));

        list = new JList<String>(Employees.getEmployees());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setFont(font);

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 200));
        listScroller.setMinimumSize(new Dimension(100, 85));
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.gridwidth = 2;
        panel.add(listScroller, constraints);

        frame.add(panel);
        frame.pack();
        frame.setSize(WIDTH, HEIGHT);
        frame.setVisible(true);
    }

    private JLabel createLabel(String content, int xPos, int yPos) {
        JLabel label = new JLabel(content);
        label.setFont(font);
        constraints.gridx = xPos;
        constraints.gridy = yPos;
        panel.add(label, constraints);
        return label;

    }
    private JTextField createTextField(String content, int xPos, int yPos) {
        JTextField textField = new JTextField(content);
        textField.setFont(font);
        textField.setMinimumSize(new Dimension(70, 30));
        textField.setPreferredSize(new Dimension(70, 30));
        constraints.gridx = xPos;
        constraints.gridy = yPos;
        panel.add(textField, constraints);
        return textField;
    }

    class MyActionListener implements ActionListener {
        // ikke bra? føles som jeg hopper gjennom mange hoops og gjør mye invklet for å få til at
        // PropertyChangeEvent funker i layerui
        private CheckmarkLayerUI layerUI;
        public MyActionListener(CheckmarkLayerUI l) {
            layerUI = l;
        }
        public void actionPerformed(ActionEvent event) {

            String selectedValue = (String)list.getSelectedValue();
            String fromTimeString = textFieldFrom.getText();
            String toTimeString = textFieldTo.getText();
            if(selectedValue == null) {
                createErrorMessage("Ingen ansatt valgt! Vennligst velg en ansatt");
            }
            else if(!checkFormat(fromTimeString) || !checkFormat(toTimeString)) {
                createErrorMessage("Feil format! Vennligst skriv tiden i formatet: (t)t:mm feks:\n8:00");
            }
            else {
                float fromTime = convertStringTimeToFloatTime(fromTimeString);
                float toTime = convertStringTimeToFloatTime(toTimeString);
                if(toTime < fromTime) {
                    createErrorMessage("Ugyldig fra/til tid! Til tid er mindre enn fra tid!");
                }
                else {
                    int code = Excel.writeTime(selectedValue, fromTime, toTime);
                    // burde kanskje hatt en observer her
                    if (code == -1) {
                        createErrorMessage("FEIL! Kunne ikke skrive til excel dokumentet!");
                    }
                    else {
                        layerUI.setValidateDrawCheckmark(true);
                        Timer timer = new Timer(3000, new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                layerUI.setValidateDrawCheckmark(false);
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();
                    }
                }
            }
        }
    }

    class CheckmarkLayerUI extends LayerUI<JPanel> {

        Boolean validateDrawCheckmark = false;

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);

            Graphics2D g2 = (Graphics2D)g.create();
            if (validateDrawCheckmark) {
                drawCheckMark(g2, c);
            }
        }
        private void drawCheckMark(Graphics2D g2, JComponent c) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            int s = 15;
            int w = c.getWidth();
            int h = c.getHeight();
            int x = w/3;
            int y = h - 6;
            g2.setPaint(new Color(70, 190, 70));
            g2.setStroke(new BasicStroke(6.0F));
            g2.drawLine(x, y, x + s, y - s);
            g2.drawLine(x - s/2, y - s/2, x, y);

            g2.dispose();
        }
        @Override
        public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
            if ("validateDrawCheckmark".equals(pce.getPropertyName())) {
                l.repaint();
            }
        }
        public void setValidateDrawCheckmark(Boolean b) {
            validateDrawCheckmark = b;
            firePropertyChange("validateDrawCheckmark", !b, b);
        }
    }
    // kunne/burde vært lagt i en utils class?
    private boolean checkFormat(String in) {
        Pattern pattern = Pattern.compile("^\\d{1,2}:\\d{2}$");
        Matcher matcher = pattern.matcher(in);
        return matcher.find();
    }
    // kunne/burde vært lagt i en utils class?
    private float convertStringTimeToFloatTime(String in) {
        String[] arr = in.split(":");
        float out = Float.parseFloat(arr[0]) + Float.parseFloat(arr[1])/60;
        return out;
    }

    private void createErrorMessage(String msg) {
        JFrame errorFrame = new JFrame(TITLE + " - Error");
        JPanel panel = new JPanel();
        panel = new JPanel(new GridBagLayout());

        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;

        int inset = 10;
        constraints.insets = new Insets(0, inset, 0, 0);

        JTextArea textPane = new JTextArea();
        textPane.setText(msg);
        textPane.setEditable(false);
        textPane.setFont(font);
        textPane.setLineWrap(true);
        textPane.setWrapStyleWord(true);
        textPane.setBackground(panel.getBackground());
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weighty = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(textPane, constraints);

        constraints.fill = GridBagConstraints.NONE;

        final ErrorLayerUI layerUI = new ErrorLayerUI();
        JPanel errorPanel = new JPanel();
        JLayer<JPanel> jlayer = new JLayer<JPanel>(errorPanel, layerUI);
        jlayer.setPreferredSize(new Dimension(40, 35));
        jlayer.setMinimumSize(new Dimension(40, 35));
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(jlayer, constraints);

        JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                errorFrame.dispatchEvent(new WindowEvent(errorFrame, WindowEvent.WINDOW_CLOSING));
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.weighty = 0;
        constraints.insets = new Insets(0, 0, inset, 0);
        panel.add(button, constraints);

        errorFrame.add(panel);
        errorFrame.setSize(460,200);
        errorFrame.setVisible(true);
    }
    class ErrorLayerUI extends LayerUI<JPanel> {
        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);

            Graphics2D g2 = (Graphics2D)g.create();
            drawErrorTriangle(g2, c);
        }
        private void drawErrorTriangle(Graphics2D g2, JComponent c) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            Path2D polygon = new Path2D.Double();
            double topX = c.getWidth()/2;
            double topY = 0;
            polygon.moveTo(topX, topY);
            polygon.lineTo(topX + 20, topY + 35);
            polygon.lineTo(topX - 20, topY + 35);
            polygon.closePath();
            g2.setColor(Color.RED);
            g2.fill(polygon);
            g2.draw(polygon);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(6.0F));
            g2.draw(new Line2D.Double(topX, topY + 11, topX, topY + 21));
            g2.draw(new Line2D.Double(topX, topY + 29, topX, topY + 30));
            g2.dispose();
        }
    }
}
