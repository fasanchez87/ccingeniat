package com.ingeniapps.cctulua.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import com.ingeniapps.cctulua.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ingeniapps.cctulua.activity.DetallePelicula;
import com.ingeniapps.cctulua.adapter.CarteleraAdapter;
import com.ingeniapps.cctulua.beans.Pelicula;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.volley.ControllerSingleton;

public class fragment_cartelera extends Fragment
{
    private gestionSharedPreferences sharedPreferences;
    private ArrayList<Pelicula> listadoPeliculas;
    public vars vars;
    private RecyclerView recycler_view_cartelera;
    private CarteleraAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    RelativeLayout layoutEspera;
    RelativeLayout layoutMacroEsperaPeliculas;
    ImageView not_found_peliculas;
    private int pagina;
    Context context;
    private boolean solicitando=false;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    private LinearLayout linear_cartelera;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        sharedPreferences=new gestionSharedPreferences(getActivity().getApplicationContext());
        listadoPeliculas=new ArrayList<Pelicula>();
        vars=new vars();
        context = getActivity();
        pagina=0;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        //not_found_peliculas=(ImageView)getActivity().findViewById(R.id.not_found_peliculas);
        //not_found_noticias.setVisibility(View.VISIBLE);
        layoutEspera=(RelativeLayout)getActivity().findViewById(R.id.layoutEsperaHistorialCartelera);
        linear_cartelera=(LinearLayout) getActivity().findViewById(R.id.linear_cartelera);
        layoutMacroEsperaPeliculas=(RelativeLayout)getActivity().findViewById(R.id.layoutMacroEsperaCartelera);
        recycler_view_cartelera=(RecyclerView) getActivity().findViewById(R.id.recycler_view_cartelera);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mAdapter = new CarteleraAdapter(getActivity(),listadoPeliculas, new CarteleraAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Pelicula pelicula)
            {
                Intent i=new Intent(getActivity(), DetallePelicula.class);
                i.putExtra("codPelicula",pelicula.getCodPelicula());
                i.putExtra("trailerPelicula",pelicula.getTrailerPelicula());
                startActivity(i);
            }
        });

        recycler_view_cartelera.setHasFixedSize(true);
        recycler_view_cartelera.setLayoutManager(mLayoutManager);
        recycler_view_cartelera.setItemAnimator(new DefaultItemAnimator());
        recycler_view_cartelera.setAdapter(mAdapter);


/*
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager)
        {
            @Override
            public void onLoadMore(final int page, final int totalItemsCount, final RecyclerView view)
            {
                final int curSize = mAdapter.getItemCount();

                view.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(!solicitando)
                        {
                            WebServiceGetNoticiasMore();
                        }
                    }
                });
            }
        };*/

        //VERSION APP
        try
        {
            versionActualApp=getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        //recycler_view_noticias.addOnScrollListener(scrollListener);
        WebServiceGetCartelera();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("getDatosCartelera");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cartelera, container, false);
    }

    private void WebServiceGetCartelera()
    {
        listadoPeliculas.clear();
        String _urlWebService = vars.ipServer.concat("/ws/getCartelera");

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
                                JSONArray listaPeliculas = response.getJSONArray("peliculas");

                                for (int i = 0; i < listaPeliculas.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaPeliculas.get(i);

                                    Pelicula pelicula = new Pelicula();
                                    //AGREGAMOS PARA PONER EL PROGRESS AL FINAL DE PANTALLA
                                    //noticia.setType(jsonObject.getString("type"));//type==evento
                                    //DATOS EVENTO
                                    pelicula.setCodPelicula(jsonObject.getString("codPelicula"));
                                    pelicula.setNomPelicula(jsonObject.getString("nomPelicula"));
                                    pelicula.setDimenPelicula(jsonObject.getString("dimenPelicula"));
                                    pelicula.setLenguaPelicula(jsonObject.getString("lenguaPelicula"));
                                    pelicula.setHoraPelicula(jsonObject.getString("horaPelicula"));
                                    pelicula.setIndEstado(jsonObject.getString("indEstado"));
                                    pelicula.setImaPelicula(jsonObject.getString("imaPelicula"));
                                    pelicula.setType(jsonObject.getString("type"));
                                    pelicula.setTrailerPelicula(TextUtils.isEmpty(jsonObject.getString("trailerPelicula"))?null:jsonObject.getString("trailerPelicula").substring(jsonObject.getString("trailerPelicula").lastIndexOf("=") + 1));
                                    listadoPeliculas.add(pelicula);
                                }

                                layoutMacroEsperaPeliculas.setVisibility(View.GONE);
                                linear_cartelera.setVisibility(View.VISIBLE);
                            }

                            else
                            {

                            }
                        }
                        catch (JSONException e)
                        {

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_cartelera.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_cartelera.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_cartelera.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_cartelera.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_cartelera.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_cartelera.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_cartelera.this.getActivity(),R.style.AlertDialogTheme));
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
                headers.put("codUsuario", sharedPreferences.getString("codUsuario"));
               /* headers.put("codUsuario", sharedPreferences.getString("codUsuario"));
                headers.put("tokenFCM", sharedPreferences.getString("tokenFCM"));
                headers.put("versionApp",versionActualApp);*/
                headers.put("MyToken", sharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDatosCartelera");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }



}