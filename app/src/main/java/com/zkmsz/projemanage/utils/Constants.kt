package com.zkmsz.projemanage.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import com.zkmsz.projemanage.activities.MyProfileActivity
import com.zkmsz.projemanage.utils.Constants.PICK_IMAGE_REQUEST_CODE

object Constants
{
    const val USERS:String= "Users"

    const val BOARDS:String = "boards"

    const val IMAGE:String= "image"
    const val NAME:String= "name"
    const val MOBILE:String= "mobile"
    const val ASSIGNED_TO:String= "assignedTo"

    const val DOCUMENT_ID:String="documentId"

    const val TASK_LIST:String= "taskList"

    const val BOARD_DETAIL:String= "board_detail"

    const val ID:String= "id"

    const val EMAIL= "email"

    const val TASK_LIST_ITEM_POSITION:String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION:String= "carf_list_item_position"

    const val BOARD_MEMBERS_LIST:String = "board_members_list"
    const val SELECT:String = "Select"
    const val UN_SELECT:String = "UnSelect"


    const val PRJEMANAG_PREFERENCES = "ProjemanagePrefs"
    const val FCM_TOKEN_UPDATED= "fcmTokenUpdated"
    const val FCM_TOKEN= "fcmToken"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAA9v4vJCw:APA91bHB16CLcN108DZfEIZvb2k5Ti85XxppxxzkbRQG3XHA_5FfpnOZtXJqed2CPov32Csq6MJ-7U5r2imsTCwgzwDhR_El9itLC4GeL6cuYfKTnEqzj34_WmwaCLxpkhUtPFuf8GnO"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"

    const val READ_STORAGE_PERMISSION_CODE= 1
    const val PICK_IMAGE_REQUEST_CODE= 2

    //to choose an image from the gallery
    fun showImageChooser(activity: Activity)
    {
        var galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    //this help me to know the type of the uri
    fun getFileExtension(activity: Activity,uri: Uri?):String?
    {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}