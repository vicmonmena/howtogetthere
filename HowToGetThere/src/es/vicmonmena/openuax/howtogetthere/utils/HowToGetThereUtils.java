package es.vicmonmena.openuax.howtogetthere.utils;

import android.location.Address;

/**
 * Clase de utilidades y funciones comunes de la aplicación
 * @author vicmonmena
 *
 */
public class HowToGetThereUtils {

	// Color verde de la APP: #63d672, color rojo: #FF2659
	
	/**
	 * Paso de parámetros entre activities
	 */
	public static final String ADDRESS_PARAM = "es.vicmonmena.openuax.howtogetthere.address";
	
	/**
	 * Paso de parámetros entre activities
	 */
	public static final String FIELD_PARAM = "es.vicmonmena.openuax.howtogetthere.field";
	
	/**
	 * Respuesta de retorno de la activity
	 */
	public static final int REQUEST_CODE_LOCATION_SELECTED = 10;
	
	/**
	 * 
	 */
	public static final int REQUEST_CODE_PGPS_ACTIVATION = 20;
	
	/**
	 * Formateo de los objetos Address para rpesentarlo al usuario en la lista
	 * @param address
	 * @return
	 */
	public static String getAddressString(Address address) {
		return
		(address.getFeatureName() != null ? address.getFeatureName() + ", " : "") +
		(address.getLocality() != null ? address.getLocality() + ", " : "") + 
		(address.getCountryName() != null ? address.getCountryName() : "");
	}
}
