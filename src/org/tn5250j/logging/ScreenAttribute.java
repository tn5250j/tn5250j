package org.tn5250j.logging;


public enum ScreenAttribute {
	
	//https://www.ibm.com/support/knowledgecenter/ssw_i5_54/apis/dsm1f.htm
	UNSET(null,"Unset"),
	GRN(" ","Green"),
	GRN_RI("!","Green/Reverse Image"),
	WHT("\"","White"),
	WHT_RI("#","White/Reverse Image"),
	GRN_UL("$","Green/Underscore"),
	GRN_UL_RI("%","Green/Underscore/Reverse Image"),
	WHT_UL("&","White/Underscore"),
	ND("'","Nondisplay"),
	RED("(","Red"),
	RED_RI(")","Red/Reverse Image"),
	RED_BL("*","Red/Blink"),
	RED_RI_BL("+","Red/Reverse Image/Blink"),
	RED_UL("`","Red/Underscore"),
	RED_UL_RI("-","Red/Underscore/Reverse Image"),
	RED_UL_BL(".","Red/Underscore/Blink"),
	ND_2F("/","Nondisplay"),
	TRQ_CS("0","Turquoise/Column Separators"),
	TRQ_CS_RI("1","Turquoise/Column Separators/Reverse Image"),
	YLW_CS("2","Yellow/Column Separators"),
	YLW_CS_RI("3","Yellow/Column Separators/Reverse Image"),
	TRQ_UL("4","Turquoise/Underscore"),
	TRQ_UL_RI("5","Turquoise/Underscore/Reverse Image"),
	YLW_UL("6","Yellow/Underscore"),
	ND_37("7","Nondisplay"),
	PNK("8","Pink"),
	PNK_RI("9","Pink/Reverse Image"),
	BLU(":","Blue"),
	BLU_RI(";","Blue/Reverse Image"),
	PNK_UL("<","Pink/Underscore"),
	PNK_UL_RI("=","Pink/Underscore/Reverse Image"),
	BLU_UL(">","Blue/Underscore"),
	ND_3F("?","Nondisplay");
	private String code;
	private String description;
    public String getCode() {
		return code;
	}
	public String getDescription() {
		return description;
	}
	private ScreenAttribute(final String code,final String desc){this.code = code; this.description=desc;}
    public String getColor(){
    	if(code != null && !"Nondisplay".equals(description)){
    		if(description.contains("Reverse")){
    			return "reverse" + description.split("/")[0];
    		}else{
	    		return description.split("/")[0].toLowerCase();
	    	}
    	}
    	return "";
    }
    public boolean isUnderLine(){
    	return description.contains("Underscore");
    }
    public boolean isNonDisplay(){
    	return "Nondisplay".equals(description);
    }
    
	public static ScreenAttribute getAttrEnum(final char currentAttr){
		for (ScreenAttribute attr : ScreenAttribute.values()) {
			if (attr.getCode() != null && currentAttr == attr.getCode().charAt(0)) {
				return attr;
			}
		}
		return UNSET;
	}
	
}
