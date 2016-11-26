package com.android.podoal.project_podoal;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.podoal.project_podoal.datamodel.SightDTO;
import com.android.podoal.project_podoal.dataquery.SelectQueryGetter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback, LocationListener{

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String lmProvider;

    private static SelectQueryGetter dbSelector;
    private static Location location;
    private static List<SightDTO> sightList;

    private static double longitude;
    private static double latitude;

    public MapsFragment() {
        // Required empty public constructor
    }

    public static double getLongitude() {
        return longitude;
    }

    public static double getLatitude() {
        return latitude;
    }

    public static List<SightDTO> getSightList() {
        return sightList;
    }

    public static Location getLocation() {
        return location;
    }

    public static SelectQueryGetter getDbSelector() {
        return dbSelector;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        System.out.println("MAPS_FRAGMENT_ON_CREATE_VIEW_BEGIN");

        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Inflate the layout for this fragment
        dbSelector = new SelectQueryGetter();
        sightList = new ArrayList<>();

        sightSetup();
        gpsSetup();

        System.out.println("MAPS_FRAGMENT_ON_CREATE_VIEW_END");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void sightSetup() {
        try {
            String result = dbSelector.execute("http://" + GlobalApplication.SERVER_IP_ADDR + ":" + GlobalApplication.SERVER_IP_PORT + "/podoal/db_get_sight_list.php").get();
            System.out.println("SIGHT_SETUP_BEGIN - RESULT : " + result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("result");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject entity = jsonArray.getJSONObject(i);
                    SightDTO dto = new SightDTO();

                    dto.setSight_id(entity.getString("sight_id"));
                    dto.setLatitude(entity.getDouble("latitude"));
                    dto.setLongitude(entity.getDouble("longitude"));
                    dto.setRadius(entity.getDouble("radius"));
                    dto.setName(entity.getString("name"));
                    dto.setInfo(entity.getString("info"));
                    dto.setLocal_number_ID(entity.getString("local_number_ID"));

                    sightList.add(new SightDTO(dto));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            System.out.println("SIGHT_SETUP_END");
        }
    }

    private void gpsSetup()
    {
        System.out.println("GPS_SETUP_BEGIN");
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        lmProvider = locationManager.getBestProvider(criteria, true);

        if (lmProvider == null || locationManager.isProviderEnabled(lmProvider)) {
            List<String> providerList = locationManager.getAllProviders();

            for (int i = 0; i < providerList.size(); i++) {
                String providerName = providerList.get(i);

                if (locationManager.isProviderEnabled(providerName)) {
                    lmProvider = providerName;
                    break;
                }
            }
        }

        try
        {
            location = locationManager.getLastKnownLocation(lmProvider);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (location == null)
                Toast.makeText(getActivity(),"현재 위치를 찾을 수 없습니다.",Toast.LENGTH_SHORT).show();
            else
                onLocationChanged(location);

            System.out.println("GPS_SETUP_END");
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng seoul = new LatLng(37.56, 126.97);
        mMap.moveCamera( CameraUpdateFactory.newLatLng(seoul));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12));

        mMap.addMarker( new MarkerOptions().position(seoul).title( "Marker in Seoul" ) );
        mMap.addMarker(new MarkerOptions().position(new LatLng(37.555873, 127.049488)).title("Hanyang Univ. IT/BT"));

        if( sightList != null)
        {
            for (int i = 0; i < sightList.size(); i++)
            {
                SightDTO dto = sightList.get(i);
                mMap.addMarker(new MarkerOptions().position(new LatLng(dto.getLatitude(), dto.getLongitude())).title(dto.getName()));
            }
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        // TODO Auto-generated method stub
    }
    @Override
    public void onProviderEnabled(String provider)
    {
        // TODO Auto-generated method stub
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        // TODO Auto-generated method stub
    }
}