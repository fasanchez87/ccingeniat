package com.ingeniapps.cctulua.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.volley.ControllerSingleton;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yanzhenjie.permission.SettingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class DetalleLocal extends AppCompatActivity implements RationaleListener
{
    DividerItemDecoration mDividerItemDecoration;
    ImageView favovitoOff;
    ImageView favovitoOn;

    private String codLocal;
    private boolean isNotifyPush=false;
    public com.ingeniapps.cctulua.vars.vars vars;
    private int meGusta;

    private Spannable span;
    private int colorLink=Color.parseColor("#00599E");
    private static final int REQUEST_PHONE_CALL = 1;
    private static final int REQUEST_CODE_SETTING = 300;
    private String datoLlamarLocal;


    private LinearLayout ll_espera_local;
    private NestedScrollView scrollDetalleLocal;
    private ImageView imagenDetalleLocal,imageViewCharedLocal;
    private TextView nomLocal,imaLocal,descLocal,dirLocal,telLocal;
    com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    //Button buttonCheckinEnable,buttonCheckinDisable;

    private long timeVisto;
    private long start;

    private HashMap<String,String> imagesSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalle_local);
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
                codLocal=null;
                isNotifyPush=false;
            }

            else
            {
                codLocal=extras.getString("codLocal");
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
                        Intent i=new Intent(DetalleLocal.this, Principal.class);
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
        ll_espera_local=(LinearLayout)findViewById(R.id.ll_espera_detalle_local);
        scrollDetalleLocal=(NestedScrollView)findViewById(R.id.scrollDetalleLocal);
        imagenDetalleLocal=(ImageView)findViewById(R.id.imagenDetalleLocal);
        imageViewCharedLocal=(ImageView)findViewById(R.id.imageViewCharedLocal);
        nomLocal=(TextView)findViewById(R.id.nomLocal);
        descLocal=(TextView)findViewById(R.id.descLocal);
        dirLocal=(TextView)findViewById(R.id.textViewDireccionLocal);
        telLocal=(TextView)findViewById(R.id.textViewTelefonoLocal);

        telLocal.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if (ContextCompat.checkSelfPermission(DetalleLocal.this, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED)
                    {
                        //PERMISO TELEFONO
                        AndPermission.with(DetalleLocal.this)
                                .requestCode(REQUEST_PHONE_CALL)
                                .permission(
                                        // Multiple permissions, array form.
                                        Manifest.permission.CALL_PHONE
                                )
                                .callback(permissionListener)
                                .rationale(new RationaleListener()
                                {
                                    @Override
                                    public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
                                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(DetalleLocal.this, R.style.AlertDialogTheme));
                                        builder
                                                .setTitle("Permiso de Télefono")
                                                .setMessage("Para lograr llamar al comercio es necesario que concedas permisos de llamada, para ello presiona el botón ACEPTAR.")
                                                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        rationale.resume();
                                                    }
                                                }).setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                rationale.cancel();
                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                                setTextColor(getResources().getColor(R.color.colorPrimary));
                                    }
                                })
                                .start();
                    }
                    else
                    {
                        llamar(getDatoLlamarLocal());
                    }
                }
                else
                {
                    llamar(getDatoLlamarLocal());
                }
            }
        });

        if(isNotifyPush)//SI ES NOTIFICADO POR PUSH, HABILITAMOS EL BUTTON CHECKIN
        {
            //  buttonCheckinEnable.setVisibility(View.VISIBLE);
        }

        favovitoOff=(ImageView) findViewById(R.id.imageViewMeGustaConvenioOffLocal);
        favovitoOn=(ImageView) findViewById(R.id.imageViewMeGustaConvenioOnLocal);

        if(gestionSharedPreferences.getInt("like_punto"+codLocal)!=0)
        {
            if(gestionSharedPreferences.getInt("like_punto"+codLocal)==1)
            {
                favovitoOff.setImageResource(R.drawable.ic_favorito);
            }
            else
            {
                favovitoOff.setImageResource(R.drawable.ic_favorito_off);
            }
        }

        imageViewCharedLocal.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Centro Comercial Tuluá - La 14");
                    String sAux = "\n"+nomLocal.getText()+"\n"+dirLocal.getText()+"\n"+""+telLocal.getText()+"."+"\n";
                    sAux = sAux + "https://npjk9.app.goo.gl/cctulua \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    v.getContext().startActivity(Intent.createChooser(i, "Comparte este local justo ahora!"));
                }
                catch(Exception e)
                {
                    //e.toString();
                }
            }
        });

        favovitoOff.setOnClickListener(new View.OnClickListener()
        {
            int meGusta = gestionSharedPreferences.getInt("like_local"+codLocal);
            public void onClick(View v)
            {
                if (meGusta==0)
                {
                    favovitoOff.setImageResource(R.drawable.ic_favorito);
                    meGusta=1;
                    animateHeart(favovitoOff);
                    gestionSharedPreferences.putInt("like_local"+codLocal,meGusta);
                    //WebServiceSetMeGusta(""+meGusta);
                }
                else
                if (meGusta==1)
                {
                    favovitoOff.setImageResource(R.drawable.ic_favorito_off);
                    meGusta=0;
                    animateHeart(favovitoOff);
                    gestionSharedPreferences.putInt("like_local"+codLocal,meGusta);
                    //WebServiceSetMeGusta(""+meGusta);
                }
            }
        });

        WebServiceGetDetalleLocal(codLocal);
    }

    public String getDatoLlamarLocal() {
        return datoLlamarLocal;
    }

    public void setDatoLlamarLocal(String datoLlamarLocal) {
        this.datoLlamarLocal = datoLlamarLocal;
    }

    private void llamar(String number)
    {
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(phoneIntent);
    }



    private PermissionListener permissionListener = new PermissionListener()
    {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions)
        {
            switch (requestCode)
            {
                case REQUEST_PHONE_CALL:
                {
                    llamar(getDatoLlamarLocal());
                    break;
                }
            }
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions)
        {
            switch (requestCode)
            {
                case REQUEST_PHONE_CALL:
                {
                    Toast.makeText(DetalleLocal.this, "Acceso denegado a llamadas, por favor habilite el permiso.", Toast.LENGTH_SHORT).show();
                    //getActivity().finish();
                    break;
                }
            }

            if (AndPermission.hasAlwaysDeniedPermission(DetalleLocal.this, deniedPermissions))
            {

                if(requestCode==100)
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(DetalleLocal.this,R.style.AlertDialogTheme));
                    builder
                            .setTitle("Permiso de Llamadas Telefónicas")
                            .setMessage("Vaya! parece que has denegado el acceso a llamadas. Presiona el botón Permitir, " +
                                    "selecciona la opción Accesos y habilita la opción de Permiso de llamar.")
                            .setPositiveButton("PERMITIR", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    SettingService settingService = AndPermission.defineSettingDialog(DetalleLocal.this, REQUEST_CODE_SETTING);
                                    settingService.execute();
                                }
                            }).setNegativeButton("CANCELAR", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {

                        }
                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                            setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                else

                if(requestCode==101)
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(DetalleLocal.this,R.style.AlertDialogTheme));
                    builder
                            .setTitle("Permiso de Llamadas Télefonicas")
                            .setMessage("Vaya! parece que has denegado el acceso a llamadas. Presiona el botón Permitir, " +
                                    "selecciona la opción Accesos y habilita la opción de Permiso de llamar.")
                            .setPositiveButton("PERMITIR", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    SettingService settingService = AndPermission.defineSettingDialog(DetalleLocal.this, REQUEST_CODE_SETTING);
                                    settingService.execute();
                                }
                            }).setNegativeButton("CANCELAR", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {

                        }
                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                            setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        }
    };

    @Override
    public void showRequestPermissionRationale(int requestCode, Rationale rationale)
    {

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
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "updateTokenFCMDetalleLocal");
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
                    Intent i=new Intent(DetalleLocal.this, Principal.class);
                    startActivity(i);
                    isNotifyPush=false;
                    DetalleLocal.this.finish();
                }
                else
                {
                    Log.i("notificado","notificado por push - principal activa");
                    DetalleLocal.this.finish();
                }
            }
            else
            {
                DetalleLocal.this.finish();
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
        ControllerSingleton.getInstance().cancelPendingReq("getDetalleLocal");
        ControllerSingleton.getInstance().cancelPendingReq("updateTokenFCMDetalleLocal");
    }

    @Override
    public void onStop()
    {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        super.onStop();
    }

    private void WebServiceGetDetalleLocal(final String codLocal)
    {
        String _urlWebService= vars.ipServer.concat("/ws/getDetalleLocal");

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
                                Glide.with(DetalleLocal.this).
                                        load("" + response.getString("imgLocal")).
                                       into(imagenDetalleLocal);

                                nomLocal.setText(response.getString("nomLocal"));
                                descLocal.setText(response.getString("descLocal"));

                                setDatoLlamarLocal(""+response.getString("telLocal"));

                                span=new SpannableString(""+response.getString("telLocal"));
                                span.setSpan(new ForegroundColorSpan(colorLink), 0, response.getString("telLocal").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                telLocal.setText(span);

                                dirLocal.setText("Local "+response.getString("ubicacionLocal"));

                                ll_espera_local.setVisibility(View.GONE);
                                scrollDetalleLocal.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ll_espera_local.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleLocal.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage("Error obteniendo detalle del Local")
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
                            ll_espera_local.setVisibility(View.GONE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleLocal.this,R.style.AlertDialogTheme));
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
                        ll_espera_local.setVisibility(View.GONE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleLocal.this,R.style.AlertDialogTheme));
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
                            ll_espera_local.setVisibility(View.GONE);

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
                            ll_espera_local.setVisibility(View.GONE);

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
                            ll_espera_local.setVisibility(View.GONE);

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
                            ll_espera_local.setVisibility(View.GONE);

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
                            ll_espera_local.setVisibility(View.GONE);

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
                            ll_espera_local.setVisibility(View.GONE);

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
                headers.put("codLocal", codLocal);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDetalleLocal");
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
