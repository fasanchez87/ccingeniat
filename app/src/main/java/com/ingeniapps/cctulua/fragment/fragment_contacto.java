package com.ingeniapps.cctulua.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.activity.DetalleEvento;
import com.ingeniapps.cctulua.activity.DetalleLocal;
import com.ingeniapps.cctulua.activity.DetallePelicula;
import com.ingeniapps.cctulua.adapter.CarteleraAdapter;
import com.ingeniapps.cctulua.beans.Pelicula;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.vars.vars;
import com.ingeniapps.cctulua.volley.ControllerSingleton;

import com.bumptech.glide.Glide;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yanzhenjie.permission.SettingService;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ingeniapps.cctulua.R.id.imagenDetalleEvento;


public class fragment_contacto extends Fragment implements RationaleListener
{
    private gestionSharedPreferences sharedPreferences;
    public com.ingeniapps.cctulua.vars.vars vars;
    LinearLayoutManager mLayoutManager;
    RelativeLayout layoutMacroEsperaPeliculas;
    Context context;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    private LinearLayout ll_espera_empresa;
    private NestedScrollView scrollContacto;

    private Spannable span;
    private int colorLink= Color.parseColor("#00599E");
    private static final int REQUEST_PHONE_CALL = 1;
    private static final int REQUEST_CODE_SETTING = 300;
    private String datoLlamarLocal;

    Spanned Text;

    private ImageView imagenEmpresa;
    private TextView nomEmpresa, dirEmpresa, telEmpresa, emailEmpresa, terminosEmpresa;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sharedPreferences = new gestionSharedPreferences(getActivity().getApplicationContext());
        vars = new vars();
        context = getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ll_espera_empresa = (LinearLayout) getActivity().findViewById(R.id.ll_espera_empresa);
        scrollContacto = (NestedScrollView) getActivity().findViewById(R.id.scrollContacto);

        imagenEmpresa = (ImageView) getActivity().findViewById(R.id.imagenEmpresa);
        nomEmpresa = (TextView) getActivity().findViewById(R.id.nomEmpresa);
        dirEmpresa = (TextView) getActivity().findViewById(R.id.dirEmpresa);
        telEmpresa = (TextView) getActivity().findViewById(R.id.telEmpresa);
        emailEmpresa = (TextView) getActivity().findViewById(R.id.emailEmpresa);
        terminosEmpresa = (TextView) getActivity().findViewById(R.id.terminosEmpresa);
        terminosEmpresa.setLinkTextColor(colorLink);

        Text = Html.fromHtml("" +
                "<a href='https://ingeniapps.com.co/cctulua/panel/terminos.php'>Ver Terminos y Condiciones</a>");
        terminosEmpresa.setMovementMethod(LinkMovementMethod.getInstance());
        terminosEmpresa.setText(Text);

        telEmpresa.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if (ContextCompat.checkSelfPermission(fragment_contacto.this.getActivity(), Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED)
                    {
                        //PERMISO TELEFONO
                        AndPermission.with(fragment_contacto.this.getActivity())
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
                                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(), R.style.AlertDialogTheme));
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

        //VERSION APP
        try
        {
            versionActualApp = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }





    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacto, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!this.getActivity().isFinishing())
        {
            WebServiceGetEmpresa();
        }
    }

    private void llamar(String number)
    {
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(fragment_contacto.this.getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
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

    public String getDatoLlamarLocal() {
        return datoLlamarLocal;
    }

    public void setDatoLlamarLocal(String datoLlamarLocal) {
        this.datoLlamarLocal = datoLlamarLocal;
    }

    @Override
    public void showRequestPermissionRationale(int requestCode, Rationale rationale)
    {

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
                    Toast.makeText(fragment_contacto.this.getActivity(), "Acceso denegado a llamadas, por favor habilite el permiso.", Toast.LENGTH_SHORT).show();
                    //getActivity().finish();
                    break;
                }
            }

            if (AndPermission.hasAlwaysDeniedPermission(fragment_contacto.this.getActivity(), deniedPermissions))
            {
                if(requestCode==100)
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(),R.style.AlertDialogTheme));
                    builder
                            .setTitle("Permiso de Llamadas Telefónicas")
                            .setMessage("Vaya! parece que has denegado el acceso a llamadas. Presiona el botón Permitir, " +
                                    "selecciona la opción Accesos y habilita la opción de Permiso de llamar.")
                            .setPositiveButton("PERMITIR", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    SettingService settingService = AndPermission.defineSettingDialog(fragment_contacto.this.getActivity(), REQUEST_CODE_SETTING);
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
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(),R.style.AlertDialogTheme));
                    builder
                            .setTitle("Permiso de Llamadas Télefonicas")
                            .setMessage("Vaya! parece que has denegado el acceso a llamadas. Presiona el botón Permitir, " +
                                    "selecciona la opción Accesos y habilita la opción de Permiso de llamar.")
                            .setPositiveButton("PERMITIR", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    SettingService settingService = AndPermission.defineSettingDialog(fragment_contacto.this.getActivity(), REQUEST_CODE_SETTING);
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
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("getDatosCC");
    }

    private void WebServiceGetEmpresa()
    {
        String _urlWebService = vars.ipServer.concat("/ws/getEmpresa");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if (response.getBoolean("status"))
                            {

                                Glide.with(fragment_contacto.this.getActivity()).
                                load("" + response.getString("imaEmpresa")).
                                thumbnail(0.5f).into(imagenEmpresa);

                                nomEmpresa.setText(response.getString("nomEmpresa"));
                                dirEmpresa.setText(response.getString("dirEmpresa"));
                                emailEmpresa.setText(response.getString("corEmpresa"));
                                setDatoLlamarLocal(""+response.getString("telEmpresa"));
                                span=new SpannableString(""+response.getString("telEmpresa"));
                                span.setSpan(new ForegroundColorSpan(colorLink), 0, response.getString("telEmpresa").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                telEmpresa.setText(span);

                                ll_espera_empresa.setVisibility(View.GONE);
                                scrollContacto.setVisibility(View.VISIBLE);
                            }
                            else
                            {

                            }
                        }
                        catch (JSONException e)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(), R.style.AlertDialogTheme));
                            builder
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();

                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError) {


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(), R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conexión, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();
                        } else if (error instanceof NoConnectionError) {


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(), R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Por favor, conectese a la red.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();
                        } else if (error instanceof AuthFailureError) {


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(), R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de autentificación en la red, favor contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();
                        } else if (error instanceof ServerError) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(), R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error server, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();
                        } else if (error instanceof NetworkError) {


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(), R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de red, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();
                        } else if (error instanceof ParseError) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(fragment_contacto.this.getActivity(), R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conversión Parser, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("tokenFCM", sharedPreferences.getString("tokenFCM"));
                headers.put("versionApp",versionActualApp);
                headers.put("MyToken", sharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDatosCC");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}