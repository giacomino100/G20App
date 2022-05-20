package it.polito.g20app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels

class SkillsFragment : Fragment() {

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
        val viewModel by viewModels<SkillVM>()
        val tv = root.findViewById<TextView>(R.id.id_skill)
        viewModel.courses.observe(this.requireActivity()) {
            tv.text = it.joinToString("\n"){it.name}
        }

        return root



    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SkillsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}