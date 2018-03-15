package com.ingeniapps.cctulua.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.beans.Local;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.vars.vars;


public class LocalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private List<Local> listadoLocales;

    public final int TYPE_LOCAL=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    private Context context;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;
    int previousPosition=0;
    Spannable Text;
    private int colorLink=Color.parseColor("#00599E");

    public interface OnItemClickListener
    {
        void onItemClick(Local local);
    }

    private final LocalAdapter.OnItemClickListener listener;

    public LocalAdapter(Activity activity, ArrayList<Local> listadoLocales, LocalAdapter.OnItemClickListener listener)
    {
        this.activity=activity;
        this.listadoLocales=listadoLocales;
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
        this.listener=listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_LOCAL)
        {
            return new LocalHolder(inflater.inflate(R.layout.local_row_layout,parent,false));
        }
        else
        {
            return new LoadHolder(inflater.inflate(R.layout.local_row_layout,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if(position >= getItemCount()-1 && isMoreDataAvailable && !isLoading && loadMoreListener!=null)
        {
            isLoading = true;
            loadMoreListener.onLoadMore();
        }

        if(getItemViewType(position)==TYPE_LOCAL)
        {
            ((LocalHolder)holder).bindData(listadoLocales.get(position));
        }

       /* if(position>previousPosition)
        {
            new MyAnimationUtils().animate(holder,true);
        }
        else
        {
            new MyAnimationUtils().animate(holder,false);
        }

        previousPosition=position;*/

    }

    @Override
    public int getItemViewType(int position)
    {

        if(listadoLocales.get(position).getType().equals("local"))
        {
            return TYPE_LOCAL;
        }
        else
        {
            return TYPE_LOAD;
        }
    }

    @Override
    public int getItemCount()
    {
        return listadoLocales.size();
    }

    public class LocalHolder extends RecyclerView.ViewHolder
    {
        public ImageView logoLocal;
        public TextView nombreLocal;
        public TextView ubicacionLocal;
        public TextView descripcionLocal;
        public TextView telLocal;

        public LocalHolder(View view)
        {
            super(view);
            logoLocal=(ImageView) view.findViewById(R.id.logoLocal);
            nombreLocal=(TextView) view.findViewById(R.id.nombreLocal);
            ubicacionLocal=(TextView) view.findViewById(R.id.ubicacionLocal);
            descripcionLocal=(TextView) view.findViewById(R.id.descripcionLocal);
            telLocal=(TextView) view.findViewById(R.id.telLocal);
        }

        void bindData(final Local local)
        {
            Glide.with(activity).
                    load(local.getImgLocal().toString()).
                    thumbnail(0.5f).into(logoLocal);

            /*Text=new SpannableString(""+local.getTelLocal());
            Text.setSpan(new ForegroundColorSpan(colorLink), 0, local.getTelLocal().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            telLocal.setText(Text);*/

           /* telLocal.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Toast.makeText(v.getContext(),"Telefono: ",Toast.LENGTH_LONG);
                    Log.i("Telefono","Telefono: "+local.getTelLocal());
                }
            });*/

            nombreLocal.setText(local.getNomLocal());
            ubicacionLocal.setText(local.getUbicacionLocal());
            descripcionLocal.setText(local.getDescLocal());

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    listener.onItemClick(local);
                }
            });
        }
    }

    static class LoadHolder extends RecyclerView.ViewHolder
    {
        public LoadHolder(View itemView)
        {
            super(itemView);
        }
    }

    public void setMoreDataAvailable(boolean moreDataAvailable)
    {
        isMoreDataAvailable = moreDataAvailable;
    }
    /* notifyDataSetChanged is final method so we can't override it
        call adapter.notifyDataChanged(); after update the list
        */
    public void notifyDataChanged()
    {
        notifyDataSetChanged();
        isLoading = false;
    }

    public interface OnLoadMoreListener
    {
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener)
    {
        this.loadMoreListener = loadMoreListener;
    }

    public List<Local> getLocalesList()
    {
        return listadoLocales;
    }

}
