package com.dytech.gui;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * @author Nicholas Read
 */
public class JValidatingTextField extends JTextField
{
	public JValidatingTextField(final Validation... vs)
	{
		setDocument(new PlainDocument()
		{
			@Override
			public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException
			{
				for( Validation v : vs )
				{
					s = v.validate(this, offset, s, attributeSet);
					if( s == null )
					{
						Toolkit.getDefaultToolkit().beep();
						return;
					}
				}
				super.insertString(offset, s, attributeSet);
			}
		});
	}

	public interface Validation
	{
		String validate(Document doc, int offset, String s, AttributeSet attributeSet);
	}

	public static class MaxLength implements Validation
	{
		private final int maxLength;

		public MaxLength(int maxLength)
		{
			this.maxLength = maxLength;
		}

		@Override
		public String validate(Document doc, int offset, String s, AttributeSet attributeSet)
		{
			return doc.getLength() + s.length() <= maxLength ? s : null;
		}
	}

	public static class DisallowChar implements Validation
	{
		private final char c;

		public DisallowChar(final char c)
		{
			this.c = c;
		}

		@Override
		public String validate(Document doc, int offset, String s, AttributeSet attributeSet)
		{
			return s.indexOf(c) < 0 ? s : null;
		}
	}

	public static class DisallowStr implements Validation
	{
		private final String str;

		public DisallowStr(final String str)
		{
			this.str = str;
		}

		@Override
		public String validate(Document doc, int offset, String s, AttributeSet attributeSet)
		{
			return s.indexOf(str) < 0 ? s : null;
		}
	}
}
