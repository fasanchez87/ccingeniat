package com.ingeniapps.cctulua.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerminosCondiciones extends AppCompatActivity
{
    CheckBox check_aceptar_terminos;
    TextView editTextTerminos;
    Spanned Text;
    Button buttonIngresar,buttonIngresarDisable;
    WebView myWebView;
    LinearLayout ll_espera_empresa, ll_terminos;
    private static String idDevice = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";


    public vars vars;
    private String tokenFCM;
    private Button botonLogin;
    gestionSharedPreferences gestionSharedPreferences;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Boolean guardarSesion;
    private String codUsuario;
    public String currentVersion = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminos_condiciones);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        vars=new vars();
        idDevice=getIDDevice(this);
        gestionSharedPreferences=new gestionSharedPreferences(this);
        //COMPROBAMOS LA SESION DEL USUARIO
        guardarSesion=gestionSharedPreferences.getBoolean("GuardarSesion");
        if (guardarSesion==true)
        {
            cargarActivityPrincipal();
        }
        if(checkPlayServices())
        {
            if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
            {
                tokenFCM=FirebaseInstanceId.getInstance().getToken();
            }
        }
        else
        {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(TerminosCondiciones.this,R.style.AlertDialogTheme));
            builder
                    .setTitle("Google Play Services")
                    .setMessage("Se ha encontrado un error con los servicios de Google Play, actualizalo y vuelve a ingresar.")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            finish();
                        }
                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                    setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        try
        {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        check_aceptar_terminos=(CheckBox)findViewById(R.id.check_aceptar_terminos);

        ll_espera_empresa=(LinearLayout)findViewById(R.id.ll_espera_empresa);
        ll_terminos=(LinearLayout)findViewById(R.id.ll_terminos);

        myWebView = (WebView) findViewById(R.id.webViewTerminos);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null)
        {
            // Restore the previous URL and history stack
            myWebView.restoreState(savedInstanceState);
        }

        if (Build.VERSION.SDK_INT >= 21)
        {
            myWebView.getSettings().setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW );
        }

        myWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TerminosCondiciones.this);
                String message = "Error Certificado SSL";
                switch (error.getPrimaryError())
                {
                    case SslError.SSL_UNTRUSTED:
                        message = "El certificado autorizado no es de confianza.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "El certificado ha expirado.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "El nombre de Host del certificado no coincide.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "El certificado aún no es válido.";
                        break;
                }

                message += " Deseas continuar?";

                builder.setTitle("Error Certificado SSL");
                builder.setMessage(message);
                builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url)
            {
                ll_espera_empresa.setVisibility(View.GONE);
                ll_terminos.setVisibility(View.VISIBLE);
            }
        });

        myWebView.loadUrl("https://ingeniapps.com.co/cctulua/panel/terminos.php");

        buttonIngresar=(Button)findViewById(R.id.buttonIngresar);
        buttonIngresar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(TextUtils.isEmpty(idDevice))
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(TerminosCondiciones.this,R.style.AlertDialogTheme));
                    builder
                            .setTitle(R.string.title)
                            .setMessage("No ha sido posible obtener el identificador unico UUID de su dispositivo, por favor contactenos o vuelve a intentar el ingreso.")
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                            }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                            setTextColor(getResources().getColor(R.color.colorPrimary));
                    return;
                }

                ll_terminos.setVisibility(View.GONE);
                ll_espera_empresa.setVisibility(View.VISIBLE);
                WebServiceRegistro();
            }
        });

        buttonIngresarDisable=(Button)findViewById(R.id.buttonIngresarDisable);

        check_aceptar_terminos.setOnCheckedChangeListener(null);
        //if true, your checkbox will be selected, else unselected
        //checkCiudad.setChecked(checkCiudad.isSelected());
        check_aceptar_terminos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
               if(isChecked)
               {
                   buttonIngresarDisable.setVisibility(View.GONE);
                   buttonIngresar.setVisibility(View.VISIBLE);
                   return;
               }

               else
               {
                   buttonIngresarDisable.setVisibility(View.VISIBLE);
                   buttonIngresar.setVisibility(View.GONE);
                   return;
               }
            }
        });
    }

    public void cargarActivityPrincipal()
    {
        Intent intent = new Intent(TerminosCondiciones.this, Principal.class);
        startActivity(intent);
        TerminosCondiciones.this.finish();
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

    public synchronized static String getIDDevice(Context context)
    {
        if (idDevice == null)
        {
            idDevice = UUID.randomUUID().toString();
        }

        return idDevice;
    }

    private void WebServiceRegistro()
    {
        String _urlWebService=vars.ipServer.concat("/ws/RegistroUsuario");

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            boolean status=response.getBoolean("status");
                            String message=response.getString("message");
                            /*boolean status=response.getBoolean("status");
                            boolean sesionAbierta=response.getBoolean("sesionAbierta");
                            String message=response.getString("message");*/
                            if(status)//SI NO HA INICIADO SESION Y EXISTE
                            {
                                ll_terminos.setVisibility(View.GONE);
                                ll_espera_empresa.setVisibility(View.GONE);
                                gestionSharedPreferences.putString("idDevice",""+response.getString("idDevice"));
                                Intent intent=new Intent(TerminosCondiciones.this, Principal.class);
                                gestionSharedPreferences.putBoolean("GuardarSesion", true);
                                startActivity(intent);
                                finish();
                                return;
                            }
                            else
                            {
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(TerminosCondiciones.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage(message)
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
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(TerminosCondiciones.this,R.style.AlertDialogTheme));
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
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(TerminosCondiciones.this,R.style.AlertDialogTheme));
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
                headers.put("idDevice", ""+idDevice);
                headers.put("tokenFCM", ""+tokenFCM);
                headers.put("versionApp", ""+currentVersion);
                headers.put("codSistema", "1");
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "registroUsuario");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("registroUsuario");
    }

}
