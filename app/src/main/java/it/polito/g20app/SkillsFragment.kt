package it.polito.g20app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class SkillsFragment : Fragment() {

    private val viewModel by viewModels<SkillVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_skills, container, false)
        activity?.findViewById<NavigationView?>(R.id.nav_view)?.setCheckedItem(R.id.nav_skills_list)

        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rv = root.findViewById<RecyclerView>(R.id.rv_skill)
        rv.layoutManager = LinearLayoutManager(root.context)

        viewModel.skills.observe(viewLifecycleOwner) {
            root.findViewById<TextView>(R.id.alert_skill).isVisible = it.isEmpty()
            val adapter = SkillAdapter(it as MutableList<Skill>)
            rv.adapter = adapter
        }

        return root
    }
}