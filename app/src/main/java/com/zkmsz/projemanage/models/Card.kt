package com.zkmsz.projemanage.models

import android.os.Parcel
import android.os.Parcelable

data class Card(
    val name :String="",
    val createdBy: String="",
    val assignedTo:ArrayList<String> = ArrayList(),
    val labelColor:String = "",
    val dueDate:Long = 0
):Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.createStringArrayList()!!,
        source.readString()!!,
        source.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int)= with(parcel) {
        writeString(name)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeString(labelColor)
        writeLong(dueDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}