package com.zkmsz.projemanage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.models.Board
import kotlinx.android.synthetic.main.item_board.view.*

open class BoardItemsAdapter(private val context: Context, private var list: ArrayList<Board>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_board,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model= list[position]

        if (holder is MyViewHolder)
        {
            //to display the image
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.iv_board_image)

            //set name
            holder.itemView.tv_name.text= model.name
            //set the name of user which Created the board
            holder.itemView.tv_created_by.text= "Created by: ${model.createdBy}"

            //when the the user click on the list
            holder.itemView.setOnClickListener {
                if(onClickListener != null)
                {
                    onClickListener!!.onClick(position,model)
                }
            }

        }
    }

    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
    {

    }

    fun setOnClickListener(onClickListener: OnClickListener)
    {
        this.onClickListener= onClickListener
    }




    interface OnClickListener
    {
        fun onClick(position: Int,model:Board)
    }
}