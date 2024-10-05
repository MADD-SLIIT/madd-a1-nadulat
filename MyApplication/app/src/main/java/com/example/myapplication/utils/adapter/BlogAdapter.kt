package com.example.myapplication.utils.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.EachBlogItemBinding
import com.example.myapplication.fragments.HomeFragment
import com.example.myapplication.utils.model.BlogData

class BlogAdapter(private val list: MutableList<BlogData>) : RecyclerView.Adapter<BlogAdapter.TaskViewHolder>() {

    private  val TAG = "BlogAdapter"
    private var listener:TaskAdapterInterface? = null
    fun setListener(listener: HomeFragment){
        this.listener = listener
    }
    class TaskViewHolder(val binding: EachBlogItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            EachBlogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.todoTask.text = this.blog

                Log.d(TAG, "onBindViewHolder: "+this)
                binding.editTask.setOnClickListener {
                    listener?.onEditItemClicked(this , position)
                }

                binding.deleteTask.setOnClickListener {
                    listener?.onDeleteItemClicked(this , position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface TaskAdapterInterface{
        fun onDeleteItemClicked(blogData: BlogData , position : Int)
        fun onEditItemClicked(blogData: BlogData , position: Int)
    }

}