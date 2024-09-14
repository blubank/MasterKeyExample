package ir.shahabazimi.masterkeyexample.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import ir.shahabazimi.masterkeyexample.databinding.FragmentLoginBinding

class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentLoginBinding.inflate(inflater, container, false)


}