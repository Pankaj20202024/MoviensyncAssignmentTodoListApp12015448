package com.example.mytodolistapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mytodolistapp.R
import com.example.mytodolistapp.databinding.FragmentHomeBinding
import com.example.mytodolistapp.utils.adapter.TaskAdapter
import com.example.mytodolistapp.utils.model.ToDoData
import com.google.android.material.textfield.TextInputEditText


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment(), ToDoDialogFragment.OnDialogNextBtnClickListener,
    TaskAdapter.TaskAdapterInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private var popUpFragment: ToDoDialogFragment? = null
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var toDoItemList: MutableList<ToDoData>

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

        init(view)
        getTaskFromFirebase()
        registerEvents()
    }

    private fun registerEvents(){
        binding.addTaskBtn.setOnClickListener{
            if (popUpFragment != null)
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            popUpFragment = ToDoDialogFragment()
            popUpFragment!!.setListener(this)

            popUpFragment = ToDoDialogFragment()
            popUpFragment!!.setListener(this)
            popUpFragment!!.show(
                childFragmentManager, ToDoDialogFragment.TAG
            )
        }

    }

    private fun init(view: View){
        navController= Navigation.findNavController(view);
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.
        child("Tasks").child(auth.currentUser?.uid.toString())

        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)

        toDoItemList = mutableListOf()
        taskAdapter = TaskAdapter(toDoItemList)
        taskAdapter.setListener(this)
        binding.mainRecyclerView.adapter = taskAdapter
    }

    private fun getTaskFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                toDoItemList.clear()
                for (taskSnapshot in snapshot.children) {
                    val todoTask = taskSnapshot.key?.let {
                        ToDoData(it, taskSnapshot.value.toString())
                    }
                    if (todoTask != null) {
                        toDoItemList.add(todoTask)
                    }
                }
                taskAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun saveTask(todoTask: String, todoEdit: TextInputEditText) {
        databaseRef.push().setValue(todoTask).addOnCompleteListener{
            if (it.isSuccessful) {
                Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
        todoEdit.text = null
        popUpFragment!!.dismiss()
    }

    override fun updateTask(toDoData: ToDoData, todoEdit: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[toDoData.taskId] = toDoData.task
        databaseRef.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
            todoEdit.text = null
            popUpFragment!!.dismiss()
        }
    }

    override fun onDeleteItemClicked(toDoData: ToDoData, position: Int) {
        databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditItemClicked(toDoData: ToDoData, position: Int) {
        if (popUpFragment != null)
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()

        popUpFragment = ToDoDialogFragment.newInstance(toDoData.taskId, toDoData.task)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(
            childFragmentManager,
            ToDoDialogFragment.TAG
        )
    }

}