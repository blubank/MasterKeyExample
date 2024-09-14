package ir.shahabazimi.masterkeyexample.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import ir.shahabazimi.masterkeyexample.R
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultType
import ir.shahabazimi.masterkeyexample.databinding.FragmentLoginBinding
import ir.shahabazimi.masterkeyexample.ui.BaseFragment
import ir.shahabazimi.masterkeyexample.utils.Constants.PASSWORD_SAVED_KEY
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class LoginFragment : BaseFragment<FragmentLoginBinding>() {


    private val viewModel: LoginViewModel by viewModel()
    private var isPasswordSaved = false


    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentLoginBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding) {
        isPasswordSaved = viewModel.isPasswordSaved(requireContext())
        if (isPasswordSaved) {
            loginButton.setIconResource(R.drawable.round_fingerprint)
        }
        viewModel.usernameSaved()?.let {
            username.setText(it)
        }

        loginButton.setOnClickListener {
            if (isPasswordSaved) {
                observeAuthenticateResult()
                viewModel.authenticate(requireActivity())
            } else {
                loginAndNavigate()

            }

        }

    }

    private fun loginAndNavigate() {
        if (dataIsValid())
            navigateToFingerprint()
        else
            Toast.makeText(context, "Complete the form", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToFingerprint() {
        findNavController().navigate(
            R.id.action_to_fingerprintFragment, bundleOf(
                PASSWORD_SAVED_KEY to binding.password.text.toString()
            )
        )
    }

    private fun observeAuthenticateResult() =
        viewModel.authenticateResult.observe(viewLifecycleOwner) {
            when (it.result) {
                AuthenticateResultType.SUCCESS -> navigateToMain(it.data)
                AuthenticateResultType.CANCELED -> Unit
                AuthenticateResultType.ERROR -> Toast.makeText(context, it.data, Toast.LENGTH_SHORT)
                    .show()

                AuthenticateResultType.REMOVED_KEY -> {
                    Toast.makeText(context, it.data, Toast.LENGTH_SHORT).show()
                    binding.loginButton.setIconResource(0)
                }
            }

        }

    private fun navigateToMain(password: String) {
        Toast.makeText(context, password, Toast.LENGTH_SHORT).show()
    }


    private fun dataIsValid(): Boolean = with(binding) {
        val username = username.text.toString().trim()
        val password = password.text.toString().trim()
        return username.isNotEmpty() && password.isNotEmpty()
    }

}