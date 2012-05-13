package utils.fr.jmg.extractor.api;

/*
 * An enum to be able to differenciate every type of flat
 */
public enum Type{

	PARKING(0), STUDIO(1), T2(2), T3(3), T4(4), T5(5), T6(6), T7(7), T8(8), UNKNOWN(-1);

	public final int nbRooms;

	Type(int nbRooms){
		this.nbRooms = nbRooms;
	}

	/*
	 * Returns every identified type of flat
	 */
	public static final Type[] getValidTypes(){
		return new Type[]{PARKING, STUDIO, T2, T3, T4, T5, T6, T7, T8};
	}

	public static Type getType(int t){
		switch(t){
		case 0:
			return PARKING;
		case 1:
			return STUDIO;
		case 2:
			return T2;
		case 3:
			return T3;
		case 4:
			return T4;
		case 5:
			return T5;
		case 6:
			return T6;
		case 7:
			return T7;
		case 8:
			return T8;
		default:
			return UNKNOWN;
		}
	}

	public int getNbRooms(){
		return this.nbRooms;
	}
}