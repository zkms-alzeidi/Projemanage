package com.zkmsz.projemanage.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CalendarView
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.projemanag.dialogs.MembersListDialog
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.adapters.CardMemberListItemAdapter
import com.zkmsz.projemanage.adapters.LabelColorListItemsAdapter
import com.zkmsz.projemanage.dialog.LabelColorListDialog
import com.zkmsz.projemanage.firebase.FirestoreClass
import com.zkmsz.projemanage.models.*
import com.zkmsz.projemanage.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.item_task.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails:Board
    private var mTaskListPosition= -1
    private var mCardPosition= -1

    private var mSelectedColor= ""
    private lateinit var mMembersDetailList:ArrayList<User>

    private var mSelectedDueDateMilliSeconds: Long= 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()

        setupActionBar()

        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor

        if(mSelectedColor.isNotEmpty())
        {
            setColor()
        }

        btn_update_card_details.setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty())
            {
                updateCardDetails()
            }
            else
            {
                Toast.makeText(this,"Please enter a card name",Toast.LENGTH_SHORT).show()
            }
        }

        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds= mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate

        if (mSelectedDueDateMilliSeconds > 0)
        {
            val simpleDateFormat= SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate= simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            tv_select_due_date.text= selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDatePicker()
        }


    }

    fun addUpdateTaskListSuccess()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    //setup ActionBar
    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_card_details_activity)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_24dp)
        supportActionBar!!.title= mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition]
            .name

        toolbar_card_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getIntentData()
    {

        if (intent.hasExtra(Constants.BOARD_DETAIL))
        {
            mBoardDetails= intent.getParcelableExtra(Constants.BOARD_DETAIL)
        }

        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION))
        {
            mTaskListPosition= intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION))
        {
            mCardPosition= intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }

        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST))
        {
            mMembersDetailList= intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_delete_card ->
            {
                alertDialogForDeleteList(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun colorsList(): ArrayList<String>
    {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor()
    {
        tv_select_label_color.text= ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun updateCardDetails()
    {
        val card= Card(et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds)

        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]= card

        //update the database
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    private fun deleteCard()
    {
        //get all the current cards
        val cardList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardList.removeAt(mCardPosition)

        //current task list which we have it
        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1) //remove last item which is the ADD btn

        //to update the list of cards
        taskList[mTaskListPosition].cards= cardList

        //update the database
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    private fun alertDialogForDeleteList(cardName:String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(R.string.alert)
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $cardName")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            deleteCard()
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }


    private fun labelColorsListDialog()
    {
        val colorsList: ArrayList<String> = colorsList()
        val listDialog= object :LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor)
        {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }

        }
        listDialog.show()
    }

    private fun membersListDialog()
    {
        var cardAssignedMembersList= mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if(cardAssignedMembersList.size > 0)
        {
            for (i in mMembersDetailList.indices)
            {
                for (j in cardAssignedMembersList)
                {
                    if(mMembersDetailList[i].id == j)
                    {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }
        else
        {
            for (i in mMembersDetailList.indices)
            {
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog= object : MembersListDialog(this,mMembersDetailList,resources.getString(R.string.select_members))
        {
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT)
                {
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id))
                    {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                    }
                }
                else
                {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)

                    for (i in mMembersDetailList.indices)
                    {
                        if (mMembersDetailList[i].id == user.id)
                        {
                            mMembersDetailList[i].selected = false
                        }
                    }
                }

                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun setupSelectedMembersList()
    {
        val cardAssignedMembersList= mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices)
        {
            for (j in cardAssignedMembersList)
            {
                if(mMembersDetailList[i].id == j)
                {
                    val selectedMembers= SelectedMembers(mMembersDetailList[i].id, mMembersDetailList[i].image)
                    selectedMembersList.add(selectedMembers)
                }

            }
        }

        if (selectedMembersList.size > 0 )
        {
            selectedMembersList.add(SelectedMembers("",""))
            tv_select_members.visibility= View.GONE
            rv_selected_members_list.visibility= View.VISIBLE
            rv_selected_members_list.layoutManager= GridLayoutManager(this,6)
            val adapter= CardMemberListItemAdapter(this,selectedMembersList,true)
            rv_selected_members_list.adapter= adapter
            adapter.setOnClickListener(object :CardMemberListItemAdapter.OnClickListener
            {
                override fun onClick() {
                    membersListDialog()
                }
            })
        }
        else
        {
            tv_select_members.visibility= View.VISIBLE
            rv_selected_members_list.visibility= View.GONE
        }
    }

    private fun showDatePicker()
    {
        val c = Calendar.getInstance()
        val year= c.get(Calendar.YEAR)
        val month= c.get(Calendar.MONTH)
        val day= c.get(Calendar.DAY_OF_MONTH)

        val dpd= DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDAyOfMonth= if(dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear=
                    if ((monthOfYear + 1)< 10)
                        "0${monthOfYear+1}"
                    else
                        "${monthOfYear +1}"
                val selectedDate= "$sDAyOfMonth/$sMonthOfYear/$year"

                tv_select_due_date.text= selectedDate

                val sdf= SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate= sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds= theDate!!.time

        },year,month,day)
        dpd.show()
    }
}
