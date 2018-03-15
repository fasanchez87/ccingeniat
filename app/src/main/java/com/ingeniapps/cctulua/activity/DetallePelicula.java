package com.ingeniapps.cctulua.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.app.Config;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class DetallePelicula extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener
{
    DividerItemDecoration mDividerItemDecoration;
    ImageView favovitoOff;
    ImageView favovitoOn;

    private String codPelicula;
    private String trailerPelicula;
    private boolean isNotifyPush=false;
    public com.ingeniapps.cctulua.vars.vars vars;
    private int meGusta;

    private LinearLayout ll_espera_pelicula;
    private NestedScrollView scrollDetallePelicula;
    private ImageView imagenDetallePelicula,imageViewCharedPelicula;
    private TextView nomPelicula,genPelicula,durPelicula,dirPelicula,actoresPelicula, fecPelicula,claPelicula,desPelicula;
    com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    //Button buttonCheckinEnable,buttonCheckinDisable;

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    // YouTube player view
    private YouTubePlayerView youTubeView;

    private long timeVisto;
    private long start;



    public static String vidPelicula;
    private String video;

    private HashMap<String,String> imagesSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalle_pelicula);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Fabric.with(this, new Crashlytics());

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        // Initializing video player with developer key
        youTubeView.initialize(Config.DEVELOPER_KEY, this);

        YouTubePlayerSupportFragment  mYoutubePlayerFragment = new YouTubePlayerSupportFragment();



        timeVisto=0;
        start = System.currentTimeMillis();

        gestionSharedPreferences=new gestionSharedPreferences(this);

        this.video=null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        /*setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);*/

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                codPelicula=null;
                isNotifyPush=false;
                trailerPelicula=null;
            }

            else
            {
                codPelicula=extras.getString("codPelicula");
                trailerPelicula=extras.getString("trailerPelicula");
                isNotifyPush=extras.getBoolean("isNotifyPush");
            }
        }

        Log.i("codPelicula",""+codPelicula);


      /*  toolbar.setNavigationOnClickListener(new View.OnClickListener()
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
                        Intent i=new Intent(DetallePelicula.this, Principal.class);
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
        });*/

        vars=new vars();
        ll_espera_pelicula=(LinearLayout)findViewById(R.id.ll_espera_detalle_pelicula);
        scrollDetallePelicula=(NestedScrollView)findViewById(R.id.scrollDetallePelicula);
        imagenDetallePelicula=(ImageView)findViewById(R.id.imagenDetallePelicula);
        imageViewCharedPelicula=(ImageView)findViewById(R.id.imageViewCharedPelicula);

        nomPelicula=(TextView)findViewById(R.id.nombrePelicula);
        genPelicula=(TextView)findViewById(R.id.genPelicula);
        durPelicula=(TextView)findViewById(R.id.durPelicula);
        dirPelicula=(TextView)findViewById(R.id.dirPelicula);
        actoresPelicula=(TextView)findViewById(R.id.actoresPelicula);
        fecPelicula=(TextView)findViewById(R.id.fecExtrenoPelicula);
        claPelicula=(TextView)findViewById(R.id.claPelicula);
        desPelicula=(TextView)findViewById(R.id.descPelicula);

        if(isNotifyPush)//SI ES NOTIFICADO POR PUSH, HABILITAMOS EL BUTTON CHECKIN
        {
            //  buttonCheckinEnable.setVisibility(View.VISIBLE);
        }

        favovitoOff=(ImageView) findViewById(R.id.imageViewMeGustaConvenioOffPelicula);
        favovitoOn=(ImageView) findViewById(R.id.imageViewMeGustaConvenioOnPelicula);

        if(gestionSharedPreferences.getInt("like_punto"+codPelicula)!=0)
        {
            if(gestionSharedPreferences.getInt("like_punto"+codPelicula)==1)
            {
                favovitoOff.setImageResource(R.drawable.ic_favorito);
            }
            else
            {
                favovitoOff.setImageResource(R.drawable.ic_favorito_off);
            }
        }

        imageViewCharedPelicula.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Centro Comercial Tuluá - La 14");
                    String sAux = "\n"+nomPelicula.getText()+"\n"+fecPelicula.getText()+"\n"+""+desPelicula.getText()+"."+"\n";
                    sAux = sAux + "https://npjk9.app.goo.gl/cctulua \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    v.getContext().startActivity(Intent.createChooser(i, "Comparte esta película justo ahora!"));
                }
                catch(Exception e)
                {
                    //e.toString();
                }
            }
        });

        favovitoOff.setOnClickListener(new View.OnClickListener()
        {
            int meGusta = gestionSharedPreferences.getInt("like_punto"+codPelicula);
            public void onClick(View v)
            {
                if (meGusta==0)
                {
                    favovitoOff.setImageResource(R.drawable.ic_favorito);
                    meGusta=1;
                    animateHeart(favovitoOff);
                    gestionSharedPreferences.putInt("like_punto"+codPelicula,meGusta);
                    //WebServiceSetMeGusta(""+meGusta);
                }
                else
                if (meGusta==1)
                {
                    favovitoOff.setImageResource(R.drawable.ic_favorito_off);
                    meGusta=0;
                    animateHeart(favovitoOff);
                    gestionSharedPreferences.putInt("like_punto"+codPelicula,meGusta);
                    //WebServiceSetMeGusta(""+meGusta);
                }
            }
        });

        WebServiceGetDetallePelicula(codPelicula);
    }

    public static String getVidPelicula() {
        return vidPelicula;
    }

    public static void setVidPelicula(String vidPelicula) {
        DetallePelicula.vidPelicula = vidPelicula;
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
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "setTokenFCMDetallePelicula");
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
                    Intent i=new Intent(DetallePelicula.this, Principal.class);
                    startActivity(i);
                    isNotifyPush=false;
                    DetallePelicula.this.finish();
                }
                else
                {
                    Log.i("notificado","notificado por push - principal activa");
                    DetallePelicula.this.finish();
                }
            }
            else
            {
                DetallePelicula.this.finish();
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
        ControllerSingleton.getInstance().cancelPendingReq("getDetallePelicula");
        ControllerSingleton.getInstance().cancelPendingReq("setTokenFCMDetallePelicula");

    }

    @Override
    public void onStop()
    {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        super.onStop();
    }

    private void WebServiceGetDetallePelicula(final String codPelicula)
    {
        String _urlWebService= vars.ipServer.concat("/ws/getDetallePelicula");

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            boolean status=response.getBoolean("status");
                            String peli;

                            long timestamp;
                            CharSequence timeAgo;

                            if(status)
                            {
                                Glide.with(DetallePelicula.this).
                                        load("" + response.getString("imaPelicula")).
                                        thumbnail(0.5f).into(imagenDetallePelicula);

                                nomPelicula.setText(response.getString("nomPelicula"));
                                durPelicula.setText(response.getString("durPelicula")+" min");
                                dirPelicula.setText(response.getString("dirPelicula"));
                                actoresPelicula.setText(response.getString("actPelicula"));
                                desPelicula.setText(response.getString("desPelicula"));
                                timestamp = Long.parseLong(response.getString("fecEstPelicula")) * 1000L;
                                timeAgo = DateUtils.getRelativeTimeSpanString(timestamp,
                                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                                fecPelicula.setText(timeAgo);

                                ll_espera_pelicula.setVisibility(View.GONE);
                                scrollDetallePelicula.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ll_espera_pelicula.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetallePelicula.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage("Error obteniendo detalle de la Película")
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
                            ll_espera_pelicula.setVisibility(View.GONE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetallePelicula.this,R.style.AlertDialogTheme));
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
                        ll_espera_pelicula.setVisibility(View.GONE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetallePelicula.this,R.style.AlertDialogTheme));
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
                            ll_espera_pelicula.setVisibility(View.GONE);

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
                            ll_espera_pelicula.setVisibility(View.GONE);

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
                            ll_espera_pelicula.setVisibility(View.GONE);

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
                            ll_espera_pelicula.setVisibility(View.GONE);

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
                            ll_espera_pelicula.setVisibility(View.GONE);

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
                            ll_espera_pelicula.setVisibility(View.GONE);

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
                headers.put("codPelicula", codPelicula);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDetallePelicula");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player, boolean wasRestored)
    {
        if (!wasRestored)
        {
            if (trailerPelicula!= null)
            {
                // loadVideo() will auto play video
                // Use cueVideo() method, if you don't want to play it automatically

                player.cueVideo("" + trailerPelicula);
                // Hiding player controls
                player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
            /*}*/
            }

        }
    }
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason)
    {
        if (errorReason.isUserRecoverableError())
        {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        }
        else
        {
            String errorMessage = String.format(
                    getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(Config.DEVELOPER_KEY, this);
        }
    }

    private YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_view);
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