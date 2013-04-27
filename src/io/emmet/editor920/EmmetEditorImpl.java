package io.emmet.editor920;

import io.emmet.IEmmetEditor;
import io.emmet.SelectionData;
import io.emmet.TabStop;
import io.emmet.TabStopStructure;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;
import com.jecelyin.widget.JecEditText;

public class EmmetEditorImpl implements IEmmetEditor
{
	public static String TYPE_HTML = "html";
	public static String TYPE_XML = "xml";
	public static String TYPE_CSS = "css";
	public static String TYPE_HAML = "haml";
	public static String TYPE_XSL = "xsl";

	private String caretPlaceholder = "${0}";

	private static Pattern whitespaceBegin = Pattern.compile("^(\\s+)");

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

	public String getCurrentLine(int line)
	{
		return this.editor.getLineString(line);
	}
	
	public String getCurrentLine()
	{
		int line=this.editor.getLineForOffset(this.editor.getSelectionEnd());
		return getCurrentLine(line);
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

	public void replaceContent(String value, int start, int end, boolean noIndent)
	{
	//	this.editor.getEditableText().replace(start,end,value);
		String newValue = value;

		if (!noIndent) {
			int lineNum =this.editor.getLineForOffset(start);
			String line = getCurrentLine(lineNum); //getLineFromRange(getLineRangeFromPosition(start));
			String padding = getStringPadding(line);
			newValue = padString(value, padding);
		}

		TabStopStructure tabStops = new TabStopStructure(newValue);
		newValue = tabStops.getText();
		
		try {
			this.editor.getEditableText().replace(start,end,newValue);
			//doc.replace(start, end - start, newValue);

			int totalLinks = tabStops.getTabStopsCount();

			if (totalLinks < 1) {
				tabStops.addTabStopToGroup("carets", newValue.length(), newValue.length());
			}

			String[] tabGroups = tabStops.getSortedGroupKeys();
			TabStop firstTabStop = tabStops.getFirstTabStop();

			/*
			if (totalLinks > 1 || firstTabStop != null && firstTabStop.getStart() != firstTabStop.getEnd()) {
				ITextViewer viewer = EclipseEmmetHelper.getTextViewer(editor);
				LinkedModeModel model = new LinkedModeModel();
				int exitPos = -1;

				for (int i = 0; i < tabGroups.length; i++) {
					TabStopGroup tabGroup = tabStops.getTabStopGroup(tabGroups[i]);
					LinkedPositionGroup group = null;

					if (tabGroups[i].equals("carets") || tabGroups[i].equals("0")) {
						int caretCount = tabGroup.getTabStopList().size();
						for (int j = 0; j < caretCount; j++) {
							TabStop ts = tabGroup.getTabStopList().get(j);
							group = new LinkedPositionGroup();
							group.addPosition(new LinkedPosition(doc, start + ts.getStart(), ts.getLength()));
							model.addGroup(group);
							if (j == caretCount - 1) {
								exitPos = start + ts.getStart();
							}							
						}
					} else {
						group = new LinkedPositionGroup();

						for (int j = 0; j < tabGroup.getTabStopList().size(); j++) {
							TabStop ts = tabGroup.getTabStopList().get(j);
							group.addPosition(new LinkedPosition(doc, start + ts.getStart(), ts.getLength()));
						}

						model.addGroup(group);
					}
				}

				model.forceInstall();
				LinkedModeUI linkUI = new LinkedModeUI(model, viewer);
				if (exitPos != -1) {
					linkUI.setExitPosition(viewer, exitPos, 0, Integer.MAX_VALUE);
				}

				// Aptana has a buggy linked mode implementation, use simple 
				// mode for it 
				linkUI.setSimpleMode(isApatana());
				linkUI.enter();
			} else 
			*/
			{
				setCaretPos(start + firstTabStop.getStart());
			}
		} catch (Exception e) {
			Log.e("replace", e.getMessage(),e);
			e.printStackTrace();
		}
		
	}

	
	public String getCurrentLinePadding() {
		return getStringPadding(getCurrentLine());
	}

	/**
	 * Returns whitespace padding from the beginning of the text
	 * @param text
	 * @return
	 */
	private String getStringPadding(String text) {
		Matcher matcher = whitespaceBegin.matcher(text);
		if (matcher.find()) {
			String pad = matcher.group(0);
			if(pad.length()>=6){
				return pad.substring(6);
			}
			//Log.i("padding",""+pad.length());
			return pad;
		} else {
			return "";
		}
	}

	/**
	 * Repeats string <code>howMany</code> times
	 */
	public String repeatString(String str, int howMany) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < howMany; i++) {
			result.append(str);
		}

		return result.toString();
	}

	public String getNewline() {
		return "\n";
		//return TextUtilities.getDefaultLineDelimiter(doc);
	}

	/**
	 * Indents text with padding
	 * @param {String} text Text to indent
	 * @param {String|Number} pad Padding size (number) or padding itself (string)
	 * @return {String}
	 */
	public String padString(String text, String pad) {
		StringBuilder result = new StringBuilder();
		String newline = getNewline();
		String lines[] =  text.split("\\r\\n|\\n\\r|\\r|\\n", -1);

		if (lines.length > 0) {
			result.append(lines[0]);
			for (int i = 1; i < lines.length; i++) {
				result.append(newline + pad + lines[i]);
			}
		} else {
			result.append(text);
		}

		return result.toString();
	}
	
	public String getContent()
	{
		return this.editor.getText().toString();
	}

	private String getSyntaxInternal()
	{
		String fileExt = this.editor.getCurrentFileExt();
		if ( fileExt == null )
			return null;
		if ( fileExt.equalsIgnoreCase( "xsl" ) )
			return TYPE_XSL;
		else if ( fileExt.equalsIgnoreCase( "xml" ) )
			return TYPE_XML;
		else if ( fileExt.equalsIgnoreCase( "haml" ) )
			return TYPE_HAML;
		else if ( fileExt.equalsIgnoreCase( "sass" ) )
			return TYPE_CSS;
		else if ( fileExt.equalsIgnoreCase( "css" ) )
			return TYPE_CSS;
		else if ( fileExt.equalsIgnoreCase( ".less." ) )
			return TYPE_CSS;
		else if ( fileExt.equalsIgnoreCase( "html" ) )
			return TYPE_HTML;
		return null;
	}

	public String getSyntax()
	{
		String syntax = getSyntaxInternal();
		if ( syntax == null )
			return TYPE_HTML;
		return syntax;
	}

	public String getProfileName()
	{
		return null;
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
		return this.editor.getPath();
	}

}
