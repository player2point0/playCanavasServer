package os;

import java.io.Serializable;

public class FileMetadataEntry implements Serializable
{
	public String[] parentFolders;

	public String[] tags;
	public String[] tagsource;
	public String[] tagconfidence;

	public long[] lastviewed;
	public String[] lastviewedviewer;
	
	public String[] relativeTags;
	public String[] relativetagsource;
	public String[] relativetagconfidence;
	public byte[][] relativeSha1;
}
