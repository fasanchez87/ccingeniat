package com.ingeniapps.cctulua.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.cctulua.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ingeniapps.cctulua.adapter.LocalAdapter;
import com.ingeniapps.cctulua.beans.Local;
import com.ingeniapps.cctulua.fragment.fragment_ccomercial;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.util.SimpleDividerItemDecoration;
import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.volley.ControllerSingleton;

import io.fabric.sdk.android.Fabric;

public class Buscar extends AppCompatActivity
{

    private gestionSharedPreferences sharedPreferences;
    private ArrayList<Local> listadoLocales;
    private RecyclerView recycler_view_locales_busqueda;
    private LocalAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    LinearLayout linearHabilitarLocales;
    RelativeLayout layoutEspera;
    RelativeLayout layoutMacroEsperaLocales;

    Context context;
    private boolean solicitando=false;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    private ImageView not_found_convenios;

    private ProgressDialog progressDialog;

    EditText editTextBusquedaLocal;
    TextView editTextNumConvenios;
    private String idCategoria;
    private Double lat;
    private Double lon;
    public static String distanciaPunto;

    DividerItemDecoration mDividerItemDecoration;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    vars vars;
    com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    private String cantidadKilometros;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        vars=new vars();
        gestionSharedPreferences=new gestionSharedPreferences(Buscar.this);
        Fabric.with(this, new Crashlytics());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });


        progressDialog = new ProgressDialog(new android.support.v7.view.ContextThemeWrapper(Buscar.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Buscando...");

        sharedPreferences=new gestionSharedPreferences(this);
        listadoLocales=new ArrayList<Local>();
        vars=new vars();
        context = this;

        editTextNumConvenios=(TextView)findViewById(R.id.editTextNumConvenios);
        editTextBusquedaLocal=(EditText)findViewById(R.id.editTextBusquedaLocal);

        not_found_convenios=(ImageView)findViewById(R.id.not_found_convenios);
        layoutEspera=(RelativeLayout)findViewById(R.id.layoutEsperaConvenios);
        layoutMacroEsperaLocales=(RelativeLayout)findViewById(R.id.layoutMacroEsperaLocales);
        linearHabilitarLocales=(LinearLayout)findViewById(R.id.linearHabilitarLocales);

        recycler_view_locales_busqueda=(RecyclerView) findViewById(R.id.recycler_view_locales_busqueda);
        mLayoutManager = new LinearLayoutManager(this);

        mAdapter = new LocalAdapter(this,listadoLocales,new LocalAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Local local)
            {
                Intent i=new Intent(Buscar.this, DetalleLocal.class);
                i.putExtra("codLocal",local.getCodLocal());
                startActivity(i);
            }
        });

        recycler_view_locales_busqueda.setHasFixedSize(true);
        recycler_view_locales_busqueda.setLayoutManager(mLayoutManager);
        recycler_view_locales_busqueda.setItemAnimator(new DefaultItemAnimator());
        recycler_view_locales_busqueda.setAdapter(mAdapter);

        ImageView buttonBuscar = (ImageView) findViewById(R.id.ivSearch);
        buttonBuscar.setClickable(true);
        buttonBuscar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                progressDialog.show();
                progressDialog.setCancelable(false);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //OCULTAMOS TECLADO
                imm.hideSoftInputFromWindow(editTextBusquedaLocal.getWindowToken(), 0);
                WebServiceGetLocales(editTextBusquedaLocal.getText().toString());
            }
        });

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras==null)
            {
                idCategoria = null;
                lat = null;
                lon = null;
            }
            else
            {
                idCategoria=extras.getString("idCategoria");
                lat=extras.getDouble("lat");
                lon=extras.getDouble("lon");
            }
        }

        WebServiceGetLocales(null);
    }



    @Override
    public void onResume()
    {
        super.onResume();
        updateTokenFCMToServer();
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
                        //Toast.makeText(getActivity(), "Token FCM: " + "error"+error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codUsuario", gestionSharedPreferences.getString("codUsuario"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "setTokenFCM");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("setTokenFCM");
        ControllerSingleton.getInstance().cancelPendingReq("getLocales");
    }


    @Override
    public void onBackPressed()
    {
        finish();
    }

    private void WebServiceGetLocales(final String busqueda)
    {
        String _urlWebService = vars.ipServer.concat("/ws/getLocales");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if(response.getBoolean("status"))
                            {
                                listadoLocales.clear();

                                JSONArray listaLocales = response.getJSONArray("locales");

                                for (int i = 0; i < listaLocales.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaLocales.get(i);
                                    Local local = new Local();
                                    local.setCodLocal(jsonObject.getString("codLocal"));
                                    local.setIndHabilita(jsonObject.getString("indEstado"));
                                    local.setNomLocal(jsonObject.getString("nomLocal"));
                                    local.setImgLocal(vars.ipServer+"/"+"panel/"+jsonObject.getString("imgLocal"));
                                    Log.i("marti",vars.ipServer+"/panel"+jsonObject.getString("imgLocal"));
                                    local.setUbicacionLocal(jsonObject.getString("ubicacionLocal"));
                                    local.setDescLocal(jsonObject.getString("descLocal"));
                                    local.setTelLocal(jsonObject.getString("telLocal"));
                                    local.setType(jsonObject.getString("type"));

                                    if(local.getIndHabilita().equals("1"))
                                    {
                                        //editTextNumEmpleados.setText(listaEmpleados.length()+" Empleados Encontados");
                                        listadoLocales.add(local);
                                    }
                                }

                                if(progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();
                                }

                                layoutMacroEsperaLocales.setVisibility(View.GONE);
                                linearHabilitarLocales.setVisibility(View.VISIBLE);
                            }

                            else
                            {
                                if(progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();
                                }

                                listadoLocales.clear();
                                mAdapter.notifyDataSetChanged();

                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                                builder
                                        .setMessage(response.getString("message"))
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {

                                            }
                                        }).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            /*layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();

                            e.printStackTrace();
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (error instanceof TimeoutError)
                        {
                           /* layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);
*/
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conexión, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NoConnectionError)
                        {
                            /*layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Por favor, conectese a la red.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                           /* layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de autentificación en la red, favor contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            /*layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error server, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NetworkError)
                        {
                           /* layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de red, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ParseError)
                        {
                           /* layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conversión Parser, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("buscar", TextUtils.isEmpty(busqueda)?"":busqueda);
                //headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getLocales");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}