package com.docimagetools.app.ui.image_resolution_changer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.docimagetools.app.databinding.FragmentImageResolutionChangerBinding

class ImageResolutionChangerFragment : Fragment() {

    private var _binding: FragmentImageResolutionChangerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val imageResolutionChangerViewModel =
            ViewModelProvider(this).get(ImageResolutionChangerViewModel::class.java)

        _binding = FragmentImageResolutionChangerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        imageResolutionChangerViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}