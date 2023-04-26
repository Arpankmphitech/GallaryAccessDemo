package com.example.gallaryaccessdemo.adapter

import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallaryaccessdemo.R
import com.example.gallaryaccessdemo.activity.MainActivity
import com.example.gallaryaccessdemo.model.ModelVideoList
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class AdapterVideoList(
    var arrayList: ArrayList<ModelVideoList>,
    val mainActivity: MainActivity,
    val onItemClicked: (String) -> Unit
) : RecyclerView.Adapter<AdapterVideoList.ViewHolder>() {


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        var thumbnail = itemView.findViewById<ImageView>(R.id.thumbnail)
        var menu = itemView.findViewById<ImageView>(R.id.imgMenu)
        var tvName = itemView.findViewById<TextView>(R.id.tvName)
        var tvSize = itemView.findViewById<TextView>(R.id.tvSize)
        var txtDate = itemView.findViewById<TextView>(R.id.txtDate)
        var tvDuration = itemView.findViewById<TextView>(R.id.tvDuration)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_item, parent, false)

        return ViewHolder(view)


    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {

        mainActivity.let {
            Glide.with(it).load(arrayList[pos].thumb_img).into(holder.thumbnail)
        }

        holder.tvName.text = arrayList[pos].name
        holder.tvSize.text = arrayList[pos].size!!.toLong().let { convertToFormate(it) }
        val date = arrayList.get(pos).date?.let { Date(it.toLong()) }
        if (date != null) {
            holder.txtDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
        }
        var durationTime: String? = ""

        if (arrayList[pos].duration != null) {3
            durationTime = time(arrayList[pos].duration!!.toLong())
        }
        holder.tvDuration.text = durationTime

        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        holder.itemView.setOnClickListener {
            val uri = Uri.parse(arrayList.get(pos).thumb_img)
            onItemClicked.invoke(uri.toString())

//            Toast.makeText(mainActivity, "=== $uri", Toast.LENGTH_SHORT).show()

        }

        holder.menu.setOnClickListener {

            val uri = Uri.parse(arrayList.get(pos).thumb_img).toString()

            val popup = PopupMenu(mainActivity, it)
            popup.menuInflater.inflate(R.menu.menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.itemShare -> {
                        itemShare(pos)
                    }
                    R.id.itemDelete -> {
                        itemDelete(pos)
                    }
                    R.id.itemInfo -> {
                        itemInfo(pos)
                    }

                }
                true
            }
            popup.show()
        }

    }

    private fun itemDelete(pos: Int) {

        val videoUri = Uri.parse(arrayList.get(pos).thumb_img)
        val file = File(videoUri.getPath())

        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(
                    mainActivity, "file Deleted :" + videoUri.getPath(), Toast.LENGTH_SHORT
                ).show()
                arrayList.removeAt(pos)
                notifyItemRemoved(pos)
                notifyDataSetChanged()
                MediaScannerConnection.scanFile(
                    mainActivity, arrayOf<String>(file.toString()),
                    null, null
                )

            } else {
                Toast.makeText(
                    mainActivity, "file not Deleted :" + videoUri.getPath(), Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun itemInfo(itemPos: Int) {

        val dialog = Dialog(mainActivity)
        dialog.setContentView(R.layout.info_dialog)
        dialog.getWindow()
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        dialog.getWindow()!!.getAttributes().windowAnimations

        var txtFileName = dialog.findViewById<TextView>(R.id.txtFileName)
        var txtFileDate = dialog.findViewById<TextView>(R.id.txtFileDate)
        var txtFileDuration = dialog.findViewById<TextView>(R.id.txtFileDuration)
        var txtFileSize = dialog.findViewById<TextView>(R.id.txtFileSize)
        var layOutOk = dialog.findViewById<LinearLayout>(R.id.layOutOk)



        txtFileName.text = arrayList[itemPos].name
        txtFileSize.text = arrayList[itemPos].size!!.toLong().let { convertToFormate(it) }
        val date = arrayList.get(itemPos).date?.let { Date(it.toLong()) }
        if (date != null) {
            txtFileDate.text = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
        }
        var durationTime: String? = ""
        if (arrayList[itemPos].duration != null) {
            durationTime = time(arrayList[itemPos].duration!!.toLong())
        }
        txtFileDuration.text = durationTime

        dialog.show()

        layOutOk.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun itemShare(pos: Int) {

        val videoUri = Uri.parse(arrayList.get(pos).thumb_img)
        val file = videoUri.toString()

        val targetIntent = Intent(Intent.ACTION_SEND)
        targetIntent.type = "video/*"
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        targetIntent.putExtra(Intent.EXTRA_SUBJECT, mainActivity.getString(R.string.app_name))
        targetIntent.putExtra(Intent.EXTRA_TEXT, "")
        val fileURI = FileProvider.getUriForFile(
            mainActivity,
            mainActivity.packageName + ".provider",
            File(file)
        )
        targetIntent.putExtra(Intent.EXTRA_STREAM, fileURI)
        mainActivity.startActivity(targetIntent)
    }


    private fun time(l: Long): String {
        return String.format(
            "%02d:%02d:%02d",
            java.lang.Long.valueOf(TimeUnit.MILLISECONDS.toHours(l)),
            java.lang.Long.valueOf(
                TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(l)
                )
            ),
            java.lang.Long.valueOf(
                TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(l)
                )
            )
        )
    }


    private fun convertToFormate(j: Long): String {
        if (j < 1024) {
            return "$j B"
        }
        val numberOfLeadingZeros = (63 - java.lang.Long.numberOfLeadingZeros(j)) / 10
        return String.format(
            "%.1f %sB",
            java.lang.Double.valueOf((j / (1 shl numberOfLeadingZeros * 10)).toDouble()),
            Character.valueOf(" KMGTPE"[numberOfLeadingZeros])
        )
    }

}
