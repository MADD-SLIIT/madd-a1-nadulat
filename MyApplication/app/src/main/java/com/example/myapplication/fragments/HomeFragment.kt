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
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.utils.adapter.BlogAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), BlogDialogFragment.OnDialogNextBtnClickListener,
    BlogAdapter.TaskAdapterInterface {

    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private var frag: BlogDialogFragment? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String

    private lateinit var blogAdapter: BlogAdapter
    private lateinit var blogItemList: MutableList<BlogData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        //get data from firebase
        getBlogFromFirebase()

        binding.logoutButton.setOnClickListener {
            logout()
        }
        binding.addTaskBtn.setOnClickListener {

            if (frag != null)
                childFragmentManager.beginTransaction().remove(frag!!).commit()
            frag = BlogDialogFragment()
            frag!!.setListener(this)

            frag!!.show(
                childFragmentManager,
                BlogDialogFragment.TAG
            )
            binding.goToHomeFeedBtn.setOnClickListener {
                feed()
            }

        }
    }
    private fun feed() {
        findNavController().navigate(R.id.feedFragment)
    }

    private fun logout() {
        auth.signOut() // Sign out the user
        findNavController().navigate(R.id.signInFragment) // Navigate back to SignInFragment
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun getBlogFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                blogItemList.clear()
                for (taskSnapshot in snapshot.children) {
                    val BlogTask =
                        taskSnapshot.key?.let { BlogData(it, taskSnapshot.value.toString()) }

                    if (BlogTask != null) {
                        blogItemList.add(BlogTask)
                    }

                }
                Log.d(TAG, "onDataChange: " + blogItemList)
                blogAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            }


        })
    }

    private fun init() {

        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        database = Firebase.database.reference.child("Tasks")
            .child(authId)


        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)

        blogItemList = mutableListOf()
        blogAdapter = BlogAdapter(blogItemList)
        blogAdapter.setListener(this)
        binding.mainRecyclerView.adapter = blogAdapter
    }

    override fun saveTask(blogTask: String, todoEdit: TextInputEditText) {

        database
            .push().setValue(blogTask)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show()
                    todoEdit.text = null

                } else {
                    Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        frag!!.dismiss()

    }

    override fun updateTask(blogData: BlogData, blogEdit: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[blogData.blogId] = blogData.blog
        database.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
            frag!!.dismiss()
        }
    }

    override fun onDeleteItemClicked(toDoData: BlogData, position: Int) {
        database.child(toDoData.blogId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditItemClicked(toDoData: BlogData, position: Int) {
        if (frag != null)
            childFragmentManager.beginTransaction().remove(frag!!).commit()

        frag = BlogDialogFragment.newInstance(toDoData.blogId, toDoData.blog)
        frag!!.setListener(this)
        frag!!.show(
            childFragmentManager,
            BlogDialogFragment.TAG
        )
    }

}