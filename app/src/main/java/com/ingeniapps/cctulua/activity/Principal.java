package com.ingeniapps.cctulua.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.app.Config;
import com.ingeniapps.cctulua.fragment.fragment_buscar;
import com.ingeniapps.cctulua.fragment.fragment_cartelera;
import com.ingeniapps.cctulua.fragment.fragment_ccomercial;
import com.ingeniapps.cctulua.fragment.fragment_contacto;
import com.ingeniapps.cctulua.fragment.fragment_ofertas;
import com.ingeniapps.cctulua.helper.BottomNavigationViewHelper;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.util.NotificationUtils;

import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;


public class Principal extends AppCompatActivity
{
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    vars vars;
    private String tokenFCM;
    private String indCambioClv;
    private String nomColaborador;
    private String indicaPush;
    com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    public String currentVersion = null;
    private String html="";
    private String versionPlayStore="";
    Context context;
    Dialog dialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gestionSharedPreferences=new gestionSharedPreferences(Principal.this);
        Fabric.with(this, new Crashlytics());

        tokenFCM="";
        vars=new vars();
        context = this;

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                indCambioClv = null;
                nomColaborador = null;
                indicaPush = null;
            }
            else
            {
                indCambioClv = extras.getString("indCambioClv");
                nomColaborador = extras.getString("nomColaborador");
                indicaPush = extras.getString("indicaPush");
            }
        }

        try
        {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        if(checkPlayServices())
        {
            if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
            {
                tokenFCM=FirebaseInstanceId.getInstance().getToken();
                Log.i("TokenFCM is: ",""+FirebaseInstanceId.getInstance().getToken());
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Principal.this,R.style.AlertDialogTheme));
            builder
                    .setTitle("GOOGLE PLAY SERVICES")
                    .setMessage("Se ha encontrado un error con los servicios de Google Play, actualizalo y vuelve a ingresar.")
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            finish();
                        }
                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                    setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new fragment_ccomercial());
        fragmentTransaction.commit();

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item)
                    {
                        Fragment fragment = null;
                        Class fragmentClass;

                        switch (item.getItemId())
                        {
                            case R.id.action_ccomercial:
                                fragmentClass = fragment_ccomercial.class;
                                break;
                            case R.id.action_ofertas:
                                fragmentClass = fragment_ofertas.class;
                                break;
                            case R.id.action_contacto:
                                fragmentClass = fragment_contacto.class;
                                break;
                            case R.id.action_cine:
                                fragmentClass = fragment_cartelera.class;
                                break;
                            case R.id.action_buscar:
                                fragmentClass = fragment_buscar.class;
                                break;
                            default:
                                fragmentClass = fragment_ccomercial.class;
                        }

                        try
                        {
                            fragment = (Fragment) fragmentClass.newInstance();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout, fragment);
                        fragmentTransaction.commit();
                        return true;
                    }
                });

        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // checking for type intent filter
                if (intent.getAction().equals(Config.PUSH_OFERTA))
                {
                    if(!(Principal.this).isFinishing())
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Principal.this, R.style.AlertDialogTheme));
                        builder
                                .setTitle("Centro Comercial Tuluá")
                                .setMessage("Hola! Tienes una nueva oferta justo ahora!")
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        /*bottomNavigationView.setSelectedItemId(R.id.action_historial);
                                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.replace(R.id.frame_layout, new HistorialNominaciones());
                                        fragmentTransaction.commit();*/
                                    }
                                }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            }
        };

        if(TextUtils.equals(indicaPush,"pushNominacion"))
        {
            /*bottomNavigationView.setSelectedItemId(R.id.action_historial);
            android.support.v4.app.FragmentManager fragmentManagerr = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransactionn = fragmentManagerr.beginTransaction();
            fragmentTransactionn.replace(R.id.frame_layout, new HistorialNominaciones());
            fragmentTransactionn.commit();*/
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new android.support.v7.view.ContextThemeWrapper(this, R.style.AlertDialogTheme));
            builder
                    .setTitle("Centro Comercial Tuluá")
                    .setMessage("¿Deseas salir de la aplicación?")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            finish();
                        }
                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                }
            }).show();

        }

        return false;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //COUNTER DE NOTIFICACIONES
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    private boolean notificaUpdate=false;

    @Override
    public void onResume()
    {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_OFERTA));
        NotificationUtils.clearNotifications(this);

       // _webServicecheckVersionAppPlayStore();
        if(!notificaUpdate)
        {
            new CheckUpdateAppPlayStore().execute();
            notificaUpdate=true;
        }

        updateTokenFCMToServer();
    }

    public static int compareVersions(String version1, String version2)//COMPARAR VERSIONES
    {
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");

        int length = Math.max(levels1.length, levels2.length);
        for (int i = 0; i < length; i++){
            Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            Integer v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0){
                return compare;
            }
        }
        return 0;
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS)
        {
            if(googleAPI.isUserResolvableError(result))
            {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    private void updateTokenFCMToServer()
    {
        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/UpdateTokenFCM");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status = response.getBoolean("status");
                            String message = response.getString("message");

                            if(status)
                            {
                            }
                            else
                            {
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("idDevice", gestionSharedPreferences.getString("idDevice"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "FCM_Firebase");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("FCM_Firebase");
        ControllerSingleton.getInstance().cancelPendingReq("updateApp");
    }

    private void _webServicecheckVersionAppPlayStore()
    {
        String _urlWebService = "https://play.google.com/store/apps/details?id=com.ingeniapps.cctulua";

        StringRequest jsonObjReq = new StringRequest (Request.Method.GET, _urlWebService,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        html=response;
                        Document document= Jsoup.parse(html);
                        versionPlayStore=document.select("div[itemprop=softwareVersion]").first().ownText();
                        Log.i("softwareVersion","softwareVersion: "+versionPlayStore);

                        if(compareVersions(currentVersion,versionPlayStore) == -1)
                        {
                            if(!((Activity) context).isFinishing())
                            {
                                dialog = new Dialog(Principal.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setCancelable(true);
                                dialog.setContentView(R.layout.custom_dialog);

                                Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
                                dialogButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("market://details?id=com.ingeniapps.cctulua"));

                                        if(dialog.isShowing())
                                        {
                                            dialog.dismiss();
                                        }

                                        startActivity(intent);
                                    }
                                });

                                dialog.show();
                            }
                        }
                    }

                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "updateApp");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public class CheckUpdateAppPlayStore extends AsyncTask<String, Void, String>
    {
        protected String doInBackground(String... urls)
        {
            try
            {
                versionPlayStore=Jsoup.connect("https://play.google.com/store/apps/details?id=" + "com.ingeniapps.cctulua" + "&hl=es")
                        .timeout(10000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();

                return versionPlayStore;
            }
            catch (Exception e)
            {
                return "";
            }
        }

        protected void onPostExecute(String string)
        {
            versionPlayStore = string;

            if(!TextUtils.isEmpty(versionPlayStore)&&!TextUtils.equals(versionPlayStore,""))
            {
                if (compareVersions(currentVersion, versionPlayStore) == -1)
                {
                    if (!((Activity) context).isFinishing())
                    {
                        dialog = new Dialog(Principal.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(true);
                        dialog.setContentView(R.layout.custom_dialog);

                        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
                        dialogButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=com.ingeniapps.cctulua"));
                                startActivity(intent);
                            }
                        });

                        dialog.show();
                    }
                }
            }
        }
    }



}
