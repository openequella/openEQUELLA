package com.dytech.edge.importexport;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

@Deprecated
public class ErrorDialog extends JFlatDialog
{
	public ErrorDialog(Dialog parent, String text, Throwable t)
	{
		super(parent, "Error");

		JTextPane message = new JTextPane();
		message.setText(text);
		message.setEditable(false);
		message.setBorder(new EmptyBorder(0, 0, 10, 0));

		JTextArea error = new JTextArea();
		error.setEditable(false);
		error.setBorder(null);
		error.setTabSize(2);

		ByteArrayOutputStream trace = new ByteArrayOutputStream();
		t.printStackTrace(new PrintStream(trace));
		error.setText(trace.toString());

		final JScrollPane scroller = new JScrollPane(error);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setVisible(false);

		// Set the buttons' visibility:
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				dispose();
			}
		});

		final JButton jbAdvanced = new JButton("Advanced >>");
		jbAdvanced.setPreferredSize(new Dimension(75, 24));
		jbAdvanced.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				jbAdvanced.setVisible(false);
				scroller.setVisible(true);
				Dimension size = getSize();
				size.height += 200;
				size.width += 100;
				setSize(size);
				revalidate();

				jbAdvanced.setEnabled(false);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(jbAdvanced);
		buttonPanel.add(okButton);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(message);
		content.add(scroller);

		JPanel pane = (JPanel) getContentPane();
		pane.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		pane.setLayout(new BorderLayout());
		pane.add(content, BorderLayout.CENTER);
		pane.add(buttonPanel, BorderLayout.SOUTH);

		setModal(true);
		setBounds(200, 200, 300, 130);
		getRootPane().setDefaultButton(okButton);
	}
}