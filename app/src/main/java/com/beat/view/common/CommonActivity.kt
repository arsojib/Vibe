package com.beat.view.common

import android.os.Bundle
import com.beat.R
import com.beat.base.BaseDaggerActivity
import com.beat.data.model.Track
import com.beat.util.Constants
import com.beat.view.common.queue.PlayQueueFragment
import com.beat.view.common.setting.SettingFragment
import com.beat.view.common.subscribtion.SubscriptionFragment
import com.beat.view.common.userPlaylist.AddToPlaylistFragment
import com.beat.view.common.userPlaylist.EditPlaylistTrackFragment
import com.beat.view.common.userPlaylist.MyPlaylistFragment

class CommonActivity : BaseDaggerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)

        initialComponent()
    }

    private fun initialComponent() {
        when {
            intent.getStringExtra("key")?.equals(Constants.ADD_PLAYLIST_TRACK)!! -> {
                val trackId = intent.getStringExtra("trackId")
                moveToFragment(
                    AddToPlaylistFragment.newInstance(trackId),
                    AddToPlaylistFragment().toString()
                )
            }
            intent.getStringExtra("key")?.equals(Constants.EDIT_PLAYLIST_TRACK)!! -> {
                val playlistId = intent.getStringExtra("playlistId")
                val playlistTitle = intent.getStringExtra("playlistTitle")
                moveToFragment(
                    EditPlaylistTrackFragment.newInstance(playlistId, playlistTitle),
                    EditPlaylistTrackFragment()
                        .toString()
                )
            }
            intent.getStringExtra("key")?.equals(Constants.USER_PLAYLIST)!! -> {
                moveToFragment(
                    MyPlaylistFragment(), MyPlaylistFragment()
                        .toString()
                )
            }
            intent.getStringExtra("key")?.equals(Constants.PLAY_QUEUE)!! -> {
                val track: Track? = intent.getParcelableExtra("track")
                moveToFragment(
                    PlayQueueFragment.newInstance(track), PlayQueueFragment()
                        .toString()
                )
            }
            intent.getStringExtra("key")?.equals(Constants.SUBSCRIPTION)!! -> {
                moveToFragment(
                    SubscriptionFragment(), SubscriptionFragment()
                        .toString()
                )
            }
            intent.getStringExtra("key")?.equals(Constants.SETTING)!! -> {
                moveToFragment(
                    SettingFragment(), SettingFragment()
                        .toString()
                )
            }

        }
    }

}