package ir.shahabazimi.masterkeyexample.ui.fingerprint

import android.view.LayoutInflater
import android.view.ViewGroup
import ir.shahabazimi.masterkeyexample.databinding.FragmentFingerprintBinding
import ir.shahabazimi.masterkeyexample.ui.BaseFragment

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class FingerprintFragment : BaseFragment<FragmentFingerprintBinding>() {

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFingerprintBinding.inflate(inflater, container, false)

}