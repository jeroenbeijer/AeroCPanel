package datatypes;

public class ICAOCodeBlock {

	int codeBlockId;
	int countryId;
	int bitMask;
	int significantBitMask;
	Boolean isMilitary;
	String country;
	
	
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getCodeBlockId() {
		return codeBlockId;
	}

	public void setCodeBlockId(int codeBlockId) {
		this.codeBlockId = codeBlockId;
	}

	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public int getBitMask() {
		return bitMask;
	}

	public void setBitMask(int bitMask) {
		this.bitMask = bitMask;
	}

	public int getSignificantBitMask() {
		return significantBitMask;
	}

	public void setSignificantBitMask(int significantBitMask) {
		this.significantBitMask = significantBitMask;
	}

	public Boolean getIsMilitary() {
		return isMilitary;
	}

	public void setIsMilitary(Boolean isMilitary) {
		this.isMilitary = isMilitary;
	}



}
