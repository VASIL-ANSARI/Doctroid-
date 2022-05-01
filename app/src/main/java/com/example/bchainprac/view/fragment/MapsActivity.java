package com.example.bchainprac.view.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.bchainprac.R;
import com.example.bchainprac.customView.CustomToast;
import com.example.bchainprac.customView.CustomToastType;
import com.example.bchainprac.helpers.DirectionJsonParser;
import com.example.bchainprac.network.model.Hospital;
import com.example.bchainprac.utilities.InternetUtilities;
import com.example.bchainprac.utilities.Utilities;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends Fragment implements OnMapReadyCallback, RoutingListener {

    public Context context;

    private Polyline mPolyline;
    private LatLng hospitalLatLng ;
    private GoogleMap map;
    private CameraPosition googlePlex;
    private Location userCurrentLocation;
    private SupportMapFragment mapFragment;

    private CardView markers;
    private ImageView hospitalMarker, userMarker;
    private Hospital hospital;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorGreen,R.color.colorAccent,R.color.colorGray,R.color.colorBabyBlue,R.color.colorRed};

    public MapsActivity(Hospital hospital,Location userLocation) {
        this.hospital=hospital;
        this.userCurrentLocation=userLocation;
        hospitalLatLng= new LatLng(hospital.getLatitude(), hospital.getLongitude());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        context = getActivity().getApplicationContext();

        initializeComponents(view);
        setListeners();
        polylines=new ArrayList<>();
        if(userCurrentLocation!=null)
        //getRouteToMarker();
            drawRoute();

        return view;
    }

    private void getRouteToMarker() {
        Log.d("message","User location");
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .key("AIzaSyDab2ItmpNldxvSrm-SG8Rmo48gWeOY1R4")
                .waypoints(new LatLng(userCurrentLocation.getLatitude(),userCurrentLocation.getLongitude()), hospitalLatLng)
                .build();
        routing.execute();
    }

    private void initializeComponents(View view) {

        hospitalMarker = view.findViewById(R.id.hospitalLocationFragment_hospitalMarker);
        userMarker = view.findViewById(R.id.hospitalLocationFragment_userMarker);
        markers = view.findViewById(R.id.hospitalLocationFragment_markersCardView);

        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void setListeners() {
        hospitalMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                googlePlex = CameraPosition.builder()
                        .target(hospitalLatLng)
                        .zoom(16f)
                        .bearing(0)
                        .tilt(45)
                        .build();

                map.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 2000, null);
            }
        });

        userMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userCurrentLocation != null) {
                    googlePlex = CameraPosition.builder()
                            .target(new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude()))
                            .zoom(16f)
                            .bearing(0)
                            .tilt(45)
                            .build();

                    map.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 2000, null);
                } else {
                    CustomToast.darkColor(getContext(), CustomToastType.ERROR, "Can't get your current location.");
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Map Initialize
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.clear();

        // Camera Position
        googlePlex = CameraPosition.builder()
                .target(hospitalLatLng)
                .zoom(13f)
                .bearing(0)
                .tilt(45)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 3000, null);

        // O6U Marker
        map.addMarker(new MarkerOptions()
                .position(hospitalLatLng)
                .title(hospital.getHospital_name())
                .draggable(false)
                .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.icon_hospital_map)));

        // User Marker
        if (userCurrentLocation != null) {

            // map.setMyLocationEnabled(true);  -- Blue dot
            // map.getUiSettings().setMyLocationButtonEnabled(true); -- my location button
            map.getUiSettings().setCompassEnabled(false);

            map.addMarker(new MarkerOptions()
                    .position(new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude()))
                    .title("YOU")
                    .snippet("Your Location")
                    .draggable(false)
                    .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.icon_user_map)));

            markers.setVisibility(View.VISIBLE);
        }
//        try {
//            if (Utilities.checkLocationPermission(getContext())) {
//                if (InternetUtilities.isLocationEnabled(Objects.requireNonNull(getContext()))) {
//                    FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
//                    Task location = fusedLocationProviderClient.getLastLocation();
//
//                    location.addOnCompleteListener(new OnCompleteListener() {
//                        @Override
//                        public void onComplete(@NonNull Task task) {
//                            if (task.isSuccessful()) {
//                                userCurrentLocation = (Location) task.getResult();
//
//
//                            }
//                        }
//                    });
//                } else {
//                    CustomToast.darkColor(getContext(), CustomToastType.WARNING, "Please enable GPS to get your current location.");
//                }
//            } else {
//                checkLocationPermission();
//            }
//
//        } catch (SecurityException e) {
//            Log.v("getDeviceLocation", "getDeviceLocation: SecurityException: " + e.getMessage());
//        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapFragment != null) {
            mapFragment.onDestroy();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapFragment.onLowMemory();
    }

    private void checkLocationPermission() {
        int permission = ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CustomToast.darkColor(getContext(), CustomToastType.ERROR, "Permission denied.");
            }
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Log.d("message",e.getMessage());
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(context, "Something went wrong, Try again", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRoutingStart() {


    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestIndex) {

        Log.d("message","success");
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = map.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(context,"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {
        Log.d("message", "Routing was cancelled.");
    }

    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    private void drawRoute(){

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(new LatLng(userCurrentLocation.getLatitude(),userCurrentLocation.getLongitude()), hospitalLatLng);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }


    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&amp;"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/json?"+parameters+"&sensor=false&mode=driving&"+key;
        Log.d("message",url);
//        url="https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
//                "&destination=${dest.latitude},${dest.longitude}" +
//                "&sensor=false" +
//                "&mode=driving" +
//                "&key=$secret";

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception on download", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask","DownloadTask : " + data);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionJsonParser parser = new DirectionJsonParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;result!=null && i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if(mPolyline != null){
                    mPolyline.remove();
                }
                mPolyline = map.addPolyline(lineOptions);

            }else
                Toast.makeText(context,"No route is found", Toast.LENGTH_LONG).show();
        }
    }

}
