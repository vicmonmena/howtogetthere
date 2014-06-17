package es.vicmonmena.openuax.howtogetthere;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import es.vicmonmena.openuax.howtogetthere.utils.HowToGetThereUtils;

/**
 * 
 * @author vicmonmena
 *
 */
public class MainActivity extends FragmentActivity implements OnMapLongClickListener {

	/**
	 * Etiqueta para los mensajes de log de esta clase
	 */
	private final String TAG = "MainActivity";
	
	/**
	 * Objeto para trabajar con Google Maps
	 */
	private GoogleMap map;
	
	/**
	 * Lista de marcadores añadidas al mapa
	 */
	private List<MarkerOptions> markers;
	
	/**
	 * Localización de destino
	 */
	private MarkerOptions destination;
	
	/**
	 * Para obtener nuestra localización
	 */
	private LocationManager locMngr;
	/**
	 * Par mostrar o no el diálogo de alerta más de una vez
	 */
	boolean showsDialog = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Comprobamos si el servicio está disponible
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
        	Toast.makeText(this, getString(R.string.maps_not_available), Toast.LENGTH_SHORT).show();
        } else {
        	Log.d(TAG, "Maps available!");
        	map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        	initMap();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
			case R.id.action_info:
				Toast.makeText(this, getString(R.string.info_text), Toast.LENGTH_SHORT).show();
				return true;
			case R.id.action_add_marker:
				Toast.makeText(this, getString(R.string.coming_soon), Toast.LENGTH_LONG).show();
				return true;
			case R.id.action_clear_map:
				map.clear();
				destination = null;
				markers.clear();
				// Limpiamos el texto
	    		((TextView)findViewById(R.id.from_edit)).setText("");
	    		((TextView)findViewById(R.id.to_edit)).setText("");
				return true;
			case R.id.action_map_type:
				if (map.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
					map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				} else if( map.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
					map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				} else if( map.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
					map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				} else {
					map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
    }
    
    /**
     * Captura el vento onCLick de los botones de búsqueda de direcciones
     * @param view
     */
    public void onFindLocationClick (View view) {
    	/* Cargamos la activity para buscar localizaciones y le pasamos el id 
    	 * de la vista que vamos a actualzar a la vuelta
    	 *  
    	 */
    	Intent intent = new Intent(this, FindLocationActivity.class);
    	intent.putExtra(HowToGetThereUtils.FIELD_PARAM, view.getId());
		startActivityForResult(intent, HowToGetThereUtils.REQUEST_CODE_LOCATION_SELECTED);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (resultCode == RESULT_OK) {
    		
    		if (requestCode == HowToGetThereUtils.REQUEST_CODE_LOCATION_SELECTED) {
	    		// Obtenemos la localización seleccionada en FindLocationActivity
	    		Address address = data.getParcelableExtra(HowToGetThereUtils.ADDRESS_PARAM);
	    		LatLng position = new LatLng(address.getLatitude(), address.getLongitude());
	    		
	    		// Formateamos la dirección para mostrarla
	    		String place = HowToGetThereUtils.getAddressString(address);
	    		
	    		// Obtenemos el id del campo que vamos a actualizar
	    		int fieldId = data.getIntExtra(HowToGetThereUtils.FIELD_PARAM, R.id.from_edit);
	    		
	    		// Texto que se muestra al clicar en el marcador
	    		String snippet = getString(R.string.from) + ": " + place;
	    		if (fieldId == R.id.to_edit) {
	    			snippet = getString(R.string.to) + ": " + place;
	    			// Para que sea pintado el último no lo añadimos a la lista
	    			destination = new MarkerOptions()
		    			.position(position)
		    			.title(place)
		    			.draggable(false)
		    			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_end));
	    			map.moveCamera(CameraUpdateFactory.newLatLngZoom(destination.getPosition(), 10));
	    		} else {
	    			// Crea el marcador inicial
	    			MarkerOptions marker = new MarkerOptions()
		    			.position(position)
		    			.title(place)
		    			.draggable(false)
		    			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_start));
	    			// Lo añadimos el primero de la lista
	    			markers.add(0, marker);
	    			map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 10));
	    		}
	    		// Actualizamos el campo de texto con la localización
	    		((TextView)findViewById(fieldId)).setText(
	    				HowToGetThereUtils.getAddressString(address));
	    		
	    		// Pintar marcadores
	    		drawMarkers();
    		} else if (requestCode == HowToGetThereUtils.REQUEST_CODE_LOCATION_SELECTED) {
    			if (!locMngr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	    			new AlertDialog.Builder(this)
	    	    	.setTitle(getString(R.string.gps_disable_title))
	    	    	.setMessage(getString(R.string.gps_disable))
	    	    	.setNeutralButton(android.R.string.yes,
	    	    	new DialogInterface.OnClickListener() {
	    	            public void onClick(DialogInterface dialog, int whichButton) {
	    	            	dialog.cancel();
	    	            }
	    	        }).create().show();
    			}
    		}
    	}
    }
    
    /**
     * Captura el evento onclick del botón para trazar la ruta
     * @param view
     */
    public void onTraceRouteClick(View view) {
    	
    	float totalDistance = 0;
    	
    	if (markers.size() > 1 || (markers.size() > 0 && destination != null)) {
	    	// Pinta las rutas exceptuando la última que la pinta después
	    	for (int i = 0; i < markers.size()-1; i++) {
	    		
	    		LatLng from = markers.get(i).getPosition();
				LatLng to = markers.get(i+1).getPosition();
				
				float [] distance = new float[1];
				Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, distance);
				// Acumulamos todas las distinacias del recorrido
				totalDistance += distance[0];
				
				// Indicamos la distancia hasta el siguiente punto
				markers.get(i).snippet(getString(R.string.distance_to_next_point) + ":" + (distance[0]/1000) + "km");
				
				// Pintamos la ruta entre los 2 puntos
	    		PolylineOptions plOpt = new PolylineOptions();
				plOpt.add(markers.get(i).getPosition(), markers.get(i+1).getPosition());
				plOpt.color(Color.parseColor("#FF2659"));
				plOpt.width(10);
				plOpt.geodesic(true);
				map.addPolyline(plOpt);
			}
	    	
	    	// Pintamos la línea hasta el último marcador
			if (destination != null) {
				LatLng from = markers.get(markers.size()-1).getPosition();
				LatLng to = destination.getPosition();
				
				float [] distance = new float[1];
				Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, distance);
				totalDistance += distance[0];
				
				// Indicamos la distancia hasta el último punto del recorrido
				markers.get(markers.size()-1).snippet(getString(R.string.distance_to_next_point) + ":" + (distance[0]/1000) + "km");
				
				// Pintamos la ruta hasta el punto final
				PolylineOptions plOpt = new PolylineOptions();
				plOpt.add(markers.get(markers.size()-1).getPosition(), destination.getPosition());
				plOpt.color(Color.parseColor("#FF2659"));
				plOpt.width(10);
				plOpt.geodesic(true);
				map.addPolyline(plOpt);
			}
			
			StringBuilder message = new StringBuilder();
			message.append(getString(R.string.distance_from));
			message.append(":");
			message.append(markers.get(0).getTitle());
			message.append(" > ");
			message.append(getString(R.string.distance_to));
			message.append(":");
			if (destination != null) {
				message.append(destination.getTitle());
			} else {
				message.append(markers.get(markers.size()-1).getTitle());	
			}
			new AlertDialog.Builder(this)
	    	.setTitle((totalDistance/1000) + " km")
	    	.setMessage(message.toString())
	    	.setNeutralButton(android.R.string.ok,
	    	new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	dialog.cancel();
	            }
	        }).create().show();
			
    	} else {
    		Toast.makeText(this, getString(R.string.tow_markers), Toast.LENGTH_SHORT).show();
    	}
    }

	@Override
	public void onMapLongClick(LatLng position) {
		
		/* Crea un marcador, lo añade a la lista y lo pinta donde se haya dejado
		 *  pulsado en el mapa 
		 */
		if (Geocoder.isPresent()) {
			try {
				List<Address> addrs = new Geocoder(this).getFromLocation(
						position.latitude, position.longitude, 1);
				String place = HowToGetThereUtils.getAddressString(addrs.get(0));
				createMarker(position, place);
				drawMarkers();
			}catch (Exception e) {
				Toast.makeText(this, "Imposible to do Geocodificación Inversa", Toast.LENGTH_SHORT).show();
			}
		}
	}
    
	/** 
     * Define los valores de los atributos del mapa como se muestra inicialmente
     */
    private void initMap() {
    	// Inicializamos el mapa
    	map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    	map.setOnMapLongClickListener(this);
    	// UAX: 40.450996,-3.987479
    	LatLng myLatLong = new LatLng(40.450996, -3.987479);
    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLong, 15));
    	// Inicializa la lista de marcadores
    	markers = new ArrayList<MarkerOptions>();
    	
    	// Falta por definir zoom y punto inicial al que se mira (localización del terminal)
    	// map.setOnMarkerClickListener(this);
    }
    
    /**
     * Crea un amrcador y lo añade a la lista para poder pintarlo
     * @param position
     * @param place
     */
    private void createMarker(LatLng position, String place) {
    	MarkerOptions marker = new MarkerOptions()
			.position(position)
			.title(place)
			.draggable(false)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
    	markers.add(marker);
    	// Camara mirando al marcador que inicia el recorrido
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 10));
    }
    
	/**
	 * Pinta los marcadores registrados
	 */
	private void drawMarkers() {
		
		// Limpiar mapa para no duplicar marcadores
		map.clear();
		
		// Pintamos marcadores si existen
		if (markers != null && markers.size() > 0) {
			for (MarkerOptions marker : markers) {
				map.addMarker(marker);
			}
		}
		
		// Pintamos el marcador de destino si se ha definido
		if (destination != null) {
			map.addMarker(destination);
		}
	}
}
