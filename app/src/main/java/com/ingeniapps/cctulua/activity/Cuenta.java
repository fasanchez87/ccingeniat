package com.ingeniapps.cctulua.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Cuenta extends AppCompatActivity
{
    private vars vars;
    private RelativeLayout layoutMacroEsperaCuenta;
    private NestedScrollView nshabilitarCuenta;
    private gestionSharedPreferences sharedPreferences;
    private Context context;
    private EditText editTextNomUsuario;
    private EditText editTextCedUsuario;
    private EditText editTextEmaUsuario;
    private EditText editTextCelUsuario;
    private Button buttonSaveInfo;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);
        context=this;

        editTextNomUsuario=findViewById(R.id.editTextNomUsuario);
        editTextCedUsuario=findViewById(R.id.editTextCedUsuario);
        editTextEmaUsuario=findViewById(R.id.editTextEmaUsuario);
        editTextCelUsuario=findViewById(R.id.editTextCelUsuario);

        layoutMacroEsperaCuenta=findViewById(R.id.layoutMacroEsperaCuenta);
        nshabilitarCuenta=findViewById(R.id.nshabilitarCuenta);
        buttonSaveInfo=findViewById(R.id.buttonSaveInfo);
        buttonSaveInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (TextUtils.isEmpty(editTextNomUsuario.getText().toString()))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digite su nombre.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(editTextCedUsuario.getText().toString()))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digite su cédula.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(editTextEmaUsuario.getText().toString())||!(isValidEmail(editTextEmaUsuario.getText().toString())))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digite un email valido.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(editTextCelUsuario.getText().toString()))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digite su teléfono.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                WebServiceSaveInfo();
            }
        });
        sharedPreferences=new gestionSharedPreferences(this);


        vars=new vars();

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

        _webServiceGetInfo();
    }

    public final static boolean isValidEmail(CharSequence target)
    {
        if (target == null)
        {
            return false;
        }
        else
        {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private void _webServiceGetInfo()
    {
        String _urlWebService=vars.ipServer.concat("/ws/getDataUser");

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if(response.getBoolean("status"))
                            {
                                JSONObject usuario=response.getJSONObject("datos");

                                if (!((Activity) context).isFinishing())
                                {
                                    editTextNomUsuario.setText(usuario.getString("nomUsuario"));
                                    editTextCedUsuario.setText(usuario.getString("cedUsuario"));
                                    editTextEmaUsuario.setText(usuario.getString("emaUsuario"));
                                    editTextCelUsuario.setText(usuario.getString("telUsuario"));
                                    sharedPreferences.putString("codUsuario", usuario.getString("codUsuario"));
                                }
                            }

                            layoutMacroEsperaCuenta.setVisibility(View.GONE);
                            nshabilitarCuenta.setVisibility(View.VISIBLE);
                        }

                        catch (JSONException e)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                {
                                    layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                    nshabilitarCuenta.setVisibility(View.VISIBLE);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this,R.style.AlertDialogTheme));
                                    builder
                                            .setMessage(e.getMessage().toString())
                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                    //startActivity(intent);
                                                    //finish();
                                                }
                                            }).show();

                                    e.printStackTrace();

                                }
                            }

                        }
                    }
                },

                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {

                        if (error instanceof TimeoutError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                {
                                    layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                    nshabilitarCuenta.setVisibility(View.VISIBLE);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                    builder
                                            .setMessage(error.getMessage().toString())
                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                    //startActivity(intent);
                                                    //finish();
                                                }
                                            }).show();
                                }
                            }
                        }

                        else

                        if (error instanceof NoConnectionError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                {
                                    layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                    nshabilitarCuenta.setVisibility(View.VISIBLE);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                    builder
                                            .setMessage(error.getMessage().toString())
                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                    //startActivity(intent);
                                                    //finish();
                                                }
                                            }).show();
                                }
                            }

                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                {
                                    layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                    nshabilitarCuenta.setVisibility(View.VISIBLE);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                    builder
                                            .setMessage(error.getMessage().toString())
                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                    //startActivity(intent);
                                                    //finish();
                                                }
                                            }).show();
                                }
                            }
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                {
                                    layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                    nshabilitarCuenta.setVisibility(View.VISIBLE);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                    builder
                                            .setMessage(error.getMessage().toString())
                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                    //startActivity(intent);
                                                    //finish();
                                                }
                                            }).show();
                                }
                            }
                        }

                        else

                        if (error instanceof NetworkError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                {
                                    layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                    nshabilitarCuenta.setVisibility(View.VISIBLE);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                    builder
                                            .setMessage(error.getMessage().toString())
                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                    //startActivity(intent);
                                                    //finish();
                                                }
                                            }).show();
                                }
                            }


                        }

                        else

                        if (error instanceof ParseError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                {
                                    layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                    nshabilitarCuenta.setVisibility(View.VISIBLE);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                    builder
                                            .setMessage(error.getMessage().toString())
                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                    //startActivity(intent);
                                                    //finish();
                                                }
                                            }).show();
                                }
                            }
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
                headers.put("codUsuario", TextUtils.isEmpty(sharedPreferences.getString("codUsuario"))?null:sharedPreferences.getString("codUsuario"));
                return headers;
            }
        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq,"_GETCIUDAD");
    }

    private void WebServiceSaveInfo()
    {
        String _urlWebService=vars.ipServer.concat("/ws/setInfoUser");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(Cuenta.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(final JSONObject response)
                    {
                        try
                        {
                            Boolean status=response.getBoolean("status");
                            String message=response.getString("message");

                            if(status)
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    progressDialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this,R.style.AlertDialogTheme));
                                    builder
                                            .setTitle("Centro Comercial Tuluá")
                                            .setMessage(message)
                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    try {
                                                        sharedPreferences.putString("codUsuario",response.getString("codUsuario"));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                            setTextColor(getResources().getColor(R.color.colorPrimary));
                                }

                            }
                            else
                            if (!status)
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    progressDialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this,R.style.AlertDialogTheme));
                                    builder
                                            .setTitle("Pyde")
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
                        }
                        catch (JSONException e)
                        {
                            if (!((Activity) context).isFinishing()) {
                                {
                                    progressDialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this,R.style.AlertDialogTheme));
                                    builder
                                            .setTitle("ESTADO REGISTRO")
                                            .setMessage(e.getMessage().toString())
                                            .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
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

                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {

                        if (error instanceof TimeoutError)
                        {
                            if (error instanceof TimeoutError) {
                                if (!((Activity) context).isFinishing()) {
                                    {
                                        progressDialog.dismiss();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                        builder
                                                .setMessage(error.getMessage().toString())
                                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                        //startActivity(intent);
                                                        //finish();
                                                    }
                                                }).show();
                                    }
                                }
                            }
                        }
                        else
                        if (error instanceof NoConnectionError)
                        {
                            if (error instanceof TimeoutError) {
                                if (!((Activity) context).isFinishing()) {
                                    {
                                        progressDialog.dismiss();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                        builder
                                                .setMessage(error.getMessage().toString())
                                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                        //startActivity(intent);
                                                        //finish();
                                                    }
                                                }).show();
                                    }
                                }
                            }
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            if (error instanceof TimeoutError) {
                                if (!((Activity) context).isFinishing()) {
                                    {
                                        progressDialog.dismiss();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                        builder
                                                .setMessage(error.getMessage().toString())
                                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                        //startActivity(intent);
                                                        //finish();
                                                    }
                                                }).show();
                                    }
                                }
                            }
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            if (error instanceof TimeoutError) {
                                if (!((Activity) context).isFinishing()) {
                                    {
                                        progressDialog.dismiss();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                        builder
                                                .setMessage(error.getMessage().toString())
                                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                        //startActivity(intent);
                                                        //finish();
                                                    }
                                                }).show();
                                    }
                                }
                            }
                        }
                        else
                        if (error instanceof NetworkError)
                        {
                            if (error instanceof TimeoutError) {
                                if (!((Activity) context).isFinishing()) {
                                    {
                                        progressDialog.dismiss();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                        builder
                                                .setMessage(error.getMessage().toString())
                                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                        //startActivity(intent);
                                                        //finish();
                                                    }
                                                }).show();
                                    }
                                }
                            }
                        }
                        else
                        if (error instanceof ParseError)
                        {
                            if (error instanceof TimeoutError) {
                                if (!((Activity) context).isFinishing()) {
                                    {
                                        progressDialog.dismiss();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(Cuenta.this);
                                        builder
                                                .setMessage(error.getMessage().toString())
                                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //Intent intent = new Intent(Pago.this.getApplicationContext(), Registro.class);
                                                        //startActivity(intent);
                                                        //finish();
                                                    }
                                                }).show();
                                    }
                                }
                            }
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
                headers.put("codUsuario", TextUtils.isEmpty(sharedPreferences.getString("codUsuario"))?null:sharedPreferences.getString("codUsuario"));
                headers.put("nomUsuario", ""+editTextNomUsuario.getText().toString());
                headers.put("cedUsuario", ""+editTextCedUsuario.getText().toString());
                headers.put("emaUsuario", editTextEmaUsuario.getText().toString());
                headers.put("telUsuario", editTextCelUsuario.getText().toString());
                headers.put("tokenFCM", ""+ FirebaseInstanceId.getInstance().getToken());
                headers.put("codSistema", "1");
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_REGISTRO");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

}
