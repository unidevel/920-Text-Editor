package io.emmet.editor920;

import io.emmet.*;
import com.jecelyin.widget.*;

public class EmmetEditorImpl implements IEmmetEditor
{
	JecEditText editor;
	public EmmetEditorImpl(JecEditText editor)
	{
		this.editor=editor;
	}
	public SelectionData getSelectionRange()
	{
		return new SelectionData(this.editor.getSelectionStart(),this.editor.getSelectionEnd());
	}

	public void createSelection(int start, int end)
	{
		this.editor.setSelection(start,end);
	}

	public SelectionData getCurrentLineRange()
	{
		int line=this.editor.getLineForOffset(this.editor.getSelectionEnd());
		return new SelectionData(this.editor.getLineStart(line),this.editor.getLineEnd(line));
	}

	public int getCaretPos()
	{
		return this.editor.getSelectionEnd();
	}

	public void setCaretPos(int pos)
	{
		this.editor.setSelection(pos);
	}

	public String getCurrentLine()
	{
		int line=this.editor.getLineForOffset(this.editor.getSelectionEnd());
		return this.editor.getLineString(line);
	}

	public void replaceContent(String value)
	{
		replaceContent(value, this.editor.getSelectionStart());
	}

	public void replaceContent(String value, int start)
	{
		replaceContent(value, start, this.editor.getSelectionEnd());
	}

	public void replaceContent(String value, int start, int end)
	{
		replaceContent(value, start, end, false);
	}

	public void replaceContent(String value, int start, int end, boolean no_indent)
	{
		this.editor.getEditableText().replace(start,end,value);
	}

	public String getContent()
	{
		return this.editor.getText().toString();
	}

	public String getSyntax()
	{
		return "html";
	}

	public String getProfileName()
	{
		return "html";
	}

	public String prompt(String title)
	{
		// TODO: Implement this method
		return null;
	}

	public String getSelection()
	{
		int start = this.editor.getSelectionStart();
		int end = this.editor.getSelectionEnd();
		if (start>end){
			start=end;
			end=this.editor.getSelectionStart();
		}
		int len=end-start;
		char buf[]=new char[len];
		this.editor.getText().getChars(start, end, buf, 0);
		return new String(buf);
	}

	public String getFilePath()
	{
		// TODO: Implement this method
		return null;
	}

}
