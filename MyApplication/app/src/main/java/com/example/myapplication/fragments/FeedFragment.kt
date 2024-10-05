package com.example.myapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.utils.model.BlogData
import com.example.myapplication.databinding.FragmentFeedBinding
import com.example.myapplication.utils.adapter.BlogAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var blogAdapter: BlogAdapter
    private lateinit var blogList: MutableList<BlogData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init() // Initialize components
        getBlogFromFirebase() // Fetch blogs from Firebase

        binding.goToFeedBtn.setOnClickListener {
            home()
        }
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference.child("Blogs") // Reference to the "Blogs" node

        binding.feedRecyclerView.setHasFixedSize(true)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(context)

        blogList = mutableListOf() // Initialize the blog list
        blogAdapter = BlogAdapter(blogList) // Create the adapter with the blog list
        binding.feedRecyclerView.adapter = blogAdapter // Set the adapter to the RecyclerView
    }

    private fun home() {
        findNavController().navigate(R.id.homeFragment)
    }

    private fun getBlogFromFirebase() {
        // Adding a listener to fetch data from the "Blogs" node
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear() // Clear the previous list
                for (taskSnapshot in snapshot.children) {
                    // Create BlogData objects from the snapshot
                    val blogId = taskSnapshot.key
                    val blogContent = taskSnapshot.value.toString()

                    if (blogId != null) {
                        val blogTask = BlogData(blogId, blogContent)
                        blogList.add(blogTask) // Add the blog data to the list
                    }
                }
                Log.d("FeedFragment", "onDataChange: $blogList")
                blogAdapter.notifyDataSetChanged() // Notify the adapter of data change
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show() // Show error message
            }
        })
    }
}
