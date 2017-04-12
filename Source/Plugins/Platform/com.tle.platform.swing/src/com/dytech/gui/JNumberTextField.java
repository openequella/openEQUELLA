package com.dytech.gui;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @author Nicholas Read
 */
public class JNumberTextField extends JTextField
{
	public JNumberTextField(int maxNumber)
	{
		super(new NumberDocument(maxNumber), null, 0);
	}

	public void clear()
	{
		((NumberDocument) getDocument()).clear();
	}

	@Override
	public void setText(String t)
	{
		if( t == null || t.length() == 0 )
		{
			clear();
		}
		else
		{
			super.setText(t);
		}
	}

	public int getNumber()
	{
		return getNumber(-1);
	}

	public int getNumber(int defaultValue)
	{
		String t = getText();
		return t.length() == 0 ? defaultValue : Integer.parseInt(t);
	}

	private static class NumberDocument extends PlainDocument
	{
		private int maxNumber;

		public NumberDocument(int maxNumber)
		{
			this.maxNumber = maxNumber;
		}

		public void clear()
		{
			try
			{
				super.remove(0, getLength());
			}
			catch( BadLocationException ex )
			{
				throw new RuntimeException("This should never happen", ex);
			}
		}

		@Override
		public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException
		{
			try
			{
				int value = Integer.parseInt(s);
				if( value < 0 )
				{
					Toolkit.getDefaultToolkit().beep();
					return;
				}
			}
			catch( NumberFormatException ex )
			{
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			super.insertString(offset, s, attributeSet);

			if( Integer.parseInt(getText(0, getLength())) > maxNumber )
			{
				Toolkit.getDefaultToolkit().beep();
				super.remove(offset, s.length());
			}
		}
	}
}