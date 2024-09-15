package ir.shahabazimi.masterkeyexample.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import ir.shahabazimi.masterkeyexample.databinding.FragmentFingerprintBinding
import ir.shahabazimi.masterkeyexample.databinding.FragmentHubBinding
import ir.shahabazimi.masterkeyexample.ui.BaseFragment

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class HubFragment : BaseFragment<FragmentHubBinding>() {


    private val args by navArgs<HubFragmentArgs>()

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHubBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.text.setText(args.password.orEmpty())
    }

}