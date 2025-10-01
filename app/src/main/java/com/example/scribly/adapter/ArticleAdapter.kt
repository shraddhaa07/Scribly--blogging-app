package com.example.scribly.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.scribly.Model.BlogItemModel
import com.example.scribly.R
import com.example.scribly.databinding.ActivityYourArticleBinding
import com.example.scribly.databinding.ArticleItemBinding
import java.util.ArrayList

class ArticleAdapter(
    private val context: Context,
    private var blogList:List<BlogItemModel>,
    private val itemClickListener: OnItemClickListener
):RecyclerView.Adapter<ArticleAdapter.BlogViewHolder>() {

    interface OnItemClickListener{
        fun onEditCLick(blogItem: BlogItemModel)
        fun onDeleteCLick(blogItem: BlogItemModel)
        fun onReadMoreCLick(blogItem: BlogItemModel)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArticleAdapter.BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArticleItemBinding.inflate(inflater, parent,false)
        return BlogViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ArticleAdapter.BlogViewHolder, position: Int) {
        val blogItem =blogList[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return blogList.size
    }

    fun setData(blogSavedList: ArrayList<BlogItemModel>) {
        this.blogList =blogSavedList
        notifyDataSetChanged()
    }

    inner class BlogViewHolder(private val binding: ArticleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItem: BlogItemModel) {

            binding.intro.text = blogItem.heading
            binding.intro2.text = blogItem.post
            binding.userId.text = blogItem.userName
            binding.date.text = blogItem.date

            var imageUrl = blogItem.imageUrl ?: ""
            if (imageUrl.startsWith("http://")) {
                imageUrl = imageUrl.replace("http://", "https://")
            }
            Glide.with(binding.otherimage.context)
                .load(imageUrl.ifBlank { null })
                .placeholder(R.drawable.register)
                .error(R.drawable.love)
                .into(binding.otherimage)

            //handle read more click
            binding.readmore.setOnClickListener {
                itemClickListener.onReadMoreCLick(blogItem)
            }
            binding.edit.setOnClickListener {
                itemClickListener.onEditCLick(blogItem)
            }
            binding.delete.setOnClickListener {
                itemClickListener.onDeleteCLick(blogItem)
            }
        }

    }
}