package es.vicmonmena.openuax.howtogetthere;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import es.vicmonmena.openuax.howtogetthere.utils.HowToGetThereUtils;

/**
 * Activity para localizar direcciones
 * @author vicmonmena
 */
public class FindLocationActivity extends Activity implements OnItemClickListener {

	/**
	 * Etiqueta para los mensajes de log de esta clase
	 */
	private final String TAG = "FindLocationActivity";
	
	/**
	 * Campo de busqueda del origen
	 */
	private EditText findLocationEdit;
	
	/**
	 * Vista del listado de localizaciones que se exponen al usuario
	 */
	private ListView listView;
	
	/**
	 * Localizaciones coincidentes con la búsqueda
	 */
	List <Address> addresses;
	
	/**
	 * ID del campo que debemos actualizar en la Activity padre
	 */
	private int fieldSelected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_location);
        
        // Navegador con botón HOME para volver a la anterior activity
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        fieldSelected = getIntent().getIntExtra(HowToGetThereUtils.FIELD_PARAM, R.id.from_edit);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setOnItemClickListener(this);
        
        // Control sobre el botón de búsqueda del teclado
        findLocationEdit = (EditText) findViewById(R.id.find_location_edit);
        findLocationEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            	boolean result = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    result = true;
                    findLocation();
                }
                return result;
            }
        });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.find_location, menu);
	    return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
			case R.id.action_info:
				Toast.makeText(this, getString(R.string.info_text), Toast.LENGTH_SHORT).show();
				return true;
			case R.id.action_search:
				findLocation();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
    }
	
	/**
	 * Captura el evento onclick de la view que tenga definido el metodo
	 * @param view
	 */
	public void onClick(View view) {
		findLocation();
	}
	
	/**
	 * Carga un listado de localizaciones
	 */
	public void findLocation() {
		
		// Ocultamos el teclado
		InputMethodManager inputMngr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMngr.hideSoftInputFromWindow(findLocationEdit.getWindowToken(), 
                                  InputMethodManager.RESULT_UNCHANGED_SHOWN);
        
		if (!TextUtils.isEmpty(findLocationEdit.getText().toString())) {
			
			FindLocationsAsyncTask async = new FindLocationsAsyncTask();
			async.execute(findLocationEdit.getText().toString());
			
		}
	}
	
	/**
	 * 
	 * @author vicmonmena
	 *
	 */
	private class FindLocationsAsyncTask extends AsyncTask<String, Void, String[]> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			findViewById(R.id.listView1).setVisibility(View.GONE);
			findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
		}
		
		@Override
		protected String[] doInBackground(String... params) {
			String stringAddresses[] = null;
			if (Geocoder.isPresent()) {
				
				try {
					addresses = new Geocoder(FindLocationActivity.this).getFromLocationName(
						findLocationEdit.getText().toString(), 50);
					
					if (addresses.size() == 0) {
						Toast.makeText(FindLocationActivity.this, "No places found!", Toast.LENGTH_SHORT).show();
					} else {
						stringAddresses = new String[addresses.size()];
						for (int i = 0; i < addresses.size(); i++) {
							Address address = addresses.get(i);
							stringAddresses[i] = HowToGetThereUtils.getAddressString(address);
						}
					}
				}catch (Exception e) {
					Toast.makeText(FindLocationActivity.this, "Imposible to do Reverse Geocodification", Toast.LENGTH_SHORT).show();
					Log.d(TAG,e.getMessage());
				}
			}
			return stringAddresses;
		}
		
		@Override
		protected void onPostExecute(String[] result) {
			if (result != null) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					FindLocationActivity.this, 
					android.R.layout.simple_list_item_1,
					result);
					listView.setAdapter(adapter);
			}
			findViewById(R.id.progressBar1).setVisibility(View.GONE);
			findViewById(R.id.listView1).setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		Intent iLocation = new Intent();
		iLocation.putExtra(HowToGetThereUtils.ADDRESS_PARAM, addresses.get(position));
		iLocation.putExtra(HowToGetThereUtils.FIELD_PARAM, fieldSelected);
		setResult(RESULT_OK, iLocation);
		finish();
		
	}
}
