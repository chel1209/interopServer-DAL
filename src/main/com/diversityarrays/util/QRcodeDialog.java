/*
 * dalserver-interop library - implementation of DAL server for interoperability
 * Copyright (C) 2015  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRcodeDialog extends JDialog {
	
	private Action copyAction = new AbstractAction("Copy URL") {
		@Override
		public void actionPerformed(ActionEvent e) {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(deviceUrl), null);
		}
	};
	
	private Action closeAction = new AbstractAction("Close") {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};

	private String deviceUrl;
	
	public QRcodeDialog(Window owner, String title, String url, ModalityType modalityType) {
		super(owner, title, modalityType);
		
		this.deviceUrl = url;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		QRCodeWriter writer = null;
		
		writer = new QRCodeWriter();
		
		BufferedImage image = null;
		Exception err = null;
		try {
			BitMatrix encoded = writer.encode(deviceUrl, BarcodeFormat.QR_CODE, 200, 200);
			
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(encoded, "png", os);
			os.close();
			
			image = ImageIO.read(new ByteArrayInputStream(os.toByteArray()));
		} catch (WriterException e) {
			err = e;
		} catch (IOException e) {
			err = e;
		}				

		
		Box top = Box.createHorizontalBox();
		top.add(new JLabel(deviceUrl));
		top.add(Box.createHorizontalStrut(20));
		top.add(new JButton(copyAction));
		
		Container cp = getContentPane();
		cp.add(top, BorderLayout.NORTH);
		
		if (err==null) {
			cp.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
		}
		else {
			JTextArea ta = new JTextArea("Unable to create QR code:\n\n"+err.getMessage());
			ta.setEditable(false);
			cp.add(new JScrollPane(ta), BorderLayout.CENTER);
		}
		
		cp.add(new JButton(closeAction), BorderLayout.SOUTH);
		
		pack();
	}
}
