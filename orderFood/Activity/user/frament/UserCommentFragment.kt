package com.example.orderfood.activity.user.frament

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfood.R
import com.example.orderfood.activity.user.adapter.CommentAdapter
import com.example.orderfood.entity.Comment

import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class UserCommentFragment : Fragment(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var businessId: String? = null
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()

    companion object {
        private const val ARG_BUSINESS_ID = "business_id"

        fun newInstance(businessId: String): UserCommentFragment {
            return UserCommentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BUSINESS_ID, businessId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        arguments?.let {
            businessId = it.getString(ARG_BUSINESS_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.list_man_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}