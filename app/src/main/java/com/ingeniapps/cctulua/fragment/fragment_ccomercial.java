package com.ingeniapps.cctulua.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.activity.Buscar;
import com.ingeniapps.cctulua.activity.DetalleEvento;
import com.ingeniapps.cctulua.activity.DetalleLocal;
import com.ingeniapps.cctulua.adapter.LocalAdapter;
import com.ingeniapps.cctulua.beans.Local;
import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.volley.ControllerSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class fragment_ccomercial extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener
{
    private SliderLayout mDemoSlider;

    private gestionSharedPreferences sharedPreferences;
    private ArrayList<Local> listadoLocales;
    private RecyclerView recycler_view_locales;
    private LocalAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    RelativeLayout relativeLayoutEsperaCarga;
    RelativeLayout relativeLayoutCargaLocales;

    private int pagina;
    Context context;
    private boolean solicitando=false;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    private ImageView not_found_empleados;

    private ProgressDialog progressDialog;

    EditText editTextBusquedaCompañero;
    TextView editTextNumEmpleados;
    private String idCategoria;

    DividerItemDecoration mDividerItemDecoration;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    vars vars;
    com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        vars=new vars();
        listadoLocales=new ArrayList<Local>();
        vars=new vars();
        context = getActivity();
        gestionSharedPreferences=new gestionSharedPreferences(fragment_ccomercial.this.getActivity());
    }

    public fragment_ccomercial()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        //setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_ccomercial, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mDemoSlider = (SliderLayout)getActivity().findViewById(R.id.slider);

        relativeLayoutEsperaCarga=getActivity().findViewById(R.id.relativeLayoutEsperaCarga);
        relativeLayoutCargaLocales=getActivity().findViewById(R.id.relativeLayoutCargaLocales);

        HashMap<String,String> url_maps = new HashMap<String, String>();

        recycler_view_locales=(RecyclerView) getActivity().findViewById(R.id.recycler_view_locales);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new LocalAdapter(getActivity(),listadoLocales, new LocalAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Local local)
            {
                Intent i=new Intent(fragment_ccomercial.this.getActivity(), DetalleLocal.class);
                i.putExtra("codLocal",local.getCodLocal());
                startActivity(i);
            }
        });
        recycler_view_locales.setHasFixedSize(true);
        recycler_view_locales.setLayoutManager(mLayoutManager);
        recycler_view_locales.setItemAnimator(new DefaultItemAnimator());
        recycler_view_locales.setAdapter(mAdapter);

        WebServiceGetEventos();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_busqueda).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_buscar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.menu_busqueda:
            {
                Intent buscar=new Intent(fragment_ccomercial.this.getActivity(),Buscar.class);
                startActivity(buscar);
                break;
            }
        }
        return true;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mDemoSlider.startAutoCycle();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mDemoSlider.stopAutoCycle();
    }

    @Override
    public void onStop()
    {
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider)
    {
        //Toast.makeText(getActivity(),slider.getBundle().get("codEvento") + "",Toast.LENGTH_SHORT).show();
        Intent i=new Intent(fragment_ccomercial.this.getActivity(), DetalleEvento.class);
        i.putExtra("codEvento",""+slider.getBundle().get("codEvento"));
        startActivity(i);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("getDatosEventos");
        ControllerSingleton.getInstance().cancelPendingReq("getDatosLocales");
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
    }

    @Override
    public void onPageSelected(int position)
    {
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {
    }

    private void WebServiceGetEventos()
    {
        String _urlWebService = vars.ipServer.concat("/ws/getEventos");

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

                                JSONArray listaEventos = response.getJSONArray("eventos");
                                long timestamp;
                                CharSequence timeAgo;

                                for (int i = 0; i < listaEventos.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaEventos.get(i);

                                    timestamp = Long.parseLong(jsonObject.getString("fecExpiraEvento")) * 1000L;
                                    timeAgo = DateUtils.getRelativeTimeSpanString(timestamp,
                                            System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                                    TextSliderView textSliderView = new TextSliderView(fragment_ccomercial.this.getActivity());
                                    textSliderView
                                            .description("" + timeAgo)
                                            .image(jsonObject.getString("imgEvento"))
                                            .setScaleType(BaseSliderView.ScaleType.Fit)
                                            .setOnSliderClickListener((BaseSliderView.OnSliderClickListener)fragment_ccomercial.this);
                                    //add your extra information
                                    textSliderView.bundle(new Bundle());
                                    textSliderView.getBundle()
                                            .putString("codEvento", jsonObject.getString("codEvento"));
                                    mDemoSlider.addSlider(textSliderView);
                                }

                                mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                                mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                                mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                                mDemoSlider.setDuration(5000);
                                mDemoSlider.addOnPageChangeListener((ViewPagerEx.OnPageChangeListener) fragment_ccomercial.this);
                                mDemoSlider.setPresetTransformer("ZoomOut");

                                WebServiceGetLocales();
                            }
                        }
                        catch (JSONException e)
                        {
                            /*layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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
              /*  headers.put("buscar", TextUtils.isEmpty(busqueda)?"":busqueda);
                headers.put("categoria", TextUtils.isEmpty(codEmpleado)?"":codEmpleado);*/
                //headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDatosEventos");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceGetLocales()
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
                                relativeLayoutEsperaCarga.setVisibility(View.GONE);
                                relativeLayoutCargaLocales.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (JSONException e)
                        {
                            /*layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ccomercial.this.getActivity(),R.style.AlertDialogTheme));
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
              /*  headers.put("buscar", TextUtils.isEmpty(busqueda)?"":busqueda);
                headers.put("categoria", TextUtils.isEmpty(codEmpleado)?"":codEmpleado);*/
                //headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDatosLocales");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
