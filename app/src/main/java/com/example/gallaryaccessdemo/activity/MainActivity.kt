package com.example.gallaryaccessdemo.activity

import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gallaryaccessdemo.adapter.AdapterVideoList
import com.example.gallaryaccessdemo.databinding.ActivityMainBinding
import com.example.gallaryaccessdemo.model.ModelVideoList


class MainActivity() : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {


    lateinit var binding: ActivityMainBinding
    var size: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        loaderManager?.initLoader(13, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {


        return CursorLoader(
            this, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(
                "bucket_display_name", "title", "_data", "_size", "datetaken", "duration"
            ), null, null, "datetaken DESC"
        )

    }


    override fun onLoadFinished(loader: Loader<Cursor>?, cursor: Cursor?) {

        val arrayList = ArrayList<ModelVideoList>()

        arrayList.clear()

        size = 0L
        if (loader != null) {
            if (cursor?.count !== 0) {
                cursor?.moveToFirst()
                if (cursor != null) {
                    do {
                        try {
                            val videoModel = ModelVideoList()
                            videoModel.name =
                                cursor.getString(cursor.getColumnIndexOrThrow("title"))
                            videoModel.thumb_img =
                                cursor.getString(cursor.getColumnIndexOrThrow("_data"))
                            videoModel.date =
                                cursor.getString(cursor.getColumnIndexOrThrow("datetaken"))
                            videoModel.size =
                                cursor.getString(cursor.getColumnIndexOrThrow("_size"))
                            videoModel.duration =
                                cursor.getLong(cursor.getColumnIndexOrThrow("duration"))
                                    .let { java.lang.Long.valueOf(it) }
                            if (cursor.getColumnIndexOrThrow("_size")
                                    .let { cursor.getString(it) } == null
                            ) {
                                this.size = 0L
                            } else {
                                this.size += cursor.getString(cursor.getColumnIndexOrThrow("_size"))
                                    .toLong()
                            }
                            videoModel.isselected = false
                            arrayList.add(videoModel)


                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } while (cursor.moveToNext())
                }
            }

            if (arrayList.isNotEmpty()) {


                val adapter = AdapterVideoList(arrayList, this)
                { uri ->
                    val intent = Intent(this, VideoPlayerActivity::class.java)
                    intent.putExtra("path", uri)
                    startActivity(intent)
                }

                binding.rvVideoList.layoutManager = LinearLayoutManager(this)
                binding.rvVideoList.adapter = adapter
            } else {

            }
        }

    }


    override fun onLoaderReset(loader: Loader<Cursor>) {
    }


}