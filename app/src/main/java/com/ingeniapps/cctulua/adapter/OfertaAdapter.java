package com.ingeniapps.cctulua.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.beans.Local;
import com.ingeniapps.cctulua.beans.Oferta;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.vars.vars;

import java.util.ArrayList;
import java.util.List;


public class OfertaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private List<Oferta> listadoOfertas;

    public final int TYPE_OFERTA=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    private Context context;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;
    int previousPosition=0;
    long timestamp;
    CharSequence timeAgo;

    public interface OnItemClickListener
    {
        void onItemClick(Oferta oferta);
    }

    private final OfertaAdapter.OnItemClickListener listener;

    public OfertaAdapter(Activity activity, ArrayList<Oferta> listadoOfertas, OfertaAdapter.OnItemClickListener listener)
    {
        this.activity=activity;
        this.listadoOfertas=listadoOfertas;
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
        this.listener=listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_OFERTA)
        {
            return new LocalHolder(inflater.inflate(R.layout.oferta_row_layout,parent,false));
        }
        else
        {
            return new LoadHolder(inflater.inflate(R.layout.oferta_row_layout,parent,false));
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

        if(getItemViewType(position)==TYPE_OFERTA)
        {
            ((LocalHolder)holder).bindData(listadoOfertas.get(position));
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

        if(listadoOfertas.get(position).getType().equals("oferta"))
        {
            return TYPE_OFERTA;
        }
        else
        {
            return TYPE_LOAD;
        }
    }

    @Override
    public int getItemCount()
    {
        return listadoOfertas.size();
    }

    public class LocalHolder extends RecyclerView.ViewHolder
    {
        public ImageView logoLocalOferta;
        public TextView nombreLocalOferta;
        public TextView nomOferta;
        public TextView descOferta;
        public TextView fecExpOferta;

        public LocalHolder(View view)
        {
            super(view);
            logoLocalOferta=(ImageView) view.findViewById(R.id.logoLocalOferta);
            nombreLocalOferta=(TextView) view.findViewById(R.id.nombreLocalOferta);
            nomOferta=(TextView) view.findViewById(R.id.nomOferta);
            descOferta=(TextView) view.findViewById(R.id.descOferta);
            fecExpOferta=(TextView) view.findViewById(R.id.fecExpOferta);
        }

        void bindData(final Oferta oferta)
        {
            Glide.with(activity).
                    load(oferta.getImgLocal()).
                    thumbnail(0.5f).into(logoLocalOferta);

            nombreLocalOferta.setText(oferta.getNomLocal());
            nomOferta.setText(oferta.getNomOferta());

            if(TextUtils.equals(oferta.getDescOferta(),"0"))
            {
                descOferta.setText("Promoción");
            }

            else
            {
                descOferta.setText("Descuento "+oferta.getDescOferta()+"%");
            }

            timestamp = Long.parseLong(oferta.getFecExpOferta()) * 1000L;
            timeAgo = DateUtils.getRelativeTimeSpanString(timestamp,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
            fecExpOferta.setText("Expira "+timeAgo);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    listener.onItemClick(oferta);
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

    public List<Oferta> getOfertasList()
    {
        return listadoOfertas;
    }

}
