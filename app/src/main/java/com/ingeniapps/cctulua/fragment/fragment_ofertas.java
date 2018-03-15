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
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ingeniapps.cctulua.activity.DetalleOferta;
import com.ingeniapps.cctulua.adapter.LocalAdapter;
import com.ingeniapps.cctulua.adapter.OfertaAdapter;
import com.ingeniapps.cctulua.beans.Local;
import com.ingeniapps.cctulua.beans.Oferta;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.vars.vars;
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
public class fragment_ofertas extends Fragment
{
    private com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences sharedPreferences;
    private ArrayList<Oferta> listadoOfertas;
    private RecyclerView recycler_view_ofertas;
    private OfertaAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    RelativeLayout relativeLayoutEsperaCarga;
    RelativeLayout layoutSinOfertas;
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
    com.ingeniapps.cctulua.vars.vars vars;
    com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        vars=new vars();
        listadoOfertas=new ArrayList<Oferta>();
        vars=new vars();
        context = getActivity();
        gestionSharedPreferences=new gestionSharedPreferences(fragment_ofertas.this.getActivity());
    }

    public fragment_ofertas()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_oferta, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        relativeLayoutEsperaCarga=getActivity().findViewById(R.id.relativeLayoutEsperaCarga);
        layoutSinOfertas=(RelativeLayout) getActivity().findViewById(R.id.layoutSinOfertas);
        relativeLayoutCargaLocales=getActivity().findViewById(R.id.relativeLayoutCargaLocales);
        recycler_view_ofertas=(RecyclerView) getActivity().findViewById(R.id.recycler_view_ofertas);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new OfertaAdapter(getActivity(),listadoOfertas, new OfertaAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Oferta oferta)
            {
                Intent i=new Intent(fragment_ofertas.this.getActivity(), DetalleOferta.class);
                i.putExtra("codOferta",oferta.getCodOferta());
                startActivity(i);
            }
        });
        recycler_view_ofertas.setHasFixedSize(true);
        recycler_view_ofertas.setLayoutManager(mLayoutManager);
        recycler_view_ofertas.setItemAnimator(new DefaultItemAnimator());
        recycler_view_ofertas.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("getDatosOfertas");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        WebServiceGetOfertas();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    private void WebServiceGetOfertas()
    {
        String _urlWebService = vars.ipServer.concat("/ws/getOfertas");

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
                                listadoOfertas.clear();

                                JSONArray listaOfertas = response.getJSONArray("ofertas");
                                long timestamp;
                                CharSequence timeAgo;

                                for (int i = 0; i < listaOfertas.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaOfertas.get(i);
                                    Oferta oferta = new Oferta();
                                    oferta.setType(jsonObject.getString("type"));
                                    oferta.setCodOferta(jsonObject.getString("codOferta"));
                                    oferta.setNomOferta(jsonObject.getString("nomOferta"));
                                    oferta.setImaOferta(jsonObject.getString("imaOferta"));
                                    oferta.setDesOferta(jsonObject.getString("desOferta"));
                                    oferta.setDescOferta(jsonObject.getString("descOferta"));//DESCUENTO DE OFERTA
                                    oferta.setFecPubOferta(jsonObject.getString("fecPubOferta"));
                                    oferta.setFecExpOferta(jsonObject.getString("fecExpOferta"));
                                    oferta.setIndEstado(jsonObject.getString("indEstado"));
                                    oferta.setNomLocal(jsonObject.getString("nomLocal"));
                                    oferta.setImgLocal(jsonObject.getString("imgLocal"));
                                    oferta.setUbicacionLocal(jsonObject.getString("ubicacionLocal"));

                                    if(oferta.getIndEstado().equals("1"))
                                    {
                                        listadoOfertas.add(oferta);
                                    }
                                }

                                relativeLayoutEsperaCarga.setVisibility(View.GONE);
                                relativeLayoutCargaLocales.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                relativeLayoutEsperaCarga.setVisibility(View.GONE);
                                relativeLayoutCargaLocales.setVisibility(View.GONE);
                                layoutSinOfertas.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (JSONException e)
                        {
                            /*layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ofertas.this.getActivity(),R.style.AlertDialogTheme));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ofertas.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ofertas.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ofertas.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ofertas.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ofertas.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_ofertas.this.getActivity(),R.style.AlertDialogTheme));
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

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDatosOfertas");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
