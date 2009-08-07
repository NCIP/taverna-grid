package uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast;

public class SequenceDatabase {
	private String displayName;

	private String name;

	private SequenceType sequenceType;

	public SequenceDatabase(String name, SequenceType sequenceType) {
		this.name = name;
		this.sequenceType = sequenceType;
	}

	public SequenceDatabase() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SequenceDatabase)) {
			return false;
		}
		SequenceDatabase other = (SequenceDatabase) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (sequenceType == null) {
			if (other.sequenceType != null) {
				return false;
			}
		} else if (!sequenceType.equals(other.sequenceType)) {
			return false;
		}
		return true;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getName() {
		return name;
	}

	public SequenceType getSequenceType() {
		return sequenceType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((sequenceType == null) ? 0 : sequenceType.hashCode());
		return result;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSequenceType(SequenceType sequenceType) {
		this.sequenceType = sequenceType;
	}

	@Override
	public String toString() {
		return getName();
	}

	public enum SequenceType {
		nucleotide, protein
	}

}
