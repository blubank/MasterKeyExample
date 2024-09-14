package ir.shahabazimi.masterkeyexample.ui.hub

import android.view.LayoutInflater
import android.view.ViewGroup
import ir.shahabazimi.masterkeyexample.databinding.FragmentFingerprintBinding
import ir.shahabazimi.masterkeyexample.databinding.FragmentHubBinding
import ir.shahabazimi.masterkeyexample.ui.BaseFragment

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class HubFragment : BaseFragment<FragmentHubBinding>() {

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHubBinding.inflate(inflater, container, false)

}