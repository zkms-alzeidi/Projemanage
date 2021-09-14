package com.zkmsz.projemanage.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zkmsz.projemanage.activities.*
import com.zkmsz.projemanage.models.Board
import com.zkmsz.projemanage.models.User
import com.zkmsz.projemanage.utils.Constants
import java.util.jar.Manifest
import com.zkmsz.projemanage.activities.SignInActivity as SignInActivity1


class FirestoreClass {

    private val mFirestore= FirebaseFirestore.getInstance() //make obj

    //this is  it make collection for the user data
    fun registerUser(activity:SignUpActivity,userInfo:com.zkmsz.projemanage.models.User)
    {
        //create a new collection
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()) //set id for him if any
            .set(userInfo, SetOptions.merge()) //set the data for the user
            .addOnSuccessListener {

                activity.userRegisteredSuccess() //set the data for a new user
            }
    }

    //To create a new board
    fun createBoard(activity: CreateBoardActivity,board: Board)
    {
        mFirestore.collection(Constants.BOARDS)
            .document() //random id
            .set(board, SetOptions.merge()) //set the data for the board
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Board created successfully")
                Toast.makeText(activity,"Board created successfully",Toast.LENGTH_SHORT).show()

                activity.boardCreatedSuccessfully()//to hide the progress bar and finish the activity
            }.addOnFailureListener{exception ->
                activity.hideProgressDialog()
                Toast.makeText(activity,"Failed $exception",Toast.LENGTH_SHORT).show()
            }
    }

    //downLoad the data for the board
    fun getBoardsList(activity: MainActivity)
    {
        mFirestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId()) //check about where the value for ASSIGNED_TO
            .get()
            .addOnSuccessListener {document->
                //to display the whole document
                Log.i(activity.javaClass.simpleName,document.documents.toString())

                val boardList:ArrayList<Board> = ArrayList()

                //to set whole the data in boardList
                for (i in document.documents)
                {
                    val board = i.toObject(Board::class.java)
                    if (board != null)
                    {
                        board.documentId = i.id
                        Log.i("i",i.toString())
                        boardList.add(board)
                    }
                    else
                    {
                        Toast.makeText(activity,"Faild",Toast.LENGTH_SHORT).show()
                    }

                }

                activity.populateBoardsListToUI(boardList)
            }.addOnFailureListener{exception ->
                activity.hideProgressDialog()
                Toast.makeText(activity,"Failed $exception",Toast.LENGTH_SHORT).show()
            }
    }

    //to get Boards Details for the task List
    fun getBoardsDetails(activity: TaskListActivity,documentId:String)
    {
        mFirestore.collection(Constants.BOARDS)
            .document(documentId) //to get the id for the item which is clicked
            .get()
            .addOnSuccessListener {document->

                Log.i(activity.javaClass.simpleName,document.toString())
                val board= document.toObject(Board::class.java)!!

                board.documentId= document.id //set the id for the board
                /**
                 * this it will let me to has all the details
                 */
                activity.boardDetails(board) //use the id as an object of type board

            }.addOnFailureListener{exception ->
                activity.hideProgressDialog()
                Toast.makeText(activity,"Failed $exception",Toast.LENGTH_SHORT).show()
            }
    }

    fun addUpdateTaskList(activity:Activity,board: Board)
    {
        val taskListHashMap=HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Task Updated successfully")

                when(activity)
                {
                    is TaskListActivity ->
                    {
                        activity.addUpdateTaskListSuccess()
                    }

                    is CardDetailsActivity ->
                    {
                        activity.addUpdateTaskListSuccess()
                    }
                }
            }
            .addOnFailureListener{exception ->

                when(activity)
                {
                    is TaskListActivity ->
                    {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName,"Task Updated Error",exception)
                    }

                    is CardDetailsActivity ->
                    {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName,"Task Updated Error",exception)
                    }
                }

            }
    }

    //make fun to update the data for the user in his profile
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String,Any>)
    {
        //create a new collection
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()) //get his id
            .update(userHashMap)
            .addOnSuccessListener {

                Log.i(activity.javaClass.simpleName,"update the data has successfully")
                Toast.makeText(activity,"Profile updates successfully",Toast.LENGTH_SHORT).show()

                when(activity)
                {
                    is MainActivity -> activity.tokenUpdateSuccess()
                    is MyProfileActivity -> activity.profileUpdateSuccess() //hide the progress dialog
                }


            }.addOnFailureListener{e ->

                when(activity)
                {
                    is MainActivity -> activity.hideProgressDialog()
                    is MyProfileActivity -> activity.hideProgressDialog() //hide the progress dialog
                }

                Log.e(activity.javaClass.simpleName,"Error while creating a board",e)
                Toast.makeText(activity,"Error when updating the profile",Toast.LENGTH_SHORT).show()


            }
    }

    //take the data for the current user
    fun loadUserData(
        activity: Activity,
        readBoardList:Boolean = false) //set the readBoardList parameter to control when we want to refresh the data
    {
        //create a new collection
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()) //get his id
            .get()
            .addOnSuccessListener {document -> //if it successful

                val userData= document.toObject(com.zkmsz.projemanage.models.User::class.java)  //take the data for current user
                if (userData != null)
                {
                    when(activity)
                    {
                        is SignInActivity1 ->
                        {
                            activity.signInSuccess(userData)  //set the data for this function
                        }

                        is MainActivity ->
                        {
                            activity.updateNavigationUserDetails(userData,readBoardList)
                        }

                        is MyProfileActivity ->
                        {
                            activity.setUserDataInUI(userData)
                        }
                    }

                }

            }.addOnFailureListener { e->


                when(activity)
                {
                    is SignInActivity1 ->
                    {
                        activity.hideProgressDialog()
                    }

                    is MainActivity ->
                    {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity ->
                    {
                        activity.hideProgressDialog()
                    }
                }



                Log.e(activity.javaClass.simpleName,"Error writing document")
            }
    }




    //this is it will return a current id to let me set his data
    fun getCurrentUserId():String
    {
        //this is it will return a current id to let me set his data
        var currentUser= FirebaseAuth.getInstance().currentUser
        var currentUserID= ""

        //if we have any user logged
        if(currentUser != null)
        {
            currentUserID= currentUser.uid  //take his id
        }

        //if not .. return empty
        return currentUserID
    }


    fun getAssignedMembersListDetails
                (activity:Activity,assignedTo:ArrayList<String>)
    {
        mFirestore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener { document->
                Log.e(activity.javaClass.simpleName,document.documents.toString())

                val usersList:ArrayList<User> = ArrayList()

                for(i in document.documents)
                {
                    val user= i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                when(activity)
                {
                    is MembersActivity ->
                    {
                        activity.setupMembersList(usersList)
                    }
                    is TaskListActivity ->
                    {
                        activity.boardMembersDetailsList(usersList)
                    }
                }


            }.addOnFailureListener{e->
                when(activity)
                {
                    is MembersActivity -> {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
                        Toast . makeText (activity, "Error", Toast.LENGTH_SHORT)
                            .show()
                    }

                    is TaskListActivity -> {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
                        Toast . makeText (activity, "Error", Toast.LENGTH_SHORT)
                            .show()
                    }

                }

            }
    }

    fun getMemberDetails(activity: MembersActivity, email:String)
    {
        mFirestore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {document->
                if (document.documents.size > 0)
                {
                    val user= document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }
                else
                {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }.addOnFailureListener{e->
                Log.e(activity.javaClass.simpleName,"Error while getting user details", e)
            }

    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User)
    {
        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO]= board.assignedTo

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener{e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating a board", e)

            }

    }
}