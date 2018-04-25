package scratch.aftershockStatistics;

import scratch.aftershockStatistics.util.MarshalReader;
import scratch.aftershockStatistics.util.MarshalWriter;
import scratch.aftershockStatistics.util.MarshalException;

public class MagCompPage_Parameters {
	
	private double magCat;
	private double capG;
	private double capH;
	
	/**
	 * This class is a container for the magnitude of completeness parameters defined by 
	 * Page et al. (2016, BSSA).
	 * According to Page et al. the magnitude of completeness is
	 *  magMin(t) = Max(magMain/2 - G - H*log10(t), magCat)
	 * where t is measured in days.
	 * As a special case, if capG == 10.0 then the magnitude of completeness is always magCat
	 * (in which case it is recommended that capH == 0.0).
	 * 
	 * @param magCat
	 * @param capG
	 * @param capH
	 */
	public MagCompPage_Parameters(double magCat, double capG, double capH) {
		this.magCat = magCat;
		this.capG = capG;
		this.capH = capH;
	}

	public MagCompPage_Parameters(){}
	
	/**
	 * This returns the catalog magnitude of completeness (magCat).
	 * @return
	 */
	public double get_magCat() {return magCat;}
	
	/**
	 * This returns the G parameter (capG).
	 * @return
	 */
	public double get_capG() {return capG;}
	
	/**
	 * This returns the H parameter (capH).
	 * @return
	 */
	public double get_capH() {return capH;}

	@Override
	public String toString() {
		return "Page_Params[magCat="+get_magCat()+", capG="+get_capG()+", capH="+get_capH()+"]";
	}




	//----- Marshaling -----

	// Marshal version number.

	private static final int MARSHAL_VER_1 = 2001;

	private static final String M_VERSION_NAME = "MagCompPage_Parameters";

	// Marshal type code.

	protected static final int MARSHAL_NULL = 2000;
	protected static final int MARSHAL_MAG_COMP = 2001;

	protected static final String M_TYPE_NAME = "ClassType";

	// Get the type code.

	protected int get_marshal_type () {
		return MARSHAL_MAG_COMP;
	}

	// Marshal object, internal.

	protected void do_marshal (MarshalWriter writer) {

		// Version

		writer.marshalInt (M_VERSION_NAME, MARSHAL_VER_1);

		// Contents

		writer.marshalDouble ("magCat", magCat);
		writer.marshalDouble ("capG"  , capG  );
		writer.marshalDouble ("capH"  , capH  );
	
		return;
	}

	// Unmarshal object, internal.

	protected void do_umarshal (MarshalReader reader) {
	
		// Version

		int ver = reader.unmarshalInt (M_VERSION_NAME, MARSHAL_VER_1, MARSHAL_VER_1);

		// Contents

		magCat = reader.unmarshalDouble ("magCat");
		capG   = reader.unmarshalDouble ("capG"  );
		capH   = reader.unmarshalDouble ("capH"  );

		return;
	}

	// Marshal object.

	public void marshal (MarshalWriter writer, String name) {
		writer.marshalMapBegin (name);
		do_marshal (writer);
		writer.marshalMapEnd ();
		return;
	}

	// Unmarshal object.

	public MagCompPage_Parameters unmarshal (MarshalReader reader, String name) {
		reader.unmarshalMapBegin (name);
		do_umarshal (reader);
		reader.unmarshalMapEnd ();
		return this;
	}

	// Marshal object, polymorphic.

	public static void marshal_poly (MarshalWriter writer, String name, MagCompPage_Parameters obj) {

		writer.marshalMapBegin (name);

		if (obj == null) {
			writer.marshalInt (M_TYPE_NAME, MARSHAL_NULL);
		} else {
			writer.marshalInt (M_TYPE_NAME, obj.get_marshal_type());
			obj.do_marshal (writer);
		}

		writer.marshalMapEnd ();

		return;
	}

	// Unmarshal object, polymorphic.

	public static MagCompPage_Parameters unmarshal_poly (MarshalReader reader, String name) {
		MagCompPage_Parameters result;

		reader.unmarshalMapBegin (name);
	
		// Switch according to type

		int type = reader.unmarshalInt (M_TYPE_NAME);

		switch (type) {

		default:
			throw new MarshalException ("MagCompPage_Parameters.unmarshal_poly: Unknown class type code: type = " + type);

		case MARSHAL_NULL:
			result = null;
			break;

		case MARSHAL_MAG_COMP:
			result = new MagCompPage_Parameters();
			result.do_umarshal (reader);
			break;
		}

		reader.unmarshalMapEnd ();

		return result;
	}
	
}
