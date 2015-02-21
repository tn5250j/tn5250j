package org.tn5250j.framework.tn5250;

/**
 * IBM i 7.1 Information Center > Programmierung > i5/OS globalization > Globalization reference information > Keyboard reference information 
 * 
 * @see <a href="http://publib.boulder.ibm.com/infocenter/iseries/v7r1m0/index.jsp?topic=/nls/rbagsnatlangkeybrdtype.htm">National language keyboard types and SBCS code pages</a>
 */
public enum KbdTypesCodePages {
	
	ALI("Albanian","ALI","697","500","500"),
	CLB("Arabic","CLB","235","420","420"),
	AGB("Austrian/ German","AGB","697","273","273"),
	AGE("Austrian/ German","AGB","695","1141","1141"),
	AGI("Austrian/German (MNCS)","AGI","697","500","500"),
	BLI("Belgian MNCS","BLI","697","500","500"),
	BRB("Brazilian Portuguese","BRB","697","37","37"),
	BGB("Bulgarian","BGB","1150","1025","1025"),
	CAB("Canadian French","CAB","341","260","65535"),
	CAI("Canadian French MNCS","CAI","697","500","500"),
//	YGI("Croatian","YGI","959","870","870"),
	CYB("Cyrillic","CYB","960","880","880"),
	CSB("Czech","CSB","959","870","870"),
	DMB("Danish","DMB","697","277","277"),
	DMI("Danish MNCS","DMI","697","500","500"),
	FNB("Finnish/Swedish","FNB","697","278","278"),
	FNI("Finnish/Swedish MNCS","FNI","697","500","500"),
	FAB("French (Azerty)","FAB","697","297","297"),
	FAI("French (Azerty) MNCS","FAI","697","500","500"),
	FQB("French (Qwerty)","FQB","697","297","297"),
	FQI("French (Qwerty) MNCS","FQI","697","500","500"),
	GNB("Greek (See note 2.)","GNB","925","875","875"),
	NCB("Hebrew","NCB","941","424","424"),
	HNB("Hungarian","HNB","959","870","870"),
	ICB("Icelandic","ICB","697","871","871"),
	ICI("Icelandic MNCS","ICI","697","500","500"),
	INB("International","INB","697","500","500"),
	INBX("International-X","INB","697","500","500-ch"),
//	INB("International MNCS","INB","697","500","500"),
	IRB("Farsi (Iran)","IRB","1219","1097","1097"),
	ITB("Italian","ITB","697","280","280"),
	ITI("Italian MNCS","ITI","697","500","500"),
	JEB("Japanese-English","JEB","697","281","65535"),
	JEI("Japanese- English MNCS","JEI","697","500","500"),
	JKB("Japanese Kanji and Katakana","JKB","1172","290","5026"),
//	JUB("Japanese Kanji and US English","JUB","697","37","See note 3."),
	KAB("Japanese Katakana","KAB","332","290","290"),
	JPB("Japanese Latin Extended","JPB","1172","1027","1027"),
	KOB("Korean","KOB","1173","833","833"),
	ROB("Latin 2","ROB","959","870","870"),
	MKB("Macedonian","MKB","1150","1025","1025"),
	NEB("Dutch (Netherlands)","NEB","697","37","37"),
	NEI("Dutch (Netherlands) MNCS","NEI","697","500","500"),
	NWB("Norwegian","NWB","697","277","277"),
	NWI("Norwegian MNCS","NWI","697","500","500"),
	PLB("Polish","PLB","959","870","870"),
	PLBX("Polish 870-pl","PLB","959","870","870-pl"), // Workaround, to catch up Java codepage '870-pl'
	PRB("Portuguese","PRB","697","37","37"),
	PRI("Portuguese MNCS","PRI","697","500","500"),
	RMB("Romanian","RMB","959","870","870"),
	RUB("Russian","RUB","1150","1025","1025"),
	SQB("Serbian, Cyrillic","SQB","1150","1025","1025"),
	YGI("Serbian, Latin","YGI","959","870","870"),
	RCB("Simplified Chinese","RCB","1174","836","836"),
	SKB("Slovakian","SKB","959","870","870"),
	SKBX("Slovakian 870-sk","SKB","959","870","870-sk"), // Workaround, to catch up Java codepage '870-sk'
//	YGI("Slovenian","YGI","959","870","870"),
	SPB("Spanish","SPB","697","284","284"),
	SPI("Spanish MNCS","SPI","697","500","500"),
	SSB("Spanish Speaking","SSB","697","284","284"),
	SSI("Spanish Speaking MNCS","SSI","697","500","500"),
	SWB("Swedish","SWB","697","278","278"),
	SWI("Swedish MNCS","SWI","697","500","500"),
	SFI("French (Switzerland) MNCS","SFI","697","500","500"),
	SGI("German (Switzerland) MNCS","SGI","697","500","500"),
	THB("Thai","THB","1176","838","838"),
	TAB("Traditional Chinese","TAB","1175","37","937"),
	TKB("Turkish (Qwerty)","TKB","1152","1026","1026"),
	TRB("Turkish (F)","TRB","1152","1026","1026"),
	UKB("English (United Kingdom)","UKB","697","285","285"),
	UKI("English (United Kingdom) MNCS","UKI","697","500","500"),
	USB("English (United States and Canada)","USB","697","37","37"),
	USI("English (United States and Canada) MNCS","USI","697","500","500");
	
	public final String description;
	public final String kbdType;
	public final String charset;
	public final String codepage;
	public final String ccsid;
	
	/**
	 * @param description
	 * @param kbdType
	 * @param charset
	 * @param codepage
	 * @param ccsid
	 */
	private KbdTypesCodePages(String description, String kbdType, String charset, String codepage, String ccsid) {
		this.description = description;
		this.kbdType = kbdType;
		this.charset = charset;
		this.codepage = codepage;
		this.ccsid = ccsid;
	}

	@Override
	public String toString() {
		return "[description=" + description + ", kbdType=" + kbdType
		+ ", charset=" + charset + ", codepage=" + codepage
		+ ", ccsid=" + ccsid + "]";
	}

}
