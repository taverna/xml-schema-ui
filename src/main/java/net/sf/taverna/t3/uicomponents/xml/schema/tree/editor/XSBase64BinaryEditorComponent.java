/**
 * *****************************************************************************
 * Copyright (C) 2014 Spanish National Bioinformatics Institute (INB),
 * Barcelona Supercomputing Center and The University of Manchester
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *****************************************************************************
 */

package net.sf.taverna.t3.uicomponents.xml.schema.tree.editor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.TreeCellEditor;
import javax.xml.bind.DatatypeConverter;

/**
 * @author Dmitry Repchevsky
 */

public class XSBase64BinaryEditorComponent extends XSTextAreaEditorComponent implements ClipboardOwner {
    private final Base64Component editor;

    public XSBase64BinaryEditorComponent(final TreeCellEditor cEditor) {
        super(cEditor);

        super.textComponent.setEditable(false);
        super.textComponent.setOpaque(false);
        
        Font font = super.textComponent.getFont();
        super.textComponent.setFont(new Font(Font.MONOSPACED, font.getStyle(), font.getSize()));

        // make selection color semitransparent
        Color color = new Color(super.textComponent.getSelectionColor().getRGB() & 0xB0FFFFFF, true);
        super.textComponent.setSelectionColor(color);

        InputMap map = textComponent.getInputMap(JComponent.WHEN_FOCUSED);

        Object paste_key = map.get(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
        if (paste_key == null) {
            paste_key = "paste";
        }

        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK), paste_key);

        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = XSBase64BinaryEditorComponent.super.textComponent.getToolkit().getSystemClipboard();
                Transferable content = clipboard.getContents(this);
                if (content != null && 
                    content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        String text = (String)content.getTransferData(DataFlavor.stringFlavor);
                        XSBase64BinaryEditorComponent.super.setEditorValue(text);
                    }
                    catch (Exception ex) {}
                }
            }
        };
        textComponent.getActionMap().put(paste_key, action);
        editor = new Base64Component();
    }

    @Override
    public JComponent getXSEditor() {
        return editor;
    }

    public Object parse(String value) throws IllegalArgumentException {
        try {
            DatatypeConverter.parseBase64Binary(value);
        } catch(Exception ex) {
            return null;
        }
        return value;
    }

    @Override public void lostOwnership(Clipboard clipboard, Transferable contents) {}

    public class Base64Component extends JLayeredPane implements DocumentListener, ClipboardOwner {
        private final static String LOAD_ICON = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAN9JREFUOMvN" +
                                                "kj0OgkAQhb9dlvgTWs6ALfegtLKy8A5yBLwDhQewpPIStnIGW0Iw4mKhJOsKSKfTbCZ57+2bNwO/" +
                                                "LmE2eUJmA4KYaJRAnpDdJBsb4GrSIRFhkidTShtwrZi7mrSLHMRE6tuML9HVtWJuOwNQY8Oy3elX" +
                                                "p9rgXE2qy+9CjWDvzDi2vQLomx+gLgiVx6kuCIWDv9hyOO9YtunLod9MsvI4dWFkEBO5mtQOySbX" +
                                                "BWFz55InZIstB4YcdJHNDAYPyakJ+sgAwsEXDev2uNTHbuVzRdJ439fAn9UDvAVi/mEsh3EAAAAA" +
                                                "SUVORK5CYII=";

        private final static String PASTE_ICON = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAK9JREFUOMut" +
                                                 "k1EKgzAMhr+FwqR4mV3Ed33eQTyNvnsRLzOKsFH2lJF11uq2Qmghzdc/SXPqe4Qflss52opJz+NC" +
                                                 "UwTYAADxdC8feZhosHg6NXXGwGCB4uk+HkolxcCgELtbWLEGucu7ADaFw4DHjYurmY8oeQO4mjmF" +
                                                 "3IXrWou1G7KlAOBcEVKz3SgqWEvF1smVFJQK+x8F40Kj31UEYvhCwdrAtBVTqZVuy7k1hbqe3yJO" +
                                                 "PFHjabIAAAAASUVORK5CYII=";

        private final JPanel buttons;

        private BufferedImage image;

        public Base64Component() {
            XSBase64BinaryEditorComponent.super.textComponent.getDocument().addDocumentListener(this);

            add(XSBase64BinaryEditorComponent.super.getXSEditor(), JLayeredPane.DEFAULT_LAYER);

            buttons = new JPanel(new GridLayout(1, 2));
            buttons.setOpaque(false);
            
            try {
                BufferedImage imgLoad = ImageIO.read(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(LOAD_ICON)));

                JButton bLoad = new JButton(new ImageIcon(imgLoad));

                bLoad.setToolTipText("import binary data from a file");

                bLoad.setRolloverIcon(new ImageIcon(this.getHighlightedImage(imgLoad)));
                bLoad.setRolloverEnabled(true);

                bLoad.setBorderPainted(false);
                bLoad.setContentAreaFilled(false);
                bLoad.setFocusable(false);

                bLoad.setBorder(null);

                bLoad.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();

                        chooser.setMultiSelectionEnabled(false);
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                        chooser.setDialogTitle("Load File");
                        chooser.setApproveButtonText("Load");
                        chooser.setApproveButtonToolTipText("Conver file to base64 text");

                        final int returnVal = chooser.showOpenDialog(Base64Component.this);

                        if(returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();

                            ByteArrayOutputStream out = new ByteArrayOutputStream((int)file.length());

                            try {
                                write(out, new BufferedInputStream(new FileInputStream(file)));
                                String base64 = DatatypeConverter.printBase64Binary(out.toByteArray());
                                XSBase64BinaryEditorComponent.super.setEditorValue(base64);
                            } catch(IOException ex) {
                                XSBase64BinaryEditorComponent.super.setEditorValue("");
                            }
                        }

                    }
                });

                buttons.add(bLoad);

                final Clipboard clipboard = getToolkit().getSystemClipboard();

                BufferedImage imgPaste = ImageIO.read(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(PASTE_ICON)));
                JButton bPaste= new JButton(new ImageIcon(imgPaste));

                bPaste.setToolTipText("paste base64 text from a clipboard (Ctrl+V)");

                bPaste.setRolloverIcon(new ImageIcon(this.getHighlightedImage(imgPaste)));
                bPaste.setRolloverEnabled(true);

                bPaste.setBorderPainted(false);
                bPaste.setContentAreaFilled(false);
                bPaste.setFocusable(false);

                bPaste.setBorder(null);

                bPaste.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Transferable content = clipboard.getContents(this);
                        if (content != null &&
                            content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            try {
                                String text = (String)content.getTransferData(DataFlavor.stringFlavor);

                                // check whether a pasted text is a valid base64 one (would cause IllegalargumentException otherwise)
                                DatatypeConverter.parseBase64Binary(text);

                                XSBase64BinaryEditorComponent.super.setEditorValue(text);
                            }
                            catch (Exception ex) {}
                        }
                    }
                });

                buttons.add(bPaste);
            }
            catch(IOException ex) {}

            add(buttons, JLayeredPane.PALETTE_LAYER);
        }

        @Override
        public Dimension getPreferredSize() {
            final int width = super.getPreferredSize().width;
            final int height = XSBase64BinaryEditorComponent.super.getXSEditor().getPreferredSize().height;

            return new Dimension(width, height);
        }

        @Override
        public void doLayout() {
            Insets ins = this.getInsets();
            Dimension dim = this.getSize();

            XSBase64BinaryEditorComponent.super.getXSEditor().setBounds(ins.left, ins.top, dim.width, dim.height);
            Dimension bSize = buttons.getPreferredSize();
            buttons.setBounds(Math.max(0, this.getPreferredSize().width - bSize.width), ins.top, bSize.width, bSize.height);
        }

        @Override
        public void paint(Graphics g) {
            final int width = this.getWidth();
            final int height = this.getHeight();

            g.setColor(XSBase64BinaryEditorComponent.super.textComponent.getBackground());
            
            g.fillRect(0, 0, width, height);

            if (image != null) {
                int iw = image.getWidth();
                int ih = image.getHeight();

                final float scale = Math.min((float)width / iw, (float)height / ih);

                if (scale < 1) {
                    iw = (int)(iw * scale);
                    ih = (int)(ih * scale);
                }

                Graphics2D g2d = (Graphics2D)g;

                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
                g2d.drawImage(image, 0, (height - ih)/2, iw, ih, null);
                g2d.setComposite(c);
            }
            super.paint(g);
        }

        private void write(OutputStream out, InputStream in) throws IOException {
            byte[] buf = new byte[1024];

            int n;
            while((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }

            out.flush();
        }

        private BufferedImage getHighlightedImage(BufferedImage image) {
            final int width = image.getWidth();
            final int height = image.getHeight();

            BufferedImage highlighted = new BufferedImage(width, height, image.getType());

            Graphics2D g2d = (Graphics2D)highlighted.getGraphics();

            g2d.drawImage(image, 1, 1, width, height, null);
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.drawImage(image, 1, 1, width, height, null);

            g2d.dispose();

            return highlighted;
        }

        private void setImage(String string) {
            try {
                byte[] b = DatatypeConverter.parseBase64Binary(string);
                image = ImageIO.read(new ByteArrayInputStream(b));
            } catch (IOException ex) {
                image = null;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            try {
                setImage(e.getDocument().getText(0, e.getLength()));
            }
            catch (BadLocationException ex) {}
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            try {
                setImage(e.getDocument().getText(WIDTH, WIDTH));
            }
            catch (BadLocationException ex) {}
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            try {
                setImage(e.getDocument().getText(WIDTH, WIDTH));
            }
            catch (BadLocationException ex) {}
        }

        @Override public void lostOwnership(Clipboard clipboard, Transferable contents) {}
    }
}
