package com.youtubeproxy.player.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.youtubeproxy.player.databinding.ItemVideoBinding
import com.youtubeproxy.player.domain.model.VideoItem

class VideoAdapter(
    private val onItemClick: (VideoItem) -> Unit
) : ListAdapter<VideoItem, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding, onItemClick)
    }
    
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class VideoViewHolder(
        private val binding: ItemVideoBinding,
        private val onItemClick: (VideoItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(video: VideoItem) {
            binding.titleTextView.text = video.title
            binding.channelTextView.text = video.uploader
            binding.durationTextView.text = formatDuration(video.duration)
            binding.viewsTextView.text = formatViews(video.views)
            
            Glide.with(binding.thumbnailImageView.context)
                .load(video.thumbnailUrl)
                .centerCrop()
                .into(binding.thumbnailImageView)
            
            binding.root.setOnClickListener {
                onItemClick(video)
            }
        }
        
        private fun formatDuration(durationSeconds: Long): String {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
        
        private fun formatViews(views: Long): String {
            return when {
                views >= 1_000_000 -> String.format("%.1fM views", views / 1_000_000.0)
                views >= 1_000 -> String.format("%.1fK views", views / 1_000.0)
                else -> "$views views"
            }
        }
    }
    
    class VideoDiffCallback : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.videoId == newItem.videoId
        }
        
        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }
    }
}