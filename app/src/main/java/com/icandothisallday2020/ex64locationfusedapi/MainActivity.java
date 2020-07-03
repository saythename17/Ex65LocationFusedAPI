package com.icandothisallday2020.ex64locationfusedapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    //Google Fused Location Library 사용(Android 용 X)
    //└적절한 위치정보제공자를 선정하여 위치 정보 제공(성능↑)
    //외부 라이브러리 추가 : [ play-services ] 라이브러리
    //play-services-location 버전만 받기
    //디바이스에 Google Play Store App 이 없으면 실행 불가
    GoogleApiClient googleApiClient;//위치 정보 관리 객체(LocationManager 역할)
    FusedLocationProviderClient providerClient;//위치 정보 제공자 객체(적절한 위치정보제공자를 알아서 선택)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=findViewById(R.id.tv);
        
        //위치 정보 제공을 받기위한 퍼미션 작업 추가
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int permissionResult=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionResult!= PackageManager.PERMISSION_GRANTED) {//다이얼로그 생성
                String[] permissions=new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions,10);
            }
        }
    }//onCreate...

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        switch (requestCode){
            case 10:
                if(grantResults[0]==PackageManager.PERMISSION_DENIED)
                    Toast.makeText(this, "위치 정보 사용 거부\n사용자의 위치탐색 기능이 제한됨", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void clickBtn(View view) {//Fused API 를 이용하여 내 위치 정보 얻어오기(실시간 갱신)
        //위치정보관리 객체 생성을 위해 빌더객체 생성(≒ DialogBuilder)
        GoogleApiClient.Builder builder=new GoogleApiClient.Builder(this);
        //1.구글 API 사용 키 설정
        builder.addApi(LocationServices.API);//공용 키 사용
        //♣♧2.위치정보 연결이 성공하는 것을 듣는 리스너 설정
        builder.addConnectionCallbacks(successListener);
        //☆★3.위치정보 연결 실패하는 것을 듣는 리스너
        builder.addOnConnectionFailedListener(failedListener);
        //위치정보 관리 객체 생성
        googleApiClient=builder.build();
        googleApiClient.connect();//위치정보 취득 연결 시도 
        //연결이 성공하면 callbacks 의 onConnected()실행

        //위치정보 제공자 객체 얻어오기
        providerClient=LocationServices.getFusedLocationProviderClient(this);
    }
    
    //♣♧위치정보 연결 성공 리스너
    GoogleApiClient.ConnectionCallbacks successListener=new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            //연결 되었을때 발동(위치정보를 얻을 수 있는 상태)
            Toast.makeText(MainActivity.this, "위치정보 탐색 가능", Toast.LENGTH_SHORT).show();
            //위치정보제공자 객체에게 최적의 제공자를 선택하는 기준 설정
            LocationRequest request=LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//높은 정확도 제공자(GPS)를 우선시
            request.setInterval(5000);//위치정보 탐색 주기(5초마다)
            //위치정보 제공자 객체에게 실시간 위치정보를 요청
            providerClient.requestLocationUpdates(request,callback, Looper.myLooper());
        }

        @Override
        public void onConnectionSuspended(int i) { /*연결이 유예(suspend)되었을때 발동*/ }
    };
    //☆★위치정보 연결 실패 리스너
    GoogleApiClient.OnConnectionFailedListener failedListener=new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(MainActivity.this, "위치정보 탐색 불가", Toast.LENGTH_SHORT).show();
        }
    };

    //위치정보가 갱신되는 것을 듣는 리스너
    LocationCallback callback=new LocationCallback(){
        @Override//위치정보 결과를 받았을때 호출되는 메소드┐
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Location location=locationResult.getLastLocation();
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            tv.setText(latitude+","+longitude);
        }
    };

    //액티비티가 화면에 보이지 않을때 위치정보를 더이상 찾지 않도록
    @Override
    protected void onPause() {
        super.onPause();
        if(providerClient!=null) providerClient.removeLocationUpdates(callback);
    }
}
