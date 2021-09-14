package com.zkmsz.projemanage.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirestoreRegistrar
import com.google.firebase.firestore.remote.FirebaseClientGrpcMetadataProvider
import com.zkmsz.projemanage.R
import com.zkmsz.projemanage.adapters.TaskListItemsAdapter
import com.zkmsz.projemanage.firebase.FirestoreClass
import com.zkmsz.projemanage.models.Board
import com.zkmsz.projemanage.models.Card
import com.zkmsz.projemanage.models.Task
import com.zkmsz.projemanage.models.User
import com.zkmsz.projemanage.utils.Constants
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_task_list.*
import java.text.ParsePosition

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails:Board

    private lateinit var mBoardDocumentId: String //will take the id for the board which clicked

    lateinit var mAssignedMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)



        //to tke the id for the user who has the item which is clicked
        if(intent.hasExtra(Constants.DOCUMENT_ID))
        {
            mBoardDocumentId= intent.getStringExtra(Constants.DOCUMENT_ID) //to take the id
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardsDetails(this,mBoardDocumentId)



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBER_REQUEST_CODE|| requestCode == CARD_DETAILS_REQUEST_CODE)
        {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsDetails(this,mBoardDocumentId)
        }
        else
        {
            Log.e("Canceled","Canceled")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_members -> {
                val intent= Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)

                startActivityForResult(intent,MEMBER_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //setup ActionBar
    private fun setupActionBar()
    {
        setSupportActionBar(toolbar_task_list_activity)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_24dp)
        supportActionBar!!.title= mBoardDetails.name
        toolbar_task_list_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    //take details for the board
    fun boardDetails(board:Board)
    {
        mBoardDetails= board //take the data

        hideProgressDialog()


        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess()
    {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardsDetails(this,mBoardDetails.documentId)
    }

    fun createTaskList(taskListName:String)
    {
        val task=Task(taskListName,FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName:String,model:Task)
    {
        val task= Task(listName,model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun deleteTaskList(position: Int)
    {
        mBoardDetails.taskList.removeAt(position)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }


    fun addCaerdToTaskList(position: Int,cardName:String)
    {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUserList:ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FirestoreClass().getCurrentUserId())

        val card= Card(cardName,FirestoreClass().getCurrentUserId(), cardAssignedUserList)

        val cardList= mBoardDetails.taskList[position].cards

        cardList.add(card)

        val task= Task(
            mBoardDetails.taskList[position].title, mBoardDetails.taskList[position].createdBy,
            cardList
        )

        mBoardDetails.taskList[position]= task //this will set the latest task in the top

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    //to set to the card which clicked
    fun cardDetails(taskListPosition: Int, cardPosition:Int)
    {
        val intent= Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun boardMembersDetailsList(list: ArrayList<User>)
    {
        mAssignedMembersDetailList = list

        hideProgressDialog()

        //set adapter
        val addTaskList= Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager= LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        rv_task_list.setHasFixedSize(true)
        val adapter= TaskListItemsAdapter(this,mBoardDetails.taskList)
        rv_task_list.adapter= adapter
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards:ArrayList<Card>)
    {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        mBoardDetails.taskList[taskListPosition].cards= cards

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    companion object
    {
        const val MEMBER_REQUEST_CODE:Int=  13
        const val CARD_DETAILS_REQUEST_CODE= 14
    }
}
