package com.rohit.chitchat.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.firebase.database.FirebaseDatabase
import com.rohit.chitchat.Models.LiveChatModel
import com.rohit.chitchat.MyConstants
import com.rohit.chitchat.MyUtils
import com.rohit.chitchat.R
import java.util.*
import kotlin.collections.ArrayList


class ChatLiveAdapter(
    var context: Activity,
    var chatsList: ArrayList<LiveChatModel>,
    var roomId: String,
) : RecyclerView.Adapter<ChatLiveAdapter.viewHolder>() {
    var mediaPlayer = MediaPlayer()
    var currentPlay=-1
    var previousPlay=-2
    var currentSeekTo=0
     var timer: CountDownTimer?=null
    var firebaseChats =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHATS)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        if (viewType == 1) {
            return viewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_chat_right, parent, false)!!
            )
        } else {
            return viewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_chat_left, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ChatLiveAdapter.viewHolder, position: Int) {


        if (chatsList.get(position).sender.equals(
                MyUtils.getStringValue(
                    context,
                    MyConstants.USER_PHONE
                )
            )
        ) {
            holder.viewSeen.visibility = View.VISIBLE
            if (chatsList.get(position).seenStatus.toString().equals("1")) {
                holder.viewSeen.setBackgroundResource(R.drawable.bg_message_seen)
            } else {
                holder.viewSeen.setBackgroundResource(R.drawable.bg_message_unseen)
            }
        } else {
            holder.viewSeen.visibility = View.GONE
            if (chatsList.get(position).seenStatus.equals("0") && chatsList.get(position).receiver.equals(
                    MyUtils.getStringValue(context, MyConstants.USER_PHONE)
                )
            ) {
                firebaseChats.child(roomId.toString()).child(chatsList.get(position).key.toString())
                    .child("seenStatus").setValue("1")
            }

        }

        holder.imgMessage.clipToOutline=true
        if (chatsList.get(position).messageType.equals("text")) {
            holder.imgPlay.visibility = View.GONE
            holder.imgMessage.visibility = View.GONE
            holder.txtMessage.visibility = View.VISIBLE
            holder.audioPause.visibility = View.GONE
            holder.imgLocation.visibility = View.GONE
            holder.audioPlay.visibility = View.GONE
            holder.audioSeekbar.visibility = View.GONE
            holder.txtMessage.text = chatsList[position].message

        } else if (chatsList.get(position).messageType.equals("image")) {
            holder.audioPause.visibility = View.GONE
            holder.audioPlay.visibility = View.GONE
            holder.audioSeekbar.visibility = View.GONE
            holder.txtMessage.visibility = View.GONE
            holder.imgLocation.visibility = View.GONE
            holder.imgMessage.visibility = View.VISIBLE
            Glide.with(context)
                .load(chatsList.get(position).message)
                .into(holder.imgMessage)
            holder.imgPlay.visibility = View.GONE
        }else if (chatsList.get(position).messageType.equals("location")) {
            holder.audioPause.visibility = View.GONE
            holder.audioPlay.visibility = View.GONE
            holder.audioSeekbar.visibility = View.GONE
            holder.txtMessage.visibility = View.GONE
            holder.imgLocation.visibility = View.VISIBLE
            holder.imgMessage.visibility = View.VISIBLE
            Glide.with(context)
                .load(chatsList.get(position).message)
                .into(holder.imgMessage)
            holder.imgPlay.visibility = View.GONE
        }

        else if (chatsList.get(position).messageType.equals("video")) {
            holder.audioPause.visibility = View.GONE
            holder.audioPlay.visibility = View.GONE
            holder.audioSeekbar.visibility = View.GONE
            holder.imgPlay.visibility = View.VISIBLE
            holder.txtMessage.visibility = View.GONE
            holder.imgMessage.visibility = View.VISIBLE
            holder.imgLocation.visibility = View.GONE
            Glide.with(context)
                .load(chatsList.get(position).message)
                .into(holder.imgMessage)
        } else if (chatsList.get(position).messageType.equals("audio")) {
            holder.audioSeekbar.visibility=View.VISIBLE

            if(position==currentPlay){
                holder.audioPause.visibility = View.VISIBLE
                holder.audioPlay.visibility = View.INVISIBLE
                holder.audioSeekbar.setProgress(currentSeekTo)
            }else if(position==previousPlay){
                holder.audioPause.visibility = View.INVISIBLE
                holder.audioPlay.visibility = View.VISIBLE
                holder.audioSeekbar.setProgress(0)
            }else{
                holder.audioPause.visibility = View.INVISIBLE
                holder.audioPlay.visibility = View.VISIBLE
            }

            holder.imgPlay.visibility = View.GONE
            holder.imgMessage.visibility = View.GONE
            holder.txtMessage.visibility = View.GONE
        }

Log.d("position",position.toString())
        if(position==0){
            holder.txtDate.visibility = View.VISIBLE
            holder.txtDate.setText(getFormattedDate(chatsList.get(position).time.toString().toLong()))
        }else if (!MyUtils.convertIntoDate(chatsList.get(position).time.toString()).equals(MyUtils.convertIntoDate(chatsList.get(position-1).time.toString()))) {
            holder.txtDate.visibility = View.VISIBLE
            holder.txtDate.setText(getFormattedDate(chatsList.get(position).time.toString().toLong()))
        } else {
            holder.txtDate.visibility = View.GONE
        }
        if(chatsList.get(position).time!=null)
        holder.txtTime.setText(MyUtils.convertIntoTime((chatsList.get(position).time).toString()))

        holder.imgPlay.setOnClickListener {
            showDialog(
                chatsList.get(position).message,
                chatsList.get(position).messageType.toString()
            )
        }
        holder.imgMessage.setOnClickListener {
            showDialog(
                chatsList.get(position).message,
                chatsList.get(position).messageType.toString()
            )
        }

        holder.imgLocation.setOnClickListener {

            var location=chatsList.get(position).message!!.split(",")
            var latitude=location[0]
            var logitude=location[1]
            val strUri =
                "http://maps.google.com/maps?q=loc:$latitude,$logitude (Another User location)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
            intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            )
            context.startActivity(intent!!)

        }



        holder.audioPlay.setOnClickListener {
            previousPlay=currentPlay
            currentPlay=position

            if(previousPlay==currentPlay){
                playMediaPlayerFromResume(chatsList.get(position).message,holder)
//                notifyItemChanged(previousPlay)

            }else{
                playMediaPlayerFromBegin(chatsList.get(position).message,holder)
                notifyItemChanged(previousPlay)
            }

            holder.audioPlay.visibility = View.INVISIBLE
            holder.audioPause.visibility = View.VISIBLE


        }

        holder.audioPause.setOnClickListener {
            holder.audioPlay.visibility = View.VISIBLE
            holder.audioPause.visibility = View.INVISIBLE
            if(mediaPlayer!=null)
            mediaPlayer.pause()
            if(timer!=null)
            timer!!.cancel()
        }

    }


    fun getFormattedDate( smsTimeInMilis: Long): String? {
        val smsTime: Calendar = Calendar.getInstance()
        smsTime.setTimeInMillis(smsTimeInMilis)
        val now: Calendar = Calendar.getInstance()
        val dateTimeFormatString = "dd/MM/yyyy"
        val HOURS = (60 * 60 * 60).toLong()
        return if (now.get(Calendar.DATE) === smsTime.get(Calendar.DATE)) {
            "Today"
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) === 1) {
            "Yesterday"
        } else if (now.get(Calendar.YEAR) === smsTime.get(Calendar.YEAR)) {
            DateFormat.format(dateTimeFormatString, smsTime).toString()
        } else {
            DateFormat.format("dd/MM/yyyy", smsTime).toString()
        }
    }

    private fun playMediaPlayerFromBegin(audioUri: String?, holder:ChatLiveAdapter.viewHolder) {

        if (mediaPlayer != null && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()


            if (timer != null) {
                timer!!.cancel()
            }

        }
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setDataSource(audioUri.toString())
        mediaPlayer.prepareAsync()
        MyUtils.showProgress(context)
        mediaPlayer.setOnPreparedListener(MediaPlayer.OnPreparedListener { mp ->
            MyUtils.stopProgress(context)
            mp.start()

            timer = object : CountDownTimer(1000000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                    updateSeekbar(mp, holder.audioSeekbar)
                }

                override fun onFinish() {

                }
            }
            timer!!.start()

            mp.setOnCompletionListener {
                timer!!.cancel()
               holder.audioSeekbar!!.setProgress(0)
                holder.audioPause.visibility = View.INVISIBLE
                holder.audioPlay.visibility = View.VISIBLE
            }


           holder.audioSeekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    try {
                        mp.seekTo(p0!!.progress)
                    } catch (e: Exception) {
                        Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
                    }


                }
            })
        })

    }



    private fun playMediaPlayerFromResume(audioUri: String?, holder:ChatLiveAdapter.viewHolder) {
            mediaPlayer.start()

            timer = object : CountDownTimer(1000000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                    updateSeekbar(mediaPlayer, holder.audioSeekbar)
                }

                override fun onFinish() {

                }
            }
            timer!!.start()

            mediaPlayer.setOnCompletionListener {
                timer!!.cancel()
                holder.audioSeekbar!!.setProgress(0)
                holder.audioPause.visibility = View.INVISIBLE
                holder.audioPlay.visibility = View.VISIBLE
            }


            holder. audioSeekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    try {
                        mediaPlayer.seekTo(p0!!.progress)
                    } catch (e: Exception) {
                        Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
                    }


                }
            })



    }

    private fun updateSeekbar(mp: MediaPlayer, audioSeekbar: SeekBar?) {
//        currentSeekTo=mp.currentPosition
//        notifyItemChanged(currentPlay)
        audioSeekbar!!.max = mp.duration
        audioSeekbar!!.setProgress(mp.currentPosition)

    }

    private fun showDialog(url: String?, type: String) {
        var dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_image)
        var exoPlayer:ExoPlayer?=null
        var imgUser = dialog.findViewById<ImageView>(R.id.imgUser)
        var imgBack=dialog.findViewById<ImageView>(R.id.imgBack)
        var exoPlayerView = dialog.findViewById<SimpleExoPlayerView>(R.id.idExoPlayerVIew)

        dialog.getWindow()!!.setBackgroundDrawableResource(android.R.color.black);
        dialog.window!!.setLayout(
            GridLayoutManager.LayoutParams.MATCH_PARENT,
            GridLayoutManager.LayoutParams.MATCH_PARENT
        )
        imgBack.setOnClickListener {
            dialog.cancel()
        }

        if (type.equals("video")) {
            imgUser.visibility = View.GONE
            exoPlayerView.visibility = View.VISIBLE
            try {
                val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()

                val trackSelector: TrackSelector =
                    DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))

                 exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

                val videouri = Uri.parse(url)

                val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer_video")

                val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()

                val mediaSource: MediaSource =
                    ExtractorMediaSource(videouri, dataSourceFactory, extractorsFactory, null, null)

                exoPlayerView.setPlayer(exoPlayer)
                exoPlayer.prepare(mediaSource)
                exoPlayer.setPlayWhenReady(true)
            } catch (e:Exception){
               MyUtils.showToast(context,"Unable to play video")
            }
        } else {
            imgUser.visibility = View.VISIBLE
            exoPlayerView.visibility = View.GONE
            if (!url.equals("")) {
                Glide.with(context).load(url).placeholder(R.drawable.user).into(imgUser)
            }
        }




        dialog.setOnCancelListener {
            if(exoPlayer!=null){
                exoPlayer.stop()
            }
        }

        dialog.show()

    }

    override fun getItemCount(): Int {
        return chatsList.size;
    }

    override fun getItemViewType(position: Int): Int {

        if (chatsList.get(position).sender.equals(
                MyUtils.getStringValue(
                    context,
                    MyConstants.USER_PHONE
                )
            )
        ) {
            return 1
        } else {
            return 2
        }
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtMessage = itemView.findViewById<TextView>(R.id.txtMessage)
        var imgMessage = itemView.findViewById<ImageView>(R.id.imgMessage)
        var txtTime = itemView.findViewById<TextView>(R.id.txtTime)
        var txtDate = itemView.findViewById<TextView>(R.id.txtdate)
        var viewSeen = itemView.findViewById<View>(R.id.viewSeen)
        var imgPlay = itemView.findViewById<ImageView>(R.id.imgPlay)
        var audioPlay = itemView.findViewById<ImageView>(R.id.audioPlay)
        var audioPause = itemView.findViewById<ImageView>(R.id.audioPause)
        var audioSeekbar = itemView.findViewById<SeekBar>(R.id.audioSeekbar)
        var imgLocation=itemView.findViewById<ImageView>(R.id.imgLocation)
    }
}