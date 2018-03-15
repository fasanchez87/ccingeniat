package com.ingeniapps.cctulua.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.ingeniapps.cctulua.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.volley.ControllerSingleton;
import com.ingeniapps.cctulua.vars.vars;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;


public class DetalleOferta extends AppCompatActivity
{
    DividerItemDecoration mDividerItemDecoration;
    ImageView favovitoOff;
    ImageView favovitoOn;

    private String codOferta;
    private boolean isNotifyPush=false;
    public vars vars;
    private int meGusta;

    private LinearLayout ll_espera_oferta;
    private NestedScrollView scrollDetalleOferta;
    private ImageView imagenDetalleOferta,imageViewCharedOferta;
    private TextView nomLocal,nomOferta,fecExpiraOferta,descOferta,textViewDescuentoOferta;
    com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    //Button buttonCheckinEnable,buttonCheckinDisable;

    private long timeVisto;
    private long start;

    private HashMap<String,String> imagesSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalle_oferta);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Fabric.with(this, new Crashlytics());


        timeVisto=0;
        start = System.currentTimeMillis();

        gestionSharedPreferences=new gestionSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                codOferta=null;
                isNotifyPush=false;
            }

            else
            {
                codOferta=extras.getString("codOferta");
                isNotifyPush=extras.getBoolean("isNotifyPush");
            }
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(isNotifyPush)
                {
                    if(gestionSharedPreferences.getBoolean("isActivePrincipal"))
                    {
                        finish();
                    }
                    else
                    {
                        Intent i=new Intent(DetalleOferta.this, Principal.class);
                        startActivity(i);
                        isNotifyPush=false;
                        finish();
                    }
                }
                else
                {
                    finish();
                }
            }
        });

        vars=new vars();
        ll_espera_oferta=(LinearLayout)findViewById(R.id.ll_espera_oferta);
        scrollDetalleOferta=(NestedScrollView)findViewById(R.id.scrollDetalleOferta);
        imagenDetalleOferta=(ImageView)findViewById(R.id.imagenDetalleOferta);
        imageViewCharedOferta=(ImageView)findViewById(R.id.imageViewCharedOferta);
        nomOferta=(TextView)findViewById(R.id.nomOferta);
        descOferta=(TextView)findViewById(R.id.descOferta);
        textViewDescuentoOferta=(TextView)findViewById(R.id.textViewDescuentoOferta);
        fecExpiraOferta=(TextView)findViewById(R.id.fecExpiraOferta);
        nomLocal=(TextView)findViewById(R.id.nombreLocalOferta);

        if(isNotifyPush)//SI ES NOTIFICADO POR PUSH, HABILITAMOS EL BUTTON CHECKIN
        {
          //  buttonCheckinEnable.setVisibility(View.VISIBLE);
        }

        favovitoOff=(ImageView) findViewById(R.id.imageViewMeGustaConvenioOffOferta);
        favovitoOn=(ImageView) findViewById(R.id.imageViewMeGustaConvenioOnOferta);

        if(gestionSharedPreferences.getInt("like_punto"+codOferta)!=0)
        {
            if(gestionSharedPreferences.getInt("like_punto"+codOferta)==1)
            {
                favovitoOff.setImageResource(R.drawable.ic_favorito);
            }
            else
            {
                favovitoOff.setImageResource(R.drawable.ic_favorito_off);
            }
        }

        imageViewCharedOferta.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Centro Comercial Tuluá - La 14");
                    String sAux = "\n"+nomLocal.getText()+"\n"+nomOferta.getText()+"\n"+""+descOferta.getText()+"."+"\n";
                    sAux = sAux + "https://npjk9.app.goo.gl/cctulua \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    v.getContext().startActivity(Intent.createChooser(i, "Comparte esta oferta justo ahora!"));
                }
                catch(Exception e)
                {
                    //e.toString();
                }
            }
        });

        favovitoOff.setOnClickListener(new View.OnClickListener()
        {
            int meGusta = gestionSharedPreferences.getInt("like_punto"+codOferta);
            public void onClick(View v)
            {
                if (meGusta==0)
                {
                    favovitoOff.setImageResource(R.drawable.ic_favorito);
                    meGusta=1;
                    animateHeart(favovitoOff);
                    gestionSharedPreferences.putInt("like_punto"+codOferta,meGusta);
                    //WebServiceSetMeGusta(""+meGusta);
                }
                else
                if (meGusta==1)
                {
                    favovitoOff.setImageResource(R.drawable.ic_favorito_off);
                    meGusta=0;
                    animateHeart(favovitoOff);
                    gestionSharedPreferences.putInt("like_punto"+codOferta,meGusta);
                    //WebServiceSetMeGusta(""+meGusta);
                }
            }
        });

        WebServiceGetDetalleOferta(codOferta);
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
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", ""+ FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "setTokenFCMDetalleOferta");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(isNotifyPush)
            {
                if(!gestionSharedPreferences.getBoolean("isActivePrincipal"))
                {
                    Log.i("notificado","notificado por push - principal no esta activa");
                    Intent i=new Intent(DetalleOferta.this, Principal.class);
                    startActivity(i);
                    isNotifyPush=false;
                    DetalleOferta.this.finish();
                }
                else
                {
                    Log.i("notificado","notificado por push - principal activa");
                    DetalleOferta.this.finish();
                }
            }
            else
            {
                DetalleOferta.this.finish();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void animateHeart(final ImageView view)
    {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        prepareAnimation(scaleAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        prepareAnimation(alphaAnimation);

        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(alphaAnimation);
        animation.addAnimation(scaleAnimation);
        animation.setDuration(300);
        //animation.setFillAfter(true);
        view.startAnimation(animation);

    }

    private Animation prepareAnimation(Animation animation)
    {
        animation.setRepeatCount(2);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateTokenFCMToServer();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.i("estado","pause");
        timeVisto=((System.currentTimeMillis() - start) / 1000l);
        //WebServiceSetVisto();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        timeVisto=((System.currentTimeMillis() - start) / 1000l);
        //WebServiceSetVisto();
        Log.i("time",""+timeVisto);
        ControllerSingleton.getInstance().cancelPendingReq("getDetalleOferta");
        ControllerSingleton.getInstance().cancelPendingReq("setTokenFCMDetalleOferta");
    }

    @Override
    public void onStop()
    {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        super.onStop();
    }

    private void WebServiceGetDetalleOferta(final String codOferta)
    {
        String _urlWebService= vars.ipServer.concat("/ws/getDetalleOferta");

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            boolean status=response.getBoolean("status");

                            long timestamp;
                            CharSequence timeAgo;

                            if(status)
                            {

                                Log.i("codOferta","codOferta: "+codOferta);


                                Glide.with(DetalleOferta.this).
                                        load("" + response.getString("imaOferta")).
                                        thumbnail(0.5f).into(imagenDetalleOferta);

                                nomLocal.setText(response.getString("nomLocal"));

                                if(TextUtils.equals(response.getString("descOferta").toString(),"0"))
                                {
                                    textViewDescuentoOferta.setVisibility(View.GONE);
                                }
                                else
                                {
                                    textViewDescuentoOferta.setText(response.getString("descOferta")+"%");
                                }

                                nomOferta.setText(response.getString("nomOferta"));
                                descOferta.setText(response.getString("desOferta"));

                                timestamp = Long.parseLong(response.getString("fecExpOferta")) * 1000L;
                                timeAgo = DateUtils.getRelativeTimeSpanString(timestamp,
                                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                                fecExpiraOferta.setText("Oferta expira "+timeAgo);

                                ll_espera_oferta.setVisibility(View.GONE);
                                scrollDetalleOferta.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ll_espera_oferta.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleOferta.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage("Error obteniendo detalle de la Oferta")
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {

                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                        }
                        catch (JSONException e)
                        {
                            ll_espera_oferta.setVisibility(View.GONE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleOferta.this,R.style.AlertDialogTheme));
                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        ll_espera_oferta.setVisibility(View.GONE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleOferta.this,R.style.AlertDialogTheme));
                        builder
                                .setTitle(R.string.title)
                                .setMessage(error.toString())
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                    }
                                }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));

                        if (error instanceof TimeoutError)
                        {
                            ll_espera_oferta.setVisibility(View.GONE);

                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NoConnectionError)
                        {
                            ll_espera_oferta.setVisibility(View.GONE);

                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            ll_espera_oferta.setVisibility(View.GONE);

                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            ll_espera_oferta.setVisibility(View.GONE);

                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NetworkError)
                        {
                            ll_espera_oferta.setVisibility(View.GONE);

                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof ParseError)
                        {
                            ll_espera_oferta.setVisibility(View.GONE);

                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
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
                headers.put("codOferta", codOferta);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDetalleOferta");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

   /* private void WebServiceSetMeGusta(final String indLike)
    {
        String _urlWebService= vars.ipServer.concat("/ws/setLikePuntoConvenio");

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
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
                headers.put("indLike", indLike);
                headers.put("codEmpleado",gestionSharedPreferences.getString("codEmpleado"));
                headers.put("codPunto", codPunto);
                headers.put("indLike", indLike);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceCheckin(final String codEmpleado, final String codPunto)
    {
        String _urlWebService= vars.ipServer.concat("/ws/CheckInEmpleadoConvenio");

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
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
                headers.put("codEmpleado",codEmpleado);
                headers.put("codPunto", codPunto);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceSetVisto()
    {
        String _urlWebService= vars.ipServer.concat("/ws/setVistoPuntoConvenio");
        Log.i("time","espera"+timeVisto);


        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        Log.i("time","bien"+timeVisto);

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.i("time","mal"+timeVisto);

                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado",gestionSharedPreferences.getString("codEmpleado"));
                headers.put("codPunto", codPunto);
                headers.put("tiempoVisto", ""+timeVisto);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }*/
}
