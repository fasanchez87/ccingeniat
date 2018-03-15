package com.ingeniapps.cctulua.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ingeniapps.cctulua.R;
import com.ingeniapps.cctulua.beans.Local;
import com.ingeniapps.cctulua.beans.Pelicula;
import com.ingeniapps.cctulua.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.cctulua.vars.vars;

import java.util.ArrayList;
import java.util.List;


public class CarteleraAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private List<Pelicula> listadoPeliculas;

    public final int TYPE_PELICULA=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    private Context context;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;
    int previousPosition=0;

    public interface OnItemClickListener
    {
        void onItemClick(Pelicula pelicula);
    }

    private final CarteleraAdapter.OnItemClickListener listener;

    public CarteleraAdapter(Activity activity, ArrayList<Pelicula> listadoPeliculas, CarteleraAdapter.OnItemClickListener listener)
    {
        this.activity=activity;
        this.listadoPeliculas=listadoPeliculas;
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
        this.listener=listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_PELICULA)
        {
            return new PeliculaHolder(inflater.inflate(R.layout.cartelera_row,parent,false));
        }
        else
        {
            return new LoadHolder(inflater.inflate(R.layout.cartelera_row,parent,false));
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

        if(getItemViewType(position)==TYPE_PELICULA)
        {
            ((PeliculaHolder)holder).bindData(listadoPeliculas.get(position));
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

        if(listadoPeliculas.get(position).getType().equals("pelicula"))
        {
            return TYPE_PELICULA;
        }
        else
        {
            return TYPE_LOAD;
        }
    }

    @Override
    public int getItemCount()
    {
        return listadoPeliculas.size();
    }

    public class PeliculaHolder extends RecyclerView.ViewHolder
    {
        public ImageView imagenCartelera;
        public TextView nombreCartelera;
        public TextView dimenCartelera;
        public TextView lenguaCartelera;
        public TextView horarioCartelera;

        public PeliculaHolder(View view)
        {
            super(view);
            imagenCartelera=(ImageView) view.findViewById(R.id.imagenCartelera);
            nombreCartelera=(TextView) view.findViewById(R.id.nombreCartelera);
            dimenCartelera=(TextView) view.findViewById(R.id.dimenCartelera);
            lenguaCartelera=(TextView) view.findViewById(R.id.lenguaCartelera);
            horarioCartelera=(TextView) view.findViewById(R.id.horarioCartelera);
        }

        void bindData(final Pelicula pelicula)
        {
            Glide.with(activity).
                    load(pelicula.getImaPelicula().toString()).
                    thumbnail(0.5f).into(imagenCartelera);

            nombreCartelera.setText(pelicula.getNomPelicula());
            dimenCartelera.setText(pelicula.getDimenPelicula());
            lenguaCartelera.setText(pelicula.getLenguaPelicula());
            horarioCartelera.setText(pelicula.getHoraPelicula());

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    listener.onItemClick(pelicula);
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

    public List<Pelicula> getPeliculaList()
    {
        return listadoPeliculas;
    }

}
