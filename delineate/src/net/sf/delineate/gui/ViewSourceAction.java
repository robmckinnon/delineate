/*
 * ViewSourceAction.java
 *
 * Copyright (C) 2003 Robert McKinnon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.delineate.gui;

import org.apache.batik.util.MimeTypeConstants;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.svg.SVGDocument;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.io.InputStream;
import java.io.Reader;

/**
 * To view the source of the current document.
 */
public class ViewSourceAction extends AbstractAction {

    private SVGDocument svgDocument;
    private int x;
    private int y;
    private final JFrame frame = new JFrame();
    private final JTextArea textArea = new JTextArea();

    public ViewSourceAction() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(textArea);
        frame.setContentPane(scrollPane);
    }

    public void setSvgDocument(SVGDocument svgDocument) {
        this.svgDocument = svgDocument;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void closeFrame() {
        synchronized(frame) {
            if(frame != null) {
                frame.setVisible(false);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("View source action performed");
        final ParsedURL url = new ParsedURL(svgDocument.getURL());

        new Thread() {
            public void run() {
                char[] buffer = new char[4096];

                try {
                    InputStream inputStream = url.openStream(MimeTypeConstants.MIME_TYPES_SVG);
                    Reader reader = XMLUtilities.createXMLDocumentReader(inputStream);
                    int length;

                    final Document document = new PlainDocument();

                    while((length = reader.read(buffer, 0, buffer.length)) != -1) {
                        document.insertString(document.getLength(), new String(buffer, 0, length), null);
                    }

                    reader.close();
                    inputStream.close();

                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            synchronized(frame) {
                                textArea.setDocument(document);
                                textArea.setEditable(false);

                                frame.setTitle(url.toString());
                                frame.setBounds(x, y, 600, 200);
                                frame.setVisible(true);
                            }
                        }
                    });
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }
}
