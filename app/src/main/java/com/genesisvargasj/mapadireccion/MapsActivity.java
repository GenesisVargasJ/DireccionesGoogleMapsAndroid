package com.genesisvargasj.mapadireccion;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.os.StrictMode;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends Activity {

    private GoogleMap mMap;
    double latitud;
    double longitud;
    ListView listDireccion;
    EditText txtDireccion;
    Button btnAgregar;
    ArrayList<String> items = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setUpMapIfNeeded();
        txtDireccion = (EditText) findViewById(R.id.txtDireccion);
        btnAgregar = (Button) findViewById(R.id.btnAgregar);
        listDireccion = (ListView) findViewById(R.id.listDireccion);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listDireccion.setAdapter(adapter);
        listDireccion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GetCoordinates(items.get(position).toString());
            }
        });
    }

    public void AgregarDireccion(View v) {
        items.add(txtDireccion.getText().toString());
        adapter.notifyDataSetChanged();
    }

    void GetCoordinates(String dire) {
        String uri = "http://maps.google.com/maps/api/geocode/json?address=" + dire.replace(" ","+") + "&sensor=false";
        HttpGet httpGet = new HttpGet(uri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            client.execute(httpGet);
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            latitud = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            longitud = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            setUpMap(dire);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }

    private void setUpMap(String dire) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitud, longitud, 1);
            if(addresses.size() > 0) {
                latitud = addresses.get(0).getLatitude();
                longitud = addresses.get(0).getLongitude();
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitud, longitud)).title(dire));
                CameraUpdate mCamera = CameraUpdateFactory.newLatLngZoom(new LatLng(latitud, longitud), 14);
                mMap.animateCamera(mCamera);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
