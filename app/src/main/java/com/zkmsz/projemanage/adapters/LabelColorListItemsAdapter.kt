package com.zkmsz.projemanage.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.models.Board
import kotlinx.android.synthetic.main.item_label_color.view.*

class LabelColorListItemsAdapter(private val context: Context,private var list:ArrayList<String>, private var mSelectedColor:String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_label_color,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item= list[position]
        if(holder is MyViewHolder)
        {
            holder.itemView.view_main.setBackgroundColor(Color.parseColor(item))

            if(item == mSelectedColor)
            {
                holder.itemView.iv_selected_color.visibility= View.VISIBLE
            }

            else
            {
                holder.itemView.iv_selected_color.visibility= View.GONE
            }

            holder.itemView.setOnClickListener {

                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position, item)
                }

            }
        }
    }

    private class MyViewHolder(view:View):RecyclerView.ViewHolder(view)


    interface OnItemClickListener
    {
        fun onClick(position: Int,color: String)
    }
}