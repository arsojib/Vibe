package com.beat.view.activity

import android.os.Bundle
import com.beat.R
import com.beat.base.BaseActivity
import com.beat.view.common.setting.SettingFragment
import com.beat.view.common.subscribtion.SubscriptionFragment
import com.beat.view.content.details.AlbumDetailsFragment
import com.beat.view.content.details.ArtistDetailsFragment
import com.beat.view.content.details.PlaylistDetailsFragment
import com.beat.view.content.download.DownloadFragment
import com.beat.view.content.favourite.FavoriteFragment
import com.beat.view.common.userPlaylist.MyPlaylistFragment
import com.beat.view.fragment.*

class SupportActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        initialComponent()
    }

    private fun initialComponent() {
        when {
            intent.getStringExtra("key")?.equals(getString(R.string.downloads))!! -> {
                moveToFragment(
                    DownloadFragment(), DownloadFragment()
                        .toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.favorites))!! -> {
                moveToFragment(FavoriteFragment(), FavoriteFragment().toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.settings))!! -> {
                moveToFragment(
                    SettingFragment(), SettingFragment()
                        .toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.refer_a_friend))!! -> {
                moveToFragment(ReferAFriendFragment(), ReferAFriendFragment().toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.notifications))!! -> {
                moveToFragment(NotificationFragment(), NotificationFragment().toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.playlists))!! -> {
                moveToFragment(
                    MyPlaylistFragment(), MyPlaylistFragment()
                        .toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.song))!! -> {
                moveToFragment(SongDetailsFragment(), SongDetailsFragment().toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.album))!! -> {
                moveToFragment(AlbumDetailsFragment(), AlbumDetailsFragment().toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.playlist))!! -> {
                moveToFragment(PlaylistDetailsFragment(), PlaylistDetailsFragment().toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.artist))!! -> {
                moveToFragment(ArtistDetailsFragment(), ArtistDetailsFragment().toString())
            }
            intent.getStringExtra("key")?.equals(getString(R.string.subscription))!! -> {
                moveToFragment(
                    SubscriptionFragment(), SubscriptionFragment()
                        .toString())
            }
        }
    }

}
