/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.Utilities;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Usuario
 */
public class Interfaz extends javax.swing.JFrame {

    public String RutaActual = "";
    private LineNumber lineNumber;
    private boolean isNightMode;

    /**
     * Creates new form Interfaz
     */
    public Interfaz() {
        initComponents();
        inicializar();
        colors();
    }

    private void inicializar() {
        setTitle("Nuevo archivo");
        lineNumber = new LineNumber(this.TextAreaCodigo);
        this.jScrollPane8.setRowHeaderView(this.lineNumber);
        this.jCheckBoxTheme.setSelected(false);
        this.isNightMode = false;
        this.TextAreaCodigo.addCaretListener(new CaretListener() {
            // Each time the caret is moved, it will trigger the listener and its method caretUpdate.
            // It will then pass the event to the update method including the source of the event (which is our textarea control)
            @Override
            public void caretUpdate(CaretEvent e) {
                int dot = e.getDot();
                int line;
                try {
                    line = getLineOfOffset(TextAreaCodigo, dot);
                    int positionInLine = dot - getLineStartOffset(TextAreaCodigo, line);
                    jLabelLine.setText("linea: " + (line + 1) + ", columna: " + (positionInLine + 1));
                } catch (BadLocationException ex) {
                    Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    static int getLineOfOffset(JTextComponent comp, int offset) throws BadLocationException {
        Document doc = comp.getDocument();
        if (offset < 0) {
            throw new BadLocationException("Can't translate offset to line", -1);
        } else if (offset > doc.getLength()) {
            throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
        } else {
            Element map = doc.getDefaultRootElement();
            return map.getElementIndex(offset);
        }
    }

    static int getLineStartOffset(JTextComponent comp, int line) throws BadLocationException {
        Element map = comp.getDocument().getDefaultRootElement();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= map.getElementCount()) {
            throw new BadLocationException("No such line", comp.getDocument().getLength() + 1);
        } else {
            Element lineElem = map.getElement(line);
            return lineElem.getStartOffset();
        }
    }

    //METODO PARA ENCONTRAR LAS ULTIMAS CADENAS
    private int findLastNonWordChar(String text, int index) {
        while (--index >= 0) {
            //  \\W = [A-Za-Z0-9]
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
        }
        return index;
    }

    //METODO PARA ENCONTRAR LAS PRIMERAS CADENAS 
    private int findFirstNonWordChar(String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }

    //METODO PARA PINTAS LAS PALABRAS RESEVADAS
    private void colors() {

        final StyleContext cont = StyleContext.getDefaultStyleContext();

        //COLORES 
        final AttributeSet attred = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(255, 0, 35));
        final AttributeSet attgreen = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(0, 255, 54));
        final AttributeSet attblue = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(0, 147, 255));
        final AttributeSet attpink = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(255, 192, 203));
        final AttributeSet attblack = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(0, 0, 0));
        final AttributeSet attgray = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, new Color(155, 155, 155));
        final AttributeSet attOperadores = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.cyan);
        final AttributeSet attwhite = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.WHITE);
        //STYLO 
        DefaultStyledDocument doc = new DefaultStyledDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offset);
                if (before < 0) {
                    before = 0;
                }
                int after = findFirstNonWordChar(text, offset + str.length());
                int wordL = before;
                int wordR = before;

                while (wordR <= after) {
                    if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {
                        if (text.substring(wordL, wordR).matches("(\\W)*(if|IF|else|ELSE|end|END|do|DO|while|WHILE|repeat|REPEAT|until|UNTIL|cin|cout)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attblue, true);
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(int|INT|real|REAL|boolean|BOOLEAN)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attgreen, true);
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(\\d+$)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attred, true);
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(true|TRUE|false|FALSE)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attpink, true);
                        } else if (text.substring(wordL, wordR).matches("[-+*/=]")) {
                            setCharacterAttributes(wordL, wordR - wordL, attOperadores, true);
                        } else {
                            MutableAttributeSet attrs = new SimpleAttributeSet();
                            setCharacterAttributes(wordL, wordR - wordL, attrs, true);
                        }
                        wordL = wordR;
                    }
                    wordR++;
                }
                //DETECTAR COMETARIOS
                Pattern singleLinecommentsPattern = Pattern.compile("\\/\\/.*");
                Matcher matcher = singleLinecommentsPattern.matcher(text);

                while (matcher.find()) {
                    setCharacterAttributes(matcher.start(),
                            matcher.end() - matcher.start(), attgray, false);
                }

                Pattern multipleLinecommentsPattern = Pattern.compile("\\/\\*.*?\\*\\/",
                        Pattern.DOTALL);
                matcher = multipleLinecommentsPattern.matcher(text);

                while (matcher.find()) {
                    setCharacterAttributes(matcher.start(),
                            matcher.end() - matcher.start(), attgray, false);
                }
            }

            public void romeve(int offs, int len) throws BadLocationException {
                super.remove(offs, len);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offs);
                if (before < 0) {
                    before = 0;
                }
            }

        };

        JTextPane txt = new JTextPane(doc);
        String temp = this.TextAreaCodigo.getText();
        this.TextAreaCodigo.setStyledDocument(txt.getStyledDocument());
        this.TextAreaCodigo.setText(temp);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabelLine = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaResultados = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaErrores = new javax.swing.JTextArea();
        jTabbedPane7 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaLexico = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextAreaSintacticp = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextAreaSemantico = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextAreaCodigoIntermedio = new javax.swing.JTextArea();
        jScrollPane8 = new javax.swing.JScrollPane();
        TextAreaCodigo = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jCheckBoxTheme = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabelLine.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelLine.setForeground(new java.awt.Color(255, 255, 255));
        jLabelLine.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelLine.setText("Bienvenido");
        jLabelLine.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jLabelLine, gridBagConstraints);

        jPanel2.setBackground(new java.awt.Color(153, 153, 153));

        jTextAreaResultados.setEditable(false);
        jTextAreaResultados.setColumns(20);
        jTextAreaResultados.setRows(5);
        jTextAreaResultados.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane2.setViewportView(jTextAreaResultados);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1312, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1288, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 138, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Resultado", jPanel2);

        jPanel3.setBackground(new java.awt.Color(153, 153, 153));
        jPanel3.setAlignmentX(0.0F);
        jPanel3.setAlignmentY(0.0F);
        jPanel3.setAutoscrolls(true);

        jTextAreaErrores.setEditable(false);
        jTextAreaErrores.setColumns(20);
        jTextAreaErrores.setRows(5);
        jTextAreaErrores.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane3.setViewportView(jTextAreaErrores);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1312, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1288, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 138, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Errores", jPanel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1202;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 12, 13, 12);
        jPanel1.add(jTabbedPane1, gridBagConstraints);

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));

        jTextAreaLexico.setEditable(false);
        jTextAreaLexico.setBackground(new java.awt.Color(102, 0, 102));
        jTextAreaLexico.setColumns(20);
        jTextAreaLexico.setFont(new java.awt.Font("Monospaced", 0, 18)); // NOI18N
        jTextAreaLexico.setForeground(new java.awt.Color(255, 255, 255));
        jTextAreaLexico.setRows(5);
        jTextAreaLexico.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane4.setViewportView(jTextAreaLexico);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 533, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 506, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jTabbedPane7.addTab("Lexico", jPanel4);

        jPanel5.setBackground(new java.awt.Color(153, 153, 153));

        jTextAreaSintacticp.setEditable(false);
        jTextAreaSintacticp.setColumns(20);
        jTextAreaSintacticp.setRows(5);
        jTextAreaSintacticp.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane5.setViewportView(jTextAreaSintacticp);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 533, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 506, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jTabbedPane7.addTab("Sintactico", jPanel5);

        jPanel6.setBackground(new java.awt.Color(153, 153, 153));

        jTextAreaSemantico.setEditable(false);
        jTextAreaSemantico.setColumns(20);
        jTextAreaSemantico.setRows(5);
        jTextAreaSemantico.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane6.setViewportView(jTextAreaSemantico);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 533, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 506, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jTabbedPane7.addTab("Semantico", jPanel6);

        jPanel7.setBackground(new java.awt.Color(153, 153, 153));

        jTextAreaCodigoIntermedio.setEditable(false);
        jTextAreaCodigoIntermedio.setColumns(20);
        jTextAreaCodigoIntermedio.setRows(5);
        jTextAreaCodigoIntermedio.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane7.setViewportView(jTextAreaCodigoIntermedio);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 533, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 506, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jTabbedPane7.addTab("Codigo Intermedio", jPanel7);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 417;
        gridBagConstraints.ipady = 416;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(13, 12, 0, 12);
        jPanel1.add(jTabbedPane7, gridBagConstraints);

        TextAreaCodigo.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        TextAreaCodigo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        TextAreaCodigo.setMinimumSize(new java.awt.Dimension(255, 255));
        TextAreaCodigo.setPreferredSize(new java.awt.Dimension(286, 116));
        TextAreaCodigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TextAreaCodigoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TextAreaCodigoKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                TextAreaCodigoKeyTyped(evt);
            }
        });
        jScrollPane8.setViewportView(TextAreaCodigo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 699;
        gridBagConstraints.ipady = 508;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(13, 12, 0, 0);
        jPanel1.add(jScrollPane8, gridBagConstraints);

        jMenuBar1.setBackground(new java.awt.Color(255, 255, 255));
        jMenuBar1.setPreferredSize(new java.awt.Dimension(291, 35));

        jMenu1.setText("Archivo");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Abrir");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Guardar");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Guardar Como...");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem4.setText("Cerrar");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Editar");
        jMenuBar1.add(jMenu2);

        jMenu3.setText("Formato");
        jMenuBar1.add(jMenu3);

        jMenu4.setText("Compilar");
        jMenuBar1.add(jMenu4);

        jMenu5.setText("Ayuda");
        jMenuBar1.add(jMenu5);

        jMenu6.setText("Tema");

        jCheckBoxTheme.setSelected(true);
        jCheckBoxTheme.setText("Modo noche");
        jCheckBoxTheme.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxThemeStateChanged(evt);
            }
        });
        jCheckBoxTheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxThemeActionPerformed(evt);
            }
        });
        jMenu6.add(jCheckBoxTheme);

        jMenuBar1.add(jMenu6);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1341, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 758, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        JFileChooser selectorArchivos = new JFileChooser();
        selectorArchivos.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        selectorArchivos.showOpenDialog(this);
        AbrirTxt(selectorArchivos.getSelectedFile().getPath());
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    public void AbrirTxt(String Ruta) {
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        RutaActual = Ruta;
        try {
            // Apertura del fichero y creacion de BufferedReader para poder
            // hacer una lectura comoda (disponer del metodo readLine()).
            archivo = new File(Ruta);

            TextAreaCodigo.setText(getTextFile(archivo));
            setTitle(archivo.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if (RutaActual != "") {
            try {
                String ruta = RutaActual;

                File file = new File(ruta);
                // Si el archivo no existe es creado
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(TextAreaCodigo.getText());
                setTitle(file.getName());
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JFileChooser guardar = new JFileChooser();
            guardar.showSaveDialog(null);
            guardar.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File archivo = guardar.getSelectedFile();
            System.out.println(guardar.getSelectedFile().getPath());
            guardarFichero(TextAreaCodigo.getText(), archivo);
            RutaActual = guardar.getSelectedFile().getPath();
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    public String getTextFile(File file) {
        String text = "";
        try {

            BufferedReader entrada = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            while (true) {
                int b = entrada.read();
                if (b != -1) {
                    text += (char) b;
                } else {
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("El archivo no pudo ser encontrado... " + ex.getMessage());
            return null;
        } catch (IOException ex) {
            System.out.println("Error al leer el archivo... " + ex.getMessage());
            return null;
        }
        return text;
    }


    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        JFileChooser guardar = new JFileChooser();
        guardar.showSaveDialog(null);
        guardar.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        File archivo = guardar.getSelectedFile();
        System.out.println(guardar.getSelectedFile().getPath());
        RutaActual = guardar.getSelectedFile().getPath();
        guardarFichero(TextAreaCodigo.getText(), archivo);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        TextAreaCodigo.setText("");
        RutaActual = "";
        setTitle("Nuevo archivo");
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void TextAreaCodigoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TextAreaCodigoKeyReleased
        this.tecla(evt);
    }//GEN-LAST:event_TextAreaCodigoKeyReleased

    private void TextAreaCodigoKeyTyped(java.awt.event.KeyEvent evt){
        
    }

    private void TextAreaCodigoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TextAreaCodigoKeyPressed
    }//GEN-LAST:event_TextAreaCodigoKeyPressed

    private void jCheckBoxThemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxThemeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxThemeActionPerformed

    //change theme :)))))
    private void jCheckBoxThemeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxThemeStateChanged
        if (this.jCheckBoxTheme.isSelected()) {
            jMenuBar1.setUI(new BasicMenuBarUI() {
                public void paint(Graphics g, JComponent c) {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, c.getWidth(), c.getHeight());
                }
            });
            /*jTabbedPane1.setUI(new BasicTabbedPaneUI(){
             public void paint(Graphics g, JComponent c) {
             g.setColor(Color.DARK_GRAY);
             g.fillRect(0, 0, c.getWidth(), c.getHeight());
             }
             });*/
            //this.jPanel1.setBackground(Color.DARK_GRAY);

            this.TextAreaCodigo.setBackground(Color.DARK_GRAY);
            this.TextAreaCodigo.setForeground(Color.WHITE);
            this.TextAreaCodigo.setCaretColor(Color.WHITE);
            isNightMode = true;
            this.lineNumber.updateColor(isNightMode);
            this.jMenuBar1.setBackground(Color.black);
            jMenuBar1.setOpaque(true);

            this.jMenu1.setBackground(Color.DARK_GRAY);
            this.jMenu1.setForeground(Color.WHITE);
            jMenu1.setOpaque(true);
            this.jMenuItem1.setBackground(Color.DARK_GRAY);
            this.jMenuItem1.setForeground(Color.WHITE);
            jMenuItem1.setOpaque(true);

            this.jMenuItem2.setBackground(Color.DARK_GRAY);
            this.jMenuItem2.setForeground(Color.WHITE);
            jMenuItem2.setOpaque(true);
            this.jMenuItem3.setBackground(Color.DARK_GRAY);
            this.jMenuItem3.setForeground(Color.WHITE);
            jMenuItem3.setOpaque(true);

            this.jMenuItem4.setBackground(Color.DARK_GRAY);
            this.jMenuItem4.setForeground(Color.WHITE);
            jMenuItem4.setOpaque(true);

            this.jCheckBoxTheme.setBackground(Color.DARK_GRAY);
            this.jCheckBoxTheme.setForeground(Color.WHITE);
            jCheckBoxTheme.setOpaque(true);

            this.jMenu2.setBackground(Color.DARK_GRAY);
            this.jMenu2.setForeground(Color.WHITE);
            jMenu2.setOpaque(true);
            this.jMenu3.setBackground(Color.DARK_GRAY);
            this.jMenu3.setForeground(Color.WHITE);
            jMenu3.setOpaque(true);
            this.jMenu4.setBackground(Color.DARK_GRAY);
            this.jMenu4.setForeground(Color.WHITE);
            jMenu4.setOpaque(true);
            this.jMenu5.setBackground(Color.DARK_GRAY);
            this.jMenu5.setForeground(Color.WHITE);
            jMenu5.setOpaque(true);
            this.jMenu6.setBackground(Color.DARK_GRAY);
            this.jMenu6.setForeground(Color.WHITE);

            jMenu6.setOpaque(true);
        } else {
            jMenuBar1.setUI(new BasicMenuBarUI() {
                public void paint(Graphics g, JComponent c) {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, c.getWidth(), c.getHeight());
                }
            });
            this.TextAreaCodigo.setBackground(Color.WHITE);
            this.TextAreaCodigo.setForeground(Color.BLACK);
            this.TextAreaCodigo.setCaretColor(Color.BLACK);
            isNightMode = false;
            this.lineNumber.updateColor(isNightMode);
            this.jMenuBar1.setBackground(Color.black);
            jMenuBar1.setOpaque(true);

            this.jMenu1.setBackground(Color.WHITE);
            this.jMenu1.setForeground(Color.BLACK);
            jMenu1.setOpaque(true);
            this.jMenuItem1.setBackground(Color.WHITE);
            this.jMenuItem1.setForeground(Color.BLACK);
            jMenuItem1.setOpaque(true);

            this.jMenuItem2.setBackground(Color.WHITE);
            this.jMenuItem2.setForeground(Color.BLACK);
            jMenuItem2.setOpaque(true);
            this.jMenuItem3.setBackground(Color.WHITE);
            this.jMenuItem3.setForeground(Color.BLACK);
            jMenuItem3.setOpaque(true);

            this.jMenuItem4.setBackground(Color.WHITE);
            this.jMenuItem4.setForeground(Color.BLACK);
            jMenuItem4.setOpaque(true);

            this.jCheckBoxTheme.setBackground(Color.WHITE);
            this.jCheckBoxTheme.setForeground(Color.BLACK);
            jCheckBoxTheme.setOpaque(true);

            this.jMenu2.setBackground(Color.WHITE);
            this.jMenu2.setForeground(Color.BLACK);
            jMenu2.setOpaque(true);
            this.jMenu3.setBackground(Color.WHITE);
            this.jMenu3.setForeground(Color.BLACK);
            jMenu3.setOpaque(true);
            this.jMenu4.setBackground(Color.WHITE);
            this.jMenu4.setForeground(Color.BLACK);
            jMenu4.setOpaque(true);
            this.jMenu5.setBackground(Color.WHITE);
            this.jMenu5.setForeground(Color.BLACK);
            jMenu5.setOpaque(true);
            this.jMenu6.setBackground(Color.WHITE);
            this.jMenu6.setForeground(Color.BLACK);

            jMenu6.setOpaque(true);
        }
    }//GEN-LAST:event_jCheckBoxThemeStateChanged

    private void tecla(java.awt.event.KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        if ((keyCode >= 65 && keyCode <= 90) || (keyCode >= 48 && keyCode <= 57)
                || (keyCode >= 97 && keyCode <= 122) || (keyCode != 27 && !(keyCode >= 37
                && keyCode <= 40) && !(keyCode >= 16
                && keyCode <= 18) && keyCode != 524
                && keyCode != 20)) {

            if (!getTitle().contains("*")) {
                setTitle(getTitle() + "*");
            }
        }
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

            PythonInterpreter interpreter = new PythonInterpreter();

            String[] argumentos = TextAreaCodigo.getText().split("\r?\n");
            String ArgumentosString = "[";
            for (int i = 0; i < argumentos.length; i++) {
                ArgumentosString += ("'" + argumentos[i] + "'");
                if (i == (argumentos.length - 1)) {

                } else {
                    ArgumentosString += ",";
                }
            }
            ArgumentosString += "]";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            interpreter.setOut(printStream);
            interpreter.exec(
                    "import sys\n"
                    + "sys.argv = " + ArgumentosString);
            interpreter.execfile("C:\\Users\\Lenovo\\Documents\\JAVA\\Compilador\\Compilador\\read.py");

            // Obtener la salida del script como una cadena de texto
            String output = outputStream.toString();

            jTextAreaLexico.setText(output);
        }
    }

    /**
     * @param args the command line arguments
     */
    public void guardarFichero(String cadena, File archivo) {

        FileWriter escribir;
        try {

            escribir = new FileWriter(archivo, true);
            escribir.write(cadena);
            escribir.close();
            setTitle(archivo.getName());
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar, ponga nombre al archivo");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar, en la salida");
        }
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interfaz().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane TextAreaCodigo;
    private javax.swing.JCheckBoxMenuItem jCheckBoxTheme;
    private javax.swing.JLabel jLabelLine;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane7;
    private javax.swing.JTextArea jTextAreaCodigoIntermedio;
    private javax.swing.JTextArea jTextAreaErrores;
    private javax.swing.JTextArea jTextAreaLexico;
    private javax.swing.JTextArea jTextAreaResultados;
    private javax.swing.JTextArea jTextAreaSemantico;
    private javax.swing.JTextArea jTextAreaSintacticp;
    // End of variables declaration//GEN-END:variables
}
