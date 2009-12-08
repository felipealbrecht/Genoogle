package bio.pih.genoogle.io.reader;

import java.io.BufferedReader;
import java.io.IOException;

public interface RichSequenceFormat {

	public boolean readRichSequence(BufferedReader reader, RichSequenceBuilder rsiol) throws IOException, ParseException;

}