package com.zkmsz.projemanage.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.adapters.LabelColorListItemsAdapter
import kotlinx.android.synthetic.main.activity_sign_in.view.*
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class LabelColorListDialog (context: Context,
                                     private var list: ArrayList<String>,
                                     private val title:String=" ",
                                     private val mSelectedColor:String= ""): Dialog(context)
{
    private var adapter:LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list,null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View)
    {

        view.tvTitle.text= title
        view.rvList.layoutManager= LinearLayoutManager(context)
        adapter= LabelColorListItemsAdapter(context, list,mSelectedColor)
        view.rvList.adapter= adapter

        adapter!!.onItemClickListener= object :LabelColorListItemsAdapter.OnItemClickListener
        {

            override fun onClick(position: Int, color: String)
            {

                dismiss()
                onItemSelected(color)

            }

        }
    }

    protected abstract fun onItemSelected(color:String)

}