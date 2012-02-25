/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

public class IllegalSymbolException extends Exception {

	private static final long serialVersionUID = 691086940653546495L;
	private final char c;
	private final int pos;
	private final String seq;

	public IllegalSymbolException(char c, int pos, String seq) {
		this.c = c;
		this.pos = pos;
		this.seq = seq;
	}

	public char getCharacter() {
		return c;
	}

	public int getPos() {
		return pos;
	}

	public String getSequence() {
		return seq;
	}

	@Override
	public String getMessage() {
		int endPos = Math.min(40, seq.length());
		String errorMsg = "Illegal Symbol '" + c + "' at position " + pos + " of the sequence "
				+ seq.substring(0, endPos) + ".";
		return errorMsg;
	}

}
