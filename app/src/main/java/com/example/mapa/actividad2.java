package com.example.mapa;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mapa.WebServices.Asynchtask;
import com.example.mapa.WebServices.WebService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class actividad2 extends AppCompatActivity implements OnMapReadyCallback, Asynchtask {
    GoogleMap mapa;

    Double lat, lng;
    float radio;
    Circle circulo = null;

    Slider sliderRadio;
    EditText txtLatitud, txtLongitud;
    Button btn3D;
    ArrayList<Marker> markers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actividad2);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lat = -1.02313;
        lng = -79.459561;
        radio = 1;

        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        sliderRadio = findViewById(R.id.sliderRadio);
        btn3D = findViewById(R.id.btn3D);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        sliderRadio.addOnChangeListener((slider, value, fromUser) -> {
            radio = value;
            updateInterfaz();
        });

        btn3D.setOnClickListener(v -> {
            if (mapa != null) {
                CameraPosition camPos = new CameraPosition.Builder()
                        .target(new LatLng(lat, lng))
                        .zoom(19)
                        .bearing(45)
                        .tilt(70)
                        .build();
                CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
                mapa.animateCamera(camUpd3);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);
        
        CameraUpdate camUpd1 = CameraUpdateFactory
                .newLatLngZoom(new LatLng(lat, lng), 15);

        mapa.moveCamera(camUpd1);
        
        mapa.setOnCameraIdleListener(() -> {
            LatLng center = mapa.getCameraPosition().target;
            lat = center.latitude;
            lng = center.longitude;
            updateInterfaz();
        });
    }

    private void updateInterfaz() {
        if (txtLatitud != null) txtLatitud.setText(String.format("%.4f", lat));
        if (txtLongitud != null) txtLongitud.setText(String.format("%.4f", lng));

        if (mapa == null) return;

        if (circulo != null) {
            circulo.remove();
            circulo = null;
        }

        int strokeColor = Color.parseColor("#009688");
        int fillColor = Color.argb(40, 0, 150, 136);

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius(radio * 100)
                .strokeWidth(4f)
                .strokeColor(strokeColor)
                .fillColor(fillColor);

        circulo = mapa.addCircle(circleOptions);

        Map<String, String> datos = new HashMap<>();
        String url = "https://turismo.quevedoenlinea.gob.ec/lugar_turistico/json_getlistadoMapa?lat=" + lat + "&lng=" + lng + "&radio=" + (radio / 10.0);
        
        WebService ws = new WebService(url, datos, actividad2.this, actividad2.this);
        ws.execute("GET");
    }

    @Override
    public void processFinish(String result) throws JSONException {
        if (mapa == null || result == null) return;

        for (Marker marker : markers) marker.remove();
        markers.clear();

        JSONObject JSONobj = new JSONObject(result);
        if (JSONobj.has("data")) {
            JSONArray jsonLista = JSONobj.getJSONArray("data");
            for (int i = 0; i < jsonLista.length(); i++) {
                JSONObject lugar = jsonLista.getJSONObject(i);
                LatLng posicion = new LatLng(lugar.getDouble("lat"), lugar.getDouble("lng"));
                String nombre = lugar.optString("nombre", "Lugar Turístico");
                
                markers.add(mapa.addMarker(new MarkerOptions()
                        .position(posicion)
                        .title(nombre)));
            }
        }
    }
}